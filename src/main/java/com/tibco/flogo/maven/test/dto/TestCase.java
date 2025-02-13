package com.tibco.flogo.maven.test.dto;

import java.util.List;
import java.util.Map;

public class TestCase implements Comparable{
    public String testName;
    public String flowName;
    public List<Activity> activities;
    public List<Link> links;
    public ErrorHandler errorHandler;
    public TestResult testResult;
    public String testStatus;
    public Map<String,TestCase> subFlow;

    @Override
    public int compareTo(Object o) {
        return testName.compareTo(((TestCase)o).testName);
    }
}
