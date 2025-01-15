package com.tibco.flogo.maven.coverage;

import com.tibco.flogo.maven.coverage.dto.ActivityIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowIOCoverage {

    private boolean startingFlow = false;
    private List<ActivityIO> activityList = new ArrayList<>();
    private List<ActivityIO> activityErrorList = new ArrayList<>();

    public List<ActivityIO> getActivityList() {
        return activityList;
    }

    public List<ActivityIO> getActivityErrorList() {
        return activityErrorList;
    }

    public boolean isStartingFlow() {
        return startingFlow;
    }

    public void setStartingFlow(boolean startingFlow) {
        this.startingFlow = startingFlow;
    }
}
