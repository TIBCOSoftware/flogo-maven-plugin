package com.tibco.flogo.maven.coverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private boolean processExecuted = false;

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

    public boolean isProcessExecuted() {
        return processExecuted;
    }


    public void setProcessExecuted(boolean processExecuted) {
        this.processExecuted = processExecuted;
    }
}
