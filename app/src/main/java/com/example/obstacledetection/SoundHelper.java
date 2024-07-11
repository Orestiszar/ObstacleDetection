package com.example.obstacledetection;

import android.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class SoundHelper {
    private MainActivity mainActivity;
    private boolean isPlaying=false;
    private Map<String,MediaPlayer> soundMap;

    public SoundHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        soundMap = new HashMap<String, MediaPlayer>();
        soundMap.put("obstacle_left",MediaPlayer.create(mainActivity, R.raw.obstacle_left));
        soundMap.put("obstacle_right",MediaPlayer.create(mainActivity, R.raw.obstacle_right));
        soundMap.put("obstacle_ahead",MediaPlayer.create(mainActivity, R.raw.obstacle_ahead));
        soundMap.put("way_blocked",MediaPlayer.create(mainActivity, R.raw.way_blocked));
        soundMap.put("left_and_right",MediaPlayer.create(mainActivity, R.raw.left_and_right));
        soundMap.put("hold_device_up",MediaPlayer.create(mainActivity, R.raw.hold_device_up));
        soundMap.put("steep_ahead",MediaPlayer.create(mainActivity, R.raw.steep_ahead));
        soundMap.put("steep_left",MediaPlayer.create(mainActivity, R.raw.steep_left));
        soundMap.put("steep_right",MediaPlayer.create(mainActivity, R.raw.steep_right));
        soundMap.put("steep_right_and_left",MediaPlayer.create(mainActivity, R.raw.steep_right_and_left));
        for(Map.Entry<String,MediaPlayer> entry :soundMap.entrySet()){
            entry.getValue().setOnCompletionListener((MediaPlayer mediaPlayer)->{
                isPlaying = false;
            });
        }
    }

    public void playSound(String sound){
        if(isPlaying) return;
        isPlaying=true;
        soundMap.get(sound).start();
    }

    public boolean announceSteepRoad(boolean [] steepRoadArr){
        if(steepRoadArr[0] && steepRoadArr[1] && steepRoadArr[2]) playSound("steep_ahead");//way too steep
        else if(steepRoadArr[0] && steepRoadArr[2]) playSound("steep_right_and_left");//steep left+right
        else if(steepRoadArr[0]) playSound("steep_left"); //left
        else if(steepRoadArr[2]) playSound("steep_right");//right
        else if(steepRoadArr[1]) playSound("steep_ahead"); //ahead
        else return false;
        return true; //used in mainactivity to determine if obstacle sounds are played(Steep roads have a )
    }

    public void announceObstacles(boolean [] obstacleArr){
        //if there is an obstacle in the middle and the left we call left
        //if there is an obstacle in the middle and the right we call right
        //we only call middle if the obstacle is specifically in the middle

        if(obstacleArr[0] && obstacleArr[1] && obstacleArr[2]) playSound("way_blocked");
        else if(obstacleArr[0] && obstacleArr[2]) playSound("left_and_right"); //left+right
        else if(obstacleArr[0]) playSound("obstacle_left"); //left
        else if(obstacleArr[2]) playSound("obstacle_right"); //right
        else if(obstacleArr[1]) playSound("obstacle_ahead"); //ahead
    }
}
