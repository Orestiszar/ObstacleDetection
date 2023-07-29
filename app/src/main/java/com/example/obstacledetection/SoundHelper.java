package com.example.obstacledetection;

import android.media.MediaPlayer;

import androidx.fragment.app.FragmentOnAttachListener;

public class SoundHelper {
    private MainActivity mainActivity;
    private MediaPlayer[] mp;
    private boolean isPlaying=false;

    public SoundHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mp = new MediaPlayer[] {MediaPlayer.create(mainActivity, R.raw.obstacle_left),
                MediaPlayer.create(mainActivity, R.raw.obstacle_right),
                MediaPlayer.create(mainActivity, R.raw.obstacle_ahead),
                MediaPlayer.create(mainActivity, R.raw.way_blocked),
                MediaPlayer.create(mainActivity, R.raw.left_and_right),
                MediaPlayer.create(mainActivity, R.raw.hold_device_up)
        };
        for(MediaPlayer player :mp){
            player.setOnCompletionListener((MediaPlayer mediaPlayer)->{
                isPlaying = false;
            });
        }
    }

    public void playObstacleLeft(){
        if(isPlaying) return;
        isPlaying=true;
        mp[0].start();
    }

    public void playObstacleRight(){
        if(isPlaying) return;
        isPlaying=true;
        mp[1].start();
    }

    public void playObstacleAhead(){
        if(isPlaying) return;
        isPlaying=true;
        mp[2].start();
    }

    public void playWayBlocked(){
        if(isPlaying) return;
        isPlaying=true;
        mp[3].start();
    }

    public void playLeftAndRight(){
        if(isPlaying) return;
        isPlaying=true;
        mp[4].start();
    }

    public void playHoldDeviceUp(){
        if(isPlaying) return;
        isPlaying=true;
        mp[5].start();
    }

    public void announceObstacles(boolean [] outBoundArr){
        //if there is an obstacles in the middle and the left we call left
        //if there is an obstacles in the middle and the right we call right
        //we only call middle if the obstacle is specifically in the middle

        if(outBoundArr[0] && outBoundArr[1] && outBoundArr[2]){
            playWayBlocked();
        }
        else if(outBoundArr[0] && outBoundArr[2]){
            //left+right
            playLeftAndRight();
        }
        else if(outBoundArr[0]){
            //left
            playObstacleLeft();
        }
        else if(outBoundArr[1]){
            //ahead
            playObstacleAhead();
        }
        else if(outBoundArr[2]){
            //right
            playObstacleRight();
        }
    }
}
