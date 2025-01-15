package com.tibco.flogo.maven.coverage;

import java.util.HashMap;
import java.util.Map;

public class AppCoverage {

    Map<String, FlowCoverage> flowMap = new HashMap<>();

    Map<String, TestSuiteIOCoverage> testIOMap = new HashMap<>();

    public Map<String, FlowCoverage> getFlowMap() {
        return flowMap;
    }

    public Map<String, TestSuiteIOCoverage> getTestIOMap() {
        return testIOMap;
    }


}

