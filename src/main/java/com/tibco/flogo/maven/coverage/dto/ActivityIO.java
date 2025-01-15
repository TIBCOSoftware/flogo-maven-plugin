package com.tibco.flogo.maven.coverage.dto;

public class ActivityIO {

    private String activityName;
    private String input;
    private String output;
    private String error;

    public ActivityIO( String activityName, String input, String output, String error) {
        this.activityName = activityName;
        this.input = input;
        this.output = output;
        this.error = error;
    }
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    public String getActivityName() {
        return activityName;
    }

}
