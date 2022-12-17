package com.example.obstacledetection;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.ArFrontFacingFragment;
import com.google.ar.sceneform.ux.AugmentedFaceNode;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ArFaces extends ArFragment implements BaseArFragment.OnTapArPlaneListener, BaseArFragment.OnSessionConfigurationListener, ArFragment.OnViewCreatedListener {

    private ViewRenderable viewRenderable;
    private String title = "Anchor";

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
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        this.setOnViewCreatedListener(null);
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
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
        Toast.makeText(getActivity(),"ggggg", Toast.LENGTH_LONG).show();
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (viewRenderable == null) {
            Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(this.getArSceneView().getScene());
        anchorNode.setRenderable(viewRenderable);
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
}