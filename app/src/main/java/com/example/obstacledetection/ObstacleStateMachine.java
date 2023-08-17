package com.example.obstacledetection;

import java.util.Arrays;

public class ObstacleStateMachine {
    private MainActivity mainActivity;
    private int [][] stateArr;
    private int states;

    public ObstacleStateMachine(MainActivity mainActivity, int states) {
        this.mainActivity = mainActivity;
        this.states = states;
        stateArr = new int[this.mainActivity.numLabelRows][this.mainActivity.numLabelCols];
        for(int[] row:stateArr){
            Arrays.fill(row,0);
        }
    }

    public boolean[] decideObstacles(boolean [][] obstacleArr){
        boolean[] result = new boolean[mainActivity.numLabelCols];

        for (int i = 0; i < mainActivity.numLabelRows; i++) {
            for (int j = 0; j < mainActivity.numLabelCols; j++) {


                if(obstacleArr[i][j] && stateArr[i][j]<states-1) stateArr[i][j]++;

                else if(!obstacleArr[i][j] && stateArr[i][j]>0) stateArr[i][j]--;

                if (stateArr[i][j]==states-1) result[j] = true;
            }
        }

//        mainActivity.gyrotext.setText(String.valueOf(result[0]) + " " + String.valueOf(result[1]) + " " +  String.valueOf(result[2]) + "\n" + obstacleArr[3][0]);

        return result;
    }
}
