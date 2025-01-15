package com.tibco.flogo.maven.coverage;

import com.tibco.flogo.maven.coverage.dto.ActivityIO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlowCoverage {


    private Set<String> transitions = new HashSet<>();

    private Set<String> activities = new HashSet<>();

    private Set<String> errorHandlerTransitions = new HashSet<>();

    private Set<String> errorHandlerActivities = new HashSet<>();

    private Set<String> transitionExec = new HashSet<>();

    private Set<String> activitiesExec = new HashSet<>();

    private Set<String> errorHandlerTransitionExec = new HashSet<>();

    private Set<String> errorHandlerActivitiesExec = new HashSet<>();

    private String flowName;


    private String testCaseName;


    private boolean flowExecuted = false;

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }


    public Set<String> getTransitions() {
        return transitions;
    }


    public Set<String> getActivities() {
        return activities;
    }


    public Set<String> getTransitionExec() {
        return transitionExec;
    }


    public Set<String> getActivitiesExec() {
        return activitiesExec;
    }

    public Set<String> getErrorHandlerTransitions() {
        return errorHandlerTransitions;
    }

    public Set<String> getErrorHandlerActivities() {
        return errorHandlerActivities;
    }

    public Set<String> getErrorHandlerTransitionExec() {
        return errorHandlerTransitionExec;
    }

    public Set<String> getErrorHandlerActivitiesExec() {
        return errorHandlerActivitiesExec;
    }

    public boolean isFlowExecuted() {
        return flowExecuted;
    }


    public void setFlowExecuted(boolean flowExecuted) {
        this.flowExecuted = flowExecuted;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

}
