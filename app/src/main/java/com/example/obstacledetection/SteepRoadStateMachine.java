package com.example.obstacledetection;

import java.util.Arrays;

public class SteepRoadStateMachine {
    private int [] steepArr;
    private int states;

    public void setStates(int states) {
        this.states = states;
    }

    public SteepRoadStateMachine(int states) {
        this.states = states;
        steepArr = new int[ARSettings.numLabelCols];
        Arrays.fill(steepArr,0);
    }

    public boolean[] decideSteepAhead(int[][] dist_matrix, float current_angle){
        boolean [] steepRoadArr = new boolean[ARSettings.numLabelCols];
        for(int j=0;j<ARSettings.numLabelCols;j++) {
            int dyn_bound = (int)(ARSettings.dynamic_weight*current_angle);
            if(dist_matrix[ARSettings.numLabelRows-1][j]>= ARSettings.highBoundArr[ARSettings.numLabelRows-1] + dyn_bound){
                steepRoadArr[j] = true;
            }
        }
        return steepRoadArr;
    }

    public boolean[] updateSteepAheadStateMachine(boolean [] steepRoadArr){
        boolean[] result = new boolean[ARSettings.numLabelCols];

        for (int i = 0; i < ARSettings.numLabelCols; i++) {

            if(steepRoadArr[i] && steepArr[i]<states-1) steepArr[i]++;

            else if(!steepRoadArr[i] && steepArr[i]>0) steepArr[i]--;

            if (steepArr[i]==states-1) result[i] = true;
        }
        return result;
    }
}
