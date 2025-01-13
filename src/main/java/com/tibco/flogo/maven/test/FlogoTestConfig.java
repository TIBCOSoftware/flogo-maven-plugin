package com.tibco.flogo.maven.test;

public class FlogoTestConfig {
    public static FlogoTestConfig INSTANCE = new FlogoTestConfig();
    private String appBinary;
    private String testFilePath;
    private String testOutputDir;
    private String testOutputFile;
    private boolean preserveIO;

    public String getAppBinary() {
        return appBinary;
    }

    public void setAppBinary(String appBinary) {
        this.appBinary = appBinary;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public void setTestFilePath(String testFilePath) {
        this.testFilePath = testFilePath;
    }

    public String getTestOutputDir() {
        return testOutputDir;
    }

    public void setTestOutputDir(String testOutputDir) {
        this.testOutputDir = testOutputDir;
    }

    public String getTestOutputFile() {
        return testOutputFile;
    }

    public void setTestOutputFile(String testOutputFile) {
        this.testOutputFile = testOutputFile;
    }

    public boolean getPreserveIO() {
        return preserveIO;
    }

    public void setPreserveIO(boolean preserveIO) {
        this.preserveIO = preserveIO;
    }

    public void reset() {
        INSTANCE = new FlogoTestConfig();
    }

    public void init() throws Exception {
        initConfig();
    }

    private void initConfig() throws Exception {


    }
}
