package com.example.obstacledetection;

import java.util.Arrays;

public class SteepRoadStateMachine {

    private MainActivity mainActivity;
    private int [] steepArr;
    private int states;

    public void setStates(int states) {
        this.states = states;
    }

    public SteepRoadStateMachine(MainActivity mainActivity, int states) {
        this.mainActivity = mainActivity;
        this.states = states;
        steepArr = new int[this.mainActivity.numLabelCols];
        Arrays.fill(steepArr,0);
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
