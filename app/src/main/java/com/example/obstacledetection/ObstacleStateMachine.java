package com.example.obstacledetection;
import java.util.Arrays;

public class ObstacleStateMachine {
    private int [][] stateArr;
    private int states;

    public void setStates(int states) {
        if(states==1) this.states=2;
        else this.states = states;
    }

    public ObstacleStateMachine(int states) {
        this.states = states;
        if(states==1) this.states=2;
        stateArr = new int[ARSettings.numLabelRows][ARSettings.numLabelCols];
        for(int[] row:stateArr){
            Arrays.fill(row,0);
        }
    }

    public boolean[][] decideObstacles(int[][] dist_matrix, float current_angle){
        boolean [][] obstacleArr = new boolean[ARSettings.numLabelRows][ARSettings.numLabelCols];
        for(int i=0;i<ARSettings.numLabelRows;i++) {
            for (int j = 0; j < ARSettings.numLabelCols; j++) {
                int dyn_bound = (int)(ARSettings.dynamic_weight*current_angle);
                if(dist_matrix[i][j]<= ARSettings.lowBoundArr[i] + dyn_bound){
                    obstacleArr[i][j] = true;
                }
            }
        }
    return obstacleArr;
    }

    public boolean[] updateObstacleStateMachine(boolean [][] obstacleArr){
        boolean[] result = new boolean[ARSettings.numLabelCols];

        for (int i = 0; i < ARSettings.numLabelRows; i++) {
            for (int j = 0; j < ARSettings.numLabelCols; j++) {

                if(obstacleArr[i][j] && stateArr[i][j]<states-1) stateArr[i][j]++;

                else if(!obstacleArr[i][j] && stateArr[i][j]>0) stateArr[i][j]--;

                if (stateArr[i][j]==states-1) result[j] = true;
            }
        }
        return result;
    }
}
