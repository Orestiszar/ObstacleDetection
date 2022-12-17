package com.example.obstacledetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;


public class MainActivity extends AppCompatActivity{
    private ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private String title = "Anchor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}