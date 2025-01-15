package com.tibco.flogo.maven.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppCoverage {

    Map<String, FlowCoverage> flowMap = new HashMap<>();


    public Map<String, FlowCoverage> getFlowMap() {
        return flowMap;
    }


}

