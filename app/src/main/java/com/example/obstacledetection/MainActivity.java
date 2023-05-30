package com.example.obstacledetection;

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
import android.media.Image;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements FragmentOnAttachListener, BaseArFragment.OnTapArPlaneListener, BaseArFragment.OnSessionConfigurationListener, ArFragment.OnViewCreatedListener{
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

    private void buildModel() {
        ViewRenderable.builder()
                .setView(this, createArTile(title, getApplicationContext()))
                .build()
                .thenAccept(viewRenderable -> this.viewRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    Log.d("ERR", throwable.toString());
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }


    public View createArTile(String title, Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.rounded_border);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(8, 8, 8, 8);
        TextView view = new TextView(context);
        view.setText(title);
        view.setTextSize(18);
        view.setTextColor(Color.RED);
        view.setPadding(8, 8, 8, 8);
        linearLayout.addView(view);
        return linearLayout;
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
        buildModel();

        this.text_array = new TextView[4][3];
        this.text_array[0][0] = findViewById(R.id.text00);
        this.text_array[0][1] = findViewById(R.id.text01);
        this.text_array[0][2] = findViewById(R.id.text02);

        this.text_array[1][0] = findViewById(R.id.text10);
        this.text_array[1][1] = findViewById(R.id.text11);
        this.text_array[1][2] = findViewById(R.id.text12);

        this.text_array[2][0] = findViewById(R.id.text20);
        this.text_array[2][1] = findViewById(R.id.text21);
        this.text_array[2][2] = findViewById(R.id.text22);

        this.text_array[3][0] = findViewById(R.id.text30);
        this.text_array[3][1] = findViewById(R.id.text31);
        this.text_array[3][2] = findViewById(R.id.text32);

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
        arFragment.setOnViewCreatedListener(null);
        this.arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
//        this.arSceneView.getScene().addOnUpdateListener(this::onSceneUpdate);
        this.arSceneView.getPlaneRenderer().setEnabled(false);
        try {
            this.timer.schedule(this.timerTask,2000,100);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }

    }

    public void onSceneUpdate() {
        Image depthImage = null;
        Frame frame = this.arSceneView.getArFrame();
        try {
            depthImage = frame.acquireDepthImage16Bits(); //160*90
            // Use the depth image here.

            if(this.depthMap) this.custom_imageview.setImageBitmap(ImageToBitmap(depthImage));

            this.dist_matrix = getAverageDistances(depthImage,4,3);
            for(int i=0;i<4;i++){
                for(int j=0;j<3;j++){
                    this.text_array[i][j].setText(Integer.toString(this.dist_matrix[i][j]));
                }
            }
        } catch (NotYetAvailableException e) {
            // This means that depth data is not available yet.
            // Depth data will not be available if there are no tracked
            // feature points. This can happen when there is no motion, or when the
            // camera loses its ability to track objects in the surrounding
            // environment.
            for(int i=0;i<4;i++){
                for(int j=0;j<3;j++){
                    this.text_array[i][j].setText("Null");
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
        short dist;

        for(int height=0;height<depthImage.getHeight(); height++){
            for(int width=0; width<depthImage.getWidth();width++){
                byteIndex = width * plane.getPixelStride() + height * plane.getRowStride();
                dist = buffer.getShort(byteIndex);
                if(dist<1000){
                    bitmap.setPixel(width,height,Color.argb(128, 255,0,0));
                }
                else if(dist>1000 && dist<2000){
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
                mean_value += dist;
            }
        }
        return mean_value/((heightend-heightstart)*(widthend-widthstart));
    }

    public int[][] getAverageDistances(Image depthImage, int rows, int cols){
        int [][] distance_matrix = new int[rows][cols];

        int height_increment = depthImage.getHeight()/cols;//Image needs to be rotated 90 degrees so use cols instead of rows here
        int width_increment = depthImage.getHeight()/rows;

        for(int i=0; i<rows;i++){
            for(int j=0;j<cols;j++){
                distance_matrix[i][cols-j-1] = getAverageSubImageDist(depthImage,j*height_increment,(j+1)*height_increment,i*width_increment,(i+1)*width_increment);
            }
        }
        return distance_matrix;
    }

}