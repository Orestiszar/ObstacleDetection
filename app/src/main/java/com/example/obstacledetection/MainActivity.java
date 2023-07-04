package com.example.obstacledetection;

import static android.content.ContentValues.TAG;
import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Z;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener,FragmentOnAttachListener, BaseArFragment.OnTapArPlaneListener, BaseArFragment.OnSessionConfigurationListener, ArFragment.OnViewCreatedListener{
    private ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private String title = "Anchor";
    private ArSceneView arSceneView;
    private TextView[][] text_array;
    private FrameLayout outer_frame_layout;
    private ImageView custom_imageview;
    private Switch depthSwitch;
    private boolean depthMap=false;
    private int[][] dist_matrix;
    private Timer timer;
    private TimerTask timerTask;
    private final int numLabelRows=4;
    private final int numLabelCols=3;
    private final int timerPeriod = 333;
    private int lowbound=6000,highbound=10000;

    private TextView gyrotext;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] rotationMatrixRemapped = new float[9];
    private final float[] orientationAngles = new float[3];

    private Vibrator vibrator;
    private boolean isVibrating=false;

    private ImageView settingsButton;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void updateOrientationAngles() {
        // Compute the three orientation angles based on the most recent readings from
        // the device's accelerometer and magnetometer.
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "rotationMatrix" now has up-to-date information.

        //Change coordinate system
        SensorManager.remapCoordinateSystem(rotationMatrix, AXIS_X, AXIS_Z, rotationMatrixRemapped);

        SensorManager.getOrientation(rotationMatrixRemapped, orientationAngles);
        // "orientationAngles" now has up-to-date information.

        for(int i = 0; i < 3; i++) {
            orientationAngles[i] = (float)(Math.toDegrees(orientationAngles[i]));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get updates from the accelerometer and magnetometer at a constant rate.
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer == null) {
            Log.e(TAG, "Accelerometer not available.");
            finish(); // Close app
        }
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField == null) {
            Log.e(TAG, "Magnetometer not available.");
            finish(); // Close app
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            Log.e(TAG, "Vibrator not available.");
            finish(); // Close app
        }

        gyrotext = findViewById(R.id.gyrotext);

        outer_frame_layout = findViewById(R.id.outer_frame_layout);

        this.custom_imageview = new ImageView(this);
        outer_frame_layout.addView(this.custom_imageview);

        this.depthSwitch = findViewById(R.id.depthSwitch);
        this.depthSwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b)->{
            depthMap = b;
            if(!b){
                custom_imageview.setImageBitmap(null);
            }});

        timer = new Timer("frame_timer");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()->onSceneUpdate());
            }
        };

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this::inflatePopupMenu);
    }

    public void inflatePopupMenu(View view){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        EditText lowBoundET = popupView.findViewById(R.id.lowBoundET);
        EditText highBoundET = popupView.findViewById(R.id.highBoundET);
        lowBoundET.setText(Integer.toString(lowbound));
        highBoundET.setText(Integer.toString(highbound));
        Button setButton = popupView.findViewById(R.id.setPopupParamsButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowbound = Integer.parseInt(lowBoundET.getText().toString());
                highbound = Integer.parseInt(highBoundET.getText().toString());
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
            arFragment.setOnTapArPlaneListener(this);
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
        try {
            this.timer.schedule(this.timerTask,2000,timerPeriod);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void createLabelGrid(){
        Image depthImage = null;
        Frame frame = this.arSceneView.getArFrame();
        try{
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            int imageHeight = depthImage.getWidth();
            int imageWidth = depthImage.getHeight();//must me in reverse as we opt for portrait mode
            int imageViewHeight = this.custom_imageview.getHeight();
            int imageViewWidth = this.custom_imageview.getWidth();
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
            this.text_array=null;
        }
        finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }
    }

    public void onSceneUpdate() {

        if(this.text_array==null){
            createLabelGrid();
            return;//to ensure that labels are created
        }

        updateOrientationAngles();
        gyrotext.setText(String.format(Locale.getDefault(),"x: %d \ny: %d\n z: %d", Math.round(orientationAngles[0]),Math.round(orientationAngles[1]),Math.round(orientationAngles[2])));
        if(orientationAngles[1]>30 || orientationAngles[1]<-20 || Math.abs(orientationAngles[2])>20){
            if(!isVibrating){
                isVibrating=true;
                vibrator.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
                for(int i=0;i<this.numLabelRows;i++){
                    for(int j=0;j<this.numLabelCols;j++){
                        this.text_array[i][j].setText("");
                    }
                }
                this.text_array[1][0].setText("Παρακαλώ κρατήστε όρθια τη συσκευή");
                this.text_array[1][0].setTextColor(Color.WHITE);

            }
            return;
        }
        isVibrating = false;

        Image depthImage = null;
        Frame frame = this.arSceneView.getArFrame();
        try {
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            // Use the depth image here.

            if(this.depthMap) this.custom_imageview.setImageBitmap(ImageToBitmap(depthImage));

            this.dist_matrix = getAverageDistances(depthImage,this.numLabelRows,this.numLabelCols);
            for(int i=0;i<this.numLabelRows;i++){
                for(int j=0;j<this.numLabelCols;j++){
                    this.text_array[i][j].setText(Integer.toString(this.dist_matrix[i][j]));
                    if(this.dist_matrix[i][j]<=lowbound) this.text_array[i][j].setTextColor(Color.RED);
                    else this.text_array[i][j].setTextColor(Color.WHITE);
                }
            }
        } catch (NotYetAvailableException e) {
            // This means that depth data is not available yet.
            // Depth data will not be available if there are no tracked
            // feature points. This can happen when there is no motion, or when the
            // camera loses its ability to track objects in the surrounding
            // environment.
            for(int i=0;i<this.numLabelRows;i++){
                for(int j=0;j<this.numLabelCols;j++){
                    this.text_array[i][j].setText("Null");
                    this.text_array[i][j].setTextColor(Color.WHITE);
                }
            }

        } finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNode.setRenderable(viewRenderable);
    }

    /** Obtain the depth in millimeters for depthImage at coordinates (x, y). */
    public int getMillimetersDepth(Image depthImage, int x, int y) {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.
        Image.Plane plane = depthImage.getPlanes()[0];
        int byteIndex = x * plane.getPixelStride() + y * plane.getRowStride();
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
        return buffer.getShort(byteIndex);
    }

    public Bitmap ImageToBitmap(Image depthImage) {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.

        Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());

        Bitmap bitmap = Bitmap.createBitmap(depthImage.getWidth(), depthImage.getHeight(), Bitmap.Config.ARGB_8888);
        int byteIndex;
        int dist;

        for(int height=0;height<depthImage.getHeight(); height++){
            for(int width=0; width<depthImage.getWidth();width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<0) dist = 65536-dist;//to deal with overflowing due to signed shorts
                if(dist>=0 && dist<lowbound){
                    bitmap.setPixel(width,height,Color.argb(128, 255,0,0));
                }
                else if(dist>=lowbound && dist<highbound){
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