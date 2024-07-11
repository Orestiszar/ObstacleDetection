package com.example.obstacledetection;

import android.util.Log;

import java.util.ArrayList;

public class DroppedFramesCalculator {
    public static long calculateDroppedFrames(ArrayList<Long> deltaList, int targetFPS) {
        double expectedInterval = 1_000_000_000 / targetFPS; // For 60 fps, it will be 1000 / 60 ≈ 16.67 ms
        int droppedFrames = 0;
        for (long interval: deltaList) {
            if (interval > expectedInterval * 1.5) { // Allow some tolerance (e.g., 1.5 times the expected interval)
                droppedFrames += (interval / expectedInterval) - 1;
            }
        }
        return droppedFrames;
    }

    public static long calculateTotalFrames(ArrayList<Long> deltaList, int targetFPS) {
        long totalDuration = 0;
        for (long interval : deltaList) {
            totalDuration += interval;
        }
        double expectedInterval = 1_000_000_000 / targetFPS; // For 30 fps, it will be 1_000_000_000 / 30 ≈ 33,333,333.33 ns

        return (int) Math.round(totalDuration / expectedInterval);
    }

    public static long crossReference(ArrayList<Long> deltaList, int targetFPS) {
        double frameDurationInNanoseconds = 1_000_000_000.0 / targetFPS;
        long totalDuration = 0;
        for (long interval : deltaList) {
            totalDuration += interval;
        }
        long numberOfFrames = (long)(totalDuration / frameDurationInNanoseconds);
        return numberOfFrames;
    }
}