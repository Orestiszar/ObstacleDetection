package com.example.obstacledetection;

public class ARSettings {
    protected static boolean depthMap=false;
    protected static final int numLabelRows=4;
    protected static final int numLabelCols=3;
    protected static int [] lowBoundArr = new int[] {2500,5000,4500,2000};
    protected static int [] highBoundArr = new int[] {5000,10000,9000,4000};
    protected static int dynamic_weight = 0;
    protected static int width_percentage = 60;
    protected static int timerPeriod = 200;
    protected static int maxYAngle = 20;
    protected static int minYAngle = -10;
    protected static int maxZAngle = 20;
    protected static int mean_percent = 50;
}
