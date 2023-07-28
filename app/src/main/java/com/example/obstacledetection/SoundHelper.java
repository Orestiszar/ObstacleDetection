package com.example.obstacledetection;

import android.media.MediaPlayer;

public class SoundHelper {
    private MainActivity mainActivity;
    private MediaPlayer[] mp;

    public SoundHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mp = new MediaPlayer[] {MediaPlayer.create(mainActivity, R.raw.obstacle_left), MediaPlayer.create(mainActivity, R.raw.obstacle_right), MediaPlayer.create(mainActivity, R.raw.obstacle_ahead)};
    }

    public void playObstacleLeft(){
        if(mp[0].isPlaying()) return;
        mp[0].start();
    }

    public void playObstacleRight(){
        if(mp[1].isPlaying()) return;
        mp[1].start();
    }

    public void playObstacleAhead(){
        if(mp[2].isPlaying()) return;
        mp[2].start();
    }
}
