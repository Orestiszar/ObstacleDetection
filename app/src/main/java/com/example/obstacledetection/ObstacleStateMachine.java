package com.example.obstacledetection;

import java.security.PublicKey;
import java.util.Arrays;

public class ObstacleStateMachine {
    private MainActivity mainActivity;
    private int [][] stateArr;
    private int [] steepArr;
    private int states;

    public void setStates(int states) {
        this.states = states;
    }

    public ObstacleStateMachine(MainActivity mainActivity, int states) {
        this.mainActivity = mainActivity;
        this.states = states;
        stateArr = new int[this.mainActivity.numLabelRows][this.mainActivity.numLabelCols];
        steepArr = new int[this.mainActivity.numLabelCols];
        for(int[] row:stateArr){
            Arrays.fill(row,0);
        }
        Arrays.fill(steepArr,0);
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
        return result;
    }

    public boolean[] decideSteepAhead(boolean [] steepRoadArr){
        boolean[] result = new boolean[mainActivity.numLabelCols];

        for (int i = 0; i < mainActivity.numLabelCols; i++) {

            if(steepRoadArr[i] && steepArr[i]<states-1) steepArr[i]++;

            else if(!steepRoadArr[i] && steepArr[i]>0) steepArr[i]--;

            if (steepArr[i]==states-1) result[i] = true;
        }
        return result;
    }
}
