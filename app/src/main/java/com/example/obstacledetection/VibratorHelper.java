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

    public void vibrate(){//returns true if MainActivity.onSceneUpdate needs to stop
        if(isVibrating) return;

        isVibrating=true;
        vibrator.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
        for(int i=0;i<mainActivity.numLabelRows;i++){
            for(int j=0;j<mainActivity.numLabelCols;j++){
                mainActivity.text_array[i][j].setText("");
            }
        }
        mainActivity.text_array[1][0].setText("Παρακαλώ κρατήστε όρθια τη συσκευή");
        mainActivity.text_array[1][0].setTextColor(Color.WHITE);
    }

    public void stopVibrating(){
        isVibrating=false;
    }
}
