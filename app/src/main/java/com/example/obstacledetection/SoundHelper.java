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
                MediaPlayer.create(mainActivity, R.raw.hold_device_up),
                MediaPlayer.create(mainActivity, R.raw.steep_ahead),
                MediaPlayer.create(mainActivity, R.raw.steep_left),
                MediaPlayer.create(mainActivity, R.raw.steep_right),
                MediaPlayer.create(mainActivity, R.raw.steep_right_and_left)
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

    public void playSteepAhead(){
        if(isPlaying) return;
        isPlaying=true;
        mp[6].start();
    }

    public void playSteepLeft(){
        if(isPlaying) return;
        isPlaying=true;
        mp[7].start();
    }

    public void playSteepRight(){
        if(isPlaying) return;
        isPlaying=true;
        mp[8].start();
    }

    public void playSteepLeftAndRight(){
        if(isPlaying) return;
        isPlaying=true;
        mp[9].start();
    }

    public boolean announceSteepRoad(boolean [] steepRoadArr){

        if(steepRoadArr[0] && steepRoadArr[1] && steepRoadArr[2]){
            //way too steep
            playSteepAhead();
        }
        else if(steepRoadArr[0] && steepRoadArr[2]){
            //steep left+right
            playSteepLeftAndRight();
        }
        else if(steepRoadArr[0]){
            //left
            playSteepLeft();
        }
        else if(steepRoadArr[2]){
            //right
            playSteepRight();
        }
        else if(steepRoadArr[1]){
            //ahead
            playSteepAhead();
        }
        else{
            return false;
        }
        return true;
    }

    public void announceObstacles(boolean [] obstacleArr){
        //if there is an obstacles in the middle and the left we call left
        //if there is an obstacles in the middle and the right we call right
        //we only call middle if the obstacle is specifically in the middle

        if(obstacleArr[0] && obstacleArr[1] && obstacleArr[2]){
            playWayBlocked();
        }
        else if(obstacleArr[0] && obstacleArr[2]){
            //left+right
            playLeftAndRight();
        }
        else if(obstacleArr[0]){
            //left
            playObstacleLeft();
        }
        else if(obstacleArr[2]){
            //right
            playObstacleRight();
        }
        else if(obstacleArr[1]){
            //ahead
            playObstacleAhead();
        }

    }
}
