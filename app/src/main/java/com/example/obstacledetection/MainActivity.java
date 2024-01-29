package com.example.obstacledetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;

import android.media.Image;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements FragmentOnAttachListener, BaseArFragment.OnSessionConfigurationListener, ArFragment.OnViewCreatedListener{
    private ArFragment arFragment;
    private ArSceneView arSceneView;
    protected TextView[][] text_array;
    private FrameLayout outer_frame_layout;
    private ImageView custom_imageview;
    private Switch depthSwitch;
    private ImageView settingsButton;
    public TextView gyrotext;

    protected boolean depthMap=false;
    protected int[][] dist_matrix;
    protected final int numLabelRows=4;
    protected final int numLabelCols=3;
    protected int [] lowBoundArr = new int[] {3000,5000,2000,2000};
    protected int [] highBoundArr = new int[] {10000,10000,6000,4000};
    protected int dynamic_weight = 5;
    protected int width_percentage = 80;

    private Timer timer;
    private TimerTask timerTask;
    private int timerPeriod = 1000/10;

    private SensorHelper sensorHelper;
    private VibratorHelper vibratorHelper;
    private SoundHelper soundHelper;
    private ObstacleStateMachine obstacleStateMachine;
    private SteepRoadStateMachine steepRoadStateMachine;
    private DepthImageProcessor depthImageProcessor;

    public void startTimer(int delay){
        timer = new Timer("frame_timer");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()->onSceneUpdate());
            }
        };
        try {
            timer.schedule(timerTask,delay,timerPeriod);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stopTimer(){
        try {
            if(timer!=null){
                timer.cancel();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            return;
        }
        startTimer(2000);
        // Get updates from the accelerometer and magnetometer at a constant rate.
        sensorHelper.resumeSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop updating scene
        stopTimer();
        // Don't receive any more updates from either sensor.
        sensorHelper.pauseSensors();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        soundHelper = new SoundHelper(this);
        sensorHelper = new SensorHelper(this);
        vibratorHelper = new VibratorHelper(this);
        obstacleStateMachine = new ObstacleStateMachine(this,1000/timerPeriod); //num of fps so it takes a second
        steepRoadStateMachine = new SteepRoadStateMachine(this, 1000/timerPeriod);
        depthImageProcessor =  new DepthImageProcessor();

        outer_frame_layout = findViewById(R.id.outer_frame_layout);
        gyrotext = findViewById(R.id.gyrotext);
        custom_imageview = new ImageView(this);
        outer_frame_layout.addView(custom_imageview);

        depthSwitch = findViewById(R.id.depthSwitch);
        depthSwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b)->{
            depthMap = b;
            if(!b){
                custom_imageview.setImageBitmap(null);
            }});

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this::inflatePopupMenu);
    }

    public void inflatePopupMenu(View view){
        stopTimer();
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setOnDismissListener(()->{startTimer(0);});
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TableLayout tableLayout = popupView.findViewById(R.id.ETTable);
        EditText [][] ETArr = new EditText[4][2];
        for(int i=0;i<tableLayout.getChildCount(); i++){
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            for(int j=0; j< row.getChildCount(); j++){
                ETArr[i][j] = (EditText) row.getChildAt(j);
            }
        }

        EditText dynamic_weight_ET = popupView.findViewById(R.id.EtDynamicWeight);
        EditText width_offset_ET = popupView.findViewById(R.id.EtWidthOffset);
        EditText FPS_ET = popupView.findViewById(R.id.EtFPS);

        for(int i=0;i<4;i++) {
            ETArr[i][0].setText(Integer.toString(lowBoundArr[i]));
            ETArr[i][1].setText(Integer.toString(highBoundArr[i]));
        }

        dynamic_weight_ET.setText(Integer.toString(dynamic_weight));
        width_offset_ET.setText(Integer.toString(width_percentage));
        FPS_ET.setText(Integer.toString(1000/timerPeriod));

        Button setButton = popupView.findViewById(R.id.setPopupParamsButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(int i=0;i<4;i++) {
                    lowBoundArr[i] = Integer.parseInt(ETArr[i][0].getText().toString());
                    highBoundArr[i] = Integer.parseInt(ETArr[i][1].getText().toString());
                }
                dynamic_weight = Integer.parseInt(dynamic_weight_ET.getText().toString());

                width_percentage = Integer.parseInt(width_offset_ET.getText().toString());
                if(width_percentage>100) width_percentage=100;
                else if(width_percentage<0) width_percentage=0;

                int fps = Integer.parseInt(FPS_ET.getText().toString());
                timerPeriod = 1000/fps;
                obstacleStateMachine.setStates(fps);
                steepRoadStateMachine.setStates(fps);

                for(int i =0;i< numLabelRows ;i++){
                    for (int j=0;j<numLabelCols;j++){
                        outer_frame_layout.removeView(text_array[i][j]);
                    }
                }

                createLabelGrid();
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        else{
            Toast.makeText(this,"This device does not support ARCore depth",Toast.LENGTH_LONG);
            finish();
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        this.arSceneView = arSceneView;
        this.arFragment.setOnViewCreatedListener(null);
        this.arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        this.arSceneView.getPlaneRenderer().setEnabled(false);
        this.arSceneView.getPlaneRenderer().setVisible(false);
    }

    public void createLabelGrid(){
        Image depthImage = null;
        Frame frame = arSceneView.getArFrame();
        try{
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            int imageHeight = depthImage.getWidth();
            int imageWidth = depthImage.getHeight();//must be in reverse as we opt for portrait mode
            int imageViewHeight = custom_imageview.getHeight();
            int imageViewWidth = custom_imageview.getWidth();
            float aspectRatio = (float) imageWidth / imageHeight;
            int newWidth,newHeight;
            if (imageViewWidth / (float) imageWidth < imageViewHeight / (float) imageHeight) {
                newWidth = imageViewWidth;
                newHeight = (int) (newWidth / aspectRatio);
            } else {
                newHeight = imageViewHeight;
                newWidth = (int) (newHeight * aspectRatio);
            }
            // new width and new height are the dimensions of the screen that the labels need to represent. The ImageView is set to fit center.

            int true_width = (int)(newWidth*(width_percentage/100f));
            int widthOffset= (newWidth-true_width)/2;

            int horizontalStep = true_width/numLabelCols;
            int verticalStep = newHeight/numLabelRows;
            text_array = new TextView[numLabelRows][numLabelCols];
            for(int i=0; i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    text_array[i][j] = new TextView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = (verticalStep*i)+ (verticalStep/3) + ((imageViewHeight-newHeight)/2);       // Set the top margin in pixels
                    params.leftMargin = widthOffset + (horizontalStep*j) + horizontalStep/4 + ((imageViewWidth-newWidth)/2);  // Set the left margin in pixels
                    text_array[i][j].setTextSize(24);
                    text_array[i][j].setLayoutParams(params);
                    outer_frame_layout.addView(text_array[i][j]);
                }
            }
        }
        catch (NotYetAvailableException e){
            text_array=null;
        }
        finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }
    }

    public void onSceneUpdate() {
        if(text_array==null){
            createLabelGrid();
            return;//return to avoid reading from null array in the catch block
        }

        sensorHelper.updateOrientationAngles();
        gyrotext.setText(String.format(Locale.getDefault(),"x: %d \ny: %d\n z: %d", Math.round(sensorHelper.orientationAngles[0]),Math.round(sensorHelper.orientationAngles[1]),Math.round(sensorHelper.orientationAngles[2])));

        if(sensorHelper.orientationAngles[1]>30 || sensorHelper.orientationAngles[1]<-5 || Math.abs(sensorHelper.orientationAngles[2])>20){
            vibratorHelper.vibrate();
            for(int i=0;i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    text_array[i][j].setText("");
                }
            }
            text_array[1][0].setText("Παρακαλώ κρατήστε όρθια τη συσκευή");
            text_array[1][0].setTextColor(Color.WHITE);
            soundHelper.playHoldDeviceUp();
            return;
        }
        vibratorHelper.stopVibrating();

        Image depthImage = null;
        Frame frame = arSceneView.getArFrame();
        try {
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            // Use the depth image here.
            if(depthMap) custom_imageview.setImageBitmap(depthImageProcessor.ImageToBitmap(depthImage, numLabelRows, lowBoundArr,highBoundArr));

            dist_matrix = depthImageProcessor.getAverageDistances(depthImage,numLabelRows,numLabelCols, width_percentage);

            //to check and save if an obstacle exists in this column
            boolean [][] obstacleArr = new boolean[numLabelRows][numLabelCols]; //init to false
            boolean [] steepRoadArr = new boolean[numLabelCols]; //init to false

            for(int i=0;i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){

                    text_array[i][j].setText(Integer.toString(dist_matrix[i][j]));

                    int dyn_bound = (int)((dynamic_weight*sensorHelper.orientationAngles[1]*dist_matrix[i][j])/1000);

                    if(i==numLabelRows-1 && (dist_matrix[i][j] >= highBoundArr[i] + dyn_bound)){ //for steep roads
                        text_array[i][j].setTextColor(Color.BLUE);
                        steepRoadArr[j] = true;
                        continue;
                    }

                    if(dist_matrix[i][j]<= lowBoundArr[i] + dyn_bound){
                        text_array[i][j].setTextColor(Color.RED);
                        obstacleArr[i][j] = true;
                    }
                    else text_array[i][j].setTextColor(Color.WHITE);
                }
            }
            if(soundHelper.announceSteepRoad(steepRoadStateMachine.decideSteepAhead(steepRoadArr))) return;

            soundHelper.announceObstacles(obstacleStateMachine.decideObstacles(obstacleArr));

        } catch (NotYetAvailableException e) {
            // This means that depth data is not available yet.
            // Depth data will not be available if there are no tracked
            // feature points. This can happen when there is no motion, or when the
            // camera loses its ability to track objects in the surrounding
            // environment.
            for(int i=0;i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    text_array[i][j].setText("");
                }
            }

        } finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }
    }
}