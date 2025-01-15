package com.tibco.flogo.maven.coverage;

import java.util.HashMap;
import java.util.Map;

public class TestSuiteIOCoverage {
    Map<String,TestIOCoverage> testIOCoverageMap = new HashMap<String,TestIOCoverage>();

    public Map<String, TestIOCoverage> getTestIOCoverageMap() {
        return testIOCoverageMap;
    }
}
