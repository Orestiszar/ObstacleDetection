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

    public boolean[] decideSteepAhead(boolean [] steepRoadArr){
        boolean[] result = new boolean[ARSettings.numLabelCols];

        for (int i = 0; i < ARSettings.numLabelCols; i++) {

            if(steepRoadArr[i] && steepArr[i]<states-1) steepArr[i]++;

            else if(!steepRoadArr[i] && steepArr[i]>0) steepArr[i]--;

            if (steepArr[i]==states-1) result[i] = true;
        }
        return result;
    }
}
