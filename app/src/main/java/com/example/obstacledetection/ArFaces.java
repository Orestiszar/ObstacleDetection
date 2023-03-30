package com.example.obstacledetection;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;

import android.widget.LinearLayout;

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

import com.google.ar.sceneform.rendering.CameraStream;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import com.google.ar.sceneform.ux.BaseArFragment;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;


public class ArFaces extends ArFragment implements BaseArFragment.OnTapArPlaneListener, BaseArFragment.OnSessionConfigurationListener, ArFragment.OnViewCreatedListener {

    private ViewRenderable viewRenderable;
    private String title = "Anchor";
    private Session session;

    public ArFaces() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setOnSessionConfigurationListener(this);
        this.setOnViewCreatedListener(this);
        this.setOnTapArPlaneListener(this);
        buildModel();
//        this.session = this.getArSceneView().getSession();
//        this.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

//    private void onSceneUpdate(FrameTime updatedTime) {
//
//    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        this.setOnViewCreatedListener(null);
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode
                        .DEPTH_OCCLUSION_ENABLED);
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

    private void buildModel() {
        ViewRenderable.builder()
                .setView(getActivity(), createArTile(title, getActivity().getApplicationContext()))
                .build()
                .thenAccept(viewRenderable -> this.viewRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    Log.d("ERR", throwable.toString());
                    Toast.makeText(getActivity(), "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
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

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        Image depthImage = null;
        Frame frame = this.getArSceneView().getArFrame();
        try {
            depthImage = frame.acquireDepthImage16Bits();
            // Use the depth image here.
            if(depthImage == null){
                Toast.makeText(getActivity(), "depthImage Null", Toast.LENGTH_LONG).show();
            }
            else{
                int debugdistance = getMillimetersDepth(depthImage, 95,30);
//                Toast.makeText(getActivity(),Integer.toString(depthImage.getWidth()) +" " + Integer.toString(depthImage.getHeight()) , Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), Integer.toString(debugdistance), Toast.LENGTH_LONG).show();
            }
        } catch (NotYetAvailableException e) {
            // This means that depth data is not available yet.
            // Depth data will not be available if there are no tracked
            // feature points. This can happen when there is no motion, or when the
            // camera loses its ability to track objects in the surrounding
            // environment.
        } finally {
            if (depthImage != null) {
                depthImage.close();
            }
        }

        if (viewRenderable == null) {
            Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(this.getArSceneView().getScene());
        anchorNode.setRenderable(viewRenderable);
    }


}