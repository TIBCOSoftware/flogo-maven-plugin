package com.tibco.flogo.maven.pack;

public class FlogoPackageConfig {

    public static FlogoPackageConfig INSTANCE = new FlogoPackageConfig();

    private String outputPathPlatform;
    private String outputPath;
    private String flogoCLIPath;
    private String appPath;
    private String artifactId;
    private boolean crossPlatform;
    private String deployTarget;



    public void reset() {
        INSTANCE = new FlogoPackageConfig();
    }

    public void init(String flogoCLIPath, String flogoRuntimePath, String flogoConnectorsPath) throws Exception {

        this.flogoCLIPath = flogoCLIPath;
        initConfig();
    }

    private void initConfig() throws Exception {


    }

    public String getOutputPathPlatform() {
        return outputPathPlatform;
    }

    public void setOutputPathPlatform(String outputPathPlatform) {
        this.outputPathPlatform = outputPathPlatform;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFlogoCLIPath() {
        return flogoCLIPath;
    }

    public void setFlogoCLIPath(String flogoCLIPath) {
        this.flogoCLIPath = flogoCLIPath;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public boolean isCrossPlatform() {
        return crossPlatform;
    }

    public void setCrossPlatform(boolean crossPlatform) {
        this.crossPlatform = crossPlatform;
    }

    public String getDeployTarget() {
        return deployTarget;
    }

    public void setDeployTarget(String deployTarget) {
        this.deployTarget = deployTarget;
    }
}
