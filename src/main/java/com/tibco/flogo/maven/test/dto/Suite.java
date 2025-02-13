package com.tibco.flogo.maven.test.dto;

import java.util.List;

public class Suite implements  Comparable{
    public String suiteName;
    public List<TestCase> testCases;
    public SuiteResult suiteResult;

    @Override
    public int compareTo(Object o) {
        return suiteName.compareTo(((Suite)o).suiteName);
    }
}
