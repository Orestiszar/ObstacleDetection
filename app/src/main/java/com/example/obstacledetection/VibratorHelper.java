package com.example.obstacledetection;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Color;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class VibratorHelper {
    private MainActivity mainActivity;
    private Vibrator vibrator;
    private boolean isVibrating;

    public VibratorHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        vibrator = (Vibrator) this.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            Log.e(TAG, "Vibrator not available.");
            mainActivity.finish(); // Close app
        }
    }

    public void vibrate(){
        if(isVibrating) return;

        isVibrating=true;
        vibrator.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public void stopVibrating(){
        isVibrating=false;
    }
}
