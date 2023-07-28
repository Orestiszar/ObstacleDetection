package com.example.obstacledetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
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
    public TextView[][] text_array;
    private FrameLayout outer_frame_layout;
    private ImageView custom_imageview;
    private Switch depthSwitch;
    private ImageView settingsButton;
    private TextView gyrotext;//debugging

    private boolean depthMap=false;
    private int[][] dist_matrix;
    public final int numLabelRows=4;
    public final int numLabelCols=3;
    private int [] lowBoundArr = new int[] {6000,6000,2000,1000};
    private int [] highBoundArr = new int[] {10000,10000,6000,6000};
    private int dynamic_weight = 0;

    private Timer timer;
    private TimerTask timerTask;
    private final int timerPeriod = 333;

    private SensorHelper sensorHelper;
    private VibratorHelper vibratorHelper;
    private SoundHelper soundHelper;

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
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
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

        gyrotext = findViewById(R.id.gyrotext);

        outer_frame_layout = findViewById(R.id.outer_frame_layout);

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


        EditText [][] ETArr = new EditText[4][2];
        ETArr[0][0] = popupView.findViewById(R.id.EtLow0);
        ETArr[1][0] = popupView.findViewById(R.id.EtLow1);
        ETArr[2][0] = popupView.findViewById(R.id.EtLow2);
        ETArr[3][0] = popupView.findViewById(R.id.EtLow3);

        ETArr[0][1] = popupView.findViewById(R.id.EtHigh0);
        ETArr[1][1] = popupView.findViewById(R.id.EtHigh1);
        ETArr[2][1] = popupView.findViewById(R.id.EtHigh2);
        ETArr[3][1] = popupView.findViewById(R.id.EtHigh3);

        EditText dynamic_weight_ET = findViewById(R.id.EtDynamicWeight);

        for(int i=0;i<4;i++) {
            ETArr[i][0].setText(Integer.toString(lowBoundArr[i]));
            ETArr[i][1].setText(Integer.toString(highBoundArr[i]));
        }

        Button setButton = popupView.findViewById(R.id.setPopupParamsButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(int i=0;i<4;i++) {
                    lowBoundArr[i] = Integer.parseInt(ETArr[i][0].getText().toString());
                    highBoundArr[i] = Integer.parseInt(ETArr[i][1].getText().toString());
                }
                dynamic_weight = Integer.parseInt(dynamic_weight_ET.getText().toString());
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
        Frame frame = this.arSceneView.getArFrame();
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
            int horizontalStep = newWidth/this.numLabelCols;
            int verticalStep = newHeight/this.numLabelRows;
            this.text_array = new TextView[this.numLabelRows][this.numLabelCols];
            for(int i=0; i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    if(this.text_array[i][j]!=null) continue;
                    this.text_array[i][j] = new TextView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = (verticalStep*i)+ (verticalStep/3) + ((imageViewHeight-newHeight)/2);       // Set the top margin in pixels
                    params.leftMargin = (horizontalStep*j) + horizontalStep/4 + ((imageViewWidth-newWidth)/2);  // Set the left margin in pixels
                    this.text_array[i][j].setTextSize(24);
                    this.text_array[i][j].setLayoutParams(params);
                    outer_frame_layout.addView(this.text_array[i][j]);
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
            return;//Avoid reading from null array in the catch block
        }

        sensorHelper.updateOrientationAngles();
        gyrotext.setText(String.format(Locale.getDefault(),"x: %d \ny: %d\n z: %d", Math.round(sensorHelper.orientationAngles[0]),Math.round(sensorHelper.orientationAngles[1]),Math.round(sensorHelper.orientationAngles[2])));
        if(sensorHelper.orientationAngles[1]>30 || sensorHelper.orientationAngles[1]<-20 || Math.abs(sensorHelper.orientationAngles[2])>20){
            vibratorHelper.vibrate();
            return;
        }
        vibratorHelper.stopVibrating();

        Image depthImage = null;
        Frame frame = arSceneView.getArFrame();
        try {
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            // Use the depth image here.

            if(depthMap) custom_imageview.setImageBitmap(ImageToBitmap(depthImage));

            this.dist_matrix = getAverageDistances(depthImage,numLabelRows,numLabelCols);
            for(int i=0;i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    text_array[i][j].setText(Integer.toString(dist_matrix[i][j]));
                    if(dist_matrix[i][j]<=lowBoundArr[i] + (dynamic_weight*sensorHelper.orientationAngles[1])) text_array[i][j].setTextColor(Color.RED);
                    else text_array[i][j].setTextColor(Color.WHITE);
                }
            }
        } catch (NotYetAvailableException e) {
            // This means that depth data is not available yet.
            // Depth data will not be available if there are no tracked
            // feature points. This can happen when there is no motion, or when the
            // camera loses its ability to track objects in the surrounding
            // environment.
            for(int i=0;i<numLabelRows;i++){
                for(int j=0;j<numLabelCols;j++){
                    text_array[i][j].setText("Null");
                    text_array[i][j].setTextColor(Color.WHITE);
                }
            }

        } finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }
    }

    public Bitmap ImageToBitmap(Image depthImage) {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.

        Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());

        Bitmap bitmap = Bitmap.createBitmap(depthImage.getWidth(), depthImage.getHeight(), Bitmap.Config.ARGB_8888);
        int byteIndex;
        int dist;
        int width_increment = depthImage.getWidth()/numLabelRows;

        for(int height=0;height<depthImage.getHeight(); height++){
            for(int width=0; width<depthImage.getWidth();width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<0) dist = 65536-dist;//to deal with overflowing due to signed shorts
                if(dist>=0 && dist<lowBoundArr[width/width_increment]){
                    bitmap.setPixel(width,height,Color.argb(128, 255,0,0));
                }
                else if(dist>=lowBoundArr[width/width_increment] && dist<highBoundArr[width/width_increment]){
                    bitmap.setPixel(width,height,Color.argb(128, 0,255,0));
                }
                else{
                    bitmap.setPixel(width,height,Color.argb(128, 0,0,255));
                }
            }
        }
        //rotate 90 degrees because depthImage is in landscape mode
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public int getAverageSubImageDist(Image depthImage, int heightstart, int heightend, int widthstart, int widthend){
        //starts inclusive, ends not inclusive
        Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
        int byteIndex, dist, mean_value=0;
        for(int height=heightstart;height<heightend; height++){
            for(int width=widthstart; width<widthend;width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<0) dist = 65536-dist;//to deal with overflowing due to signed shorts
                mean_value += dist;
            }
        }
        return mean_value/((heightend-heightstart)*(widthend-widthstart));
    }

    public int[][] getAverageDistances(Image depthImage, int rows, int cols){
        int [][] distance_matrix = new int[rows][cols];

        int height_increment = depthImage.getHeight()/cols;//Image needs to be rotated 90 degrees so use cols instead of rows here
        int width_increment = depthImage.getWidth()/rows;

        for(int i=0; i<rows;i++){//assume the image is horizontal
            for(int j=0;j<cols;j++){
                distance_matrix[i][cols-j-1] = getAverageSubImageDist(depthImage,j*height_increment,(j+1)*height_increment,i*width_increment,(i+1)*width_increment);
            }
        }
        return distance_matrix;
    }
}