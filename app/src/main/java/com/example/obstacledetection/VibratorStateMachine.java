package com.example.obstacledetection;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;


public class VibratorStateMachine {
    private MainActivity mainActivity;
    private Vibrator vibrator;
    private boolean isVibrating;
    private int states;
    private int currState=0;

    public VibratorStateMachine(MainActivity mainActivity, int states){
        this.mainActivity = mainActivity;
        this.states = states;
        if(states==1) this.states=2;
        vibrator = (Vibrator) this.mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            Log.e(TAG, "Vibrator not available.");
            mainActivity.finish(); // Close app
        }
    }

    public void setStates(int states) {
        if(states==1) this.states=2;
        else this.states = states;
    }

    public boolean updateVibratorStateMachine(float yAngle, float zAngle){
        boolean isOutOfBounds = (yAngle>ARSettings.maxYAngle || yAngle<ARSettings.minYAngle || Math.abs(zAngle)>ARSettings.absZAngle);

        if(isOutOfBounds && currState<states-1) currState++;
        else if (!isOutOfBounds && currState>0) currState--;

        return currState==states-1;
    }

    public void vibrate(){
        if(isVibrating) return;

        isVibrating=true;

        long[] timings = new long[] {0, 1000,1000};
        int repeat = 1;
        VibrationEffect repeatingEffect = VibrationEffect.createWaveform(timings, repeat);

        vibrator.vibrate(repeatingEffect);
    }

    public void stopVibrating(){
        isVibrating=false;
        vibrator.cancel();
    }
}
