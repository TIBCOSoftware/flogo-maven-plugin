package com.tibco.flogo.maven.coverage;

import java.util.HashMap;
import java.util.Map;

public class TestIOCoverage {

    Map<String,FlowIOCoverage> flowIOCoverageMap = new HashMap<String,FlowIOCoverage>();

    public Map<String, FlowIOCoverage> getFlowIOCoverageMap() {
        return flowIOCoverageMap;
    }

    public void setFlowIOCoverageMap(Map<String, FlowIOCoverage> flowIOCoverageMap) {
        this.flowIOCoverageMap = flowIOCoverageMap;
    }

}
