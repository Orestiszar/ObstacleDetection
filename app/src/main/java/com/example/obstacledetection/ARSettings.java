package com.example.obstacledetection;

public class ARSettings {
    protected static boolean depthMap=false;
    protected static final int numLabelRows=4;
    protected static final int numLabelCols=3;
    protected static int [] lowBoundArr = new int[] {3000,5000,2000,2000};
    protected static int [] highBoundArr = new int[] {10000,10000,6000,4000};
    protected static int dynamic_weight = 5;
    protected static int width_percentage = 80;
    protected static int timerPeriod = 100;
}
