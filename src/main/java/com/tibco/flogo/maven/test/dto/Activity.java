package com.tibco.flogo.maven.test.dto;

import java.util.List;
import java.util.Map;

public class Activity {
    public String name;
    public List<AssertionResult> assertionResult;
    public String activityStatus;
    public String type;
    public Map<String, Object> input;
    public Map<String, Object> output;
    public Error error;
}
