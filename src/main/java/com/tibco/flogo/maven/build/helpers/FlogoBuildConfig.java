package com.tibco.flogo.maven.build.helpers;

public class FlogoBuildConfig {

    public static FlogoBuildConfig INSTANCE = new FlogoBuildConfig();

    private String outputPath;
    private String outputPathPlatform;
    private String flogoCLIPath;
    private String flogoRuntimePath;
    private String flogoConnectorsPath;
    private String customExtensionsPath;
    private String appPath;
    private String artifactId;
    private String version;
    private String emsHome;
    private String mqHome;
    private boolean crossPlatform;
    private String  tags;
    private String customFQImage;

    public String getCustomFQImage() {
        return customFQImage;
    }

    public void setCustomFQImage(String customFQImage) {
        this.customFQImage = customFQImage;
    }


    public String getOutputPathPlatform() {
        return outputPathPlatform;
    }

    public void setOutputPathPlatform(String outputPathPlatform) {
        this.outputPathPlatform = outputPathPlatform;
    }

    public boolean getCrossPlatform() {
        return crossPlatform;
    }

    public void setCrossPlatform(boolean crossPlatform) {
        this.crossPlatform = crossPlatform;
    }


    public String getEmsHome() {
        return emsHome;
    }

    public void setEmsHome(String emsHome) {
        this.emsHome = emsHome;
    }

    public String getMqHome() {
        return mqHome;
    }

    public void setMqHome(String mqHome) {
        this.mqHome = mqHome;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getFlogoRuntimePath() {
        return flogoRuntimePath;
    }

    public void setFlogoRuntimePath(String flogoRuntimePath) {
        this.flogoRuntimePath = flogoRuntimePath;
    }

    public String getFlogoConnectorsPath() {
        return flogoConnectorsPath;
    }

    public void setFlogoConnectorsPath(String flogoConnectorsPath) {
        this.flogoConnectorsPath = flogoConnectorsPath;
    }

    public String getCustomExtensionsPath() {
        return customExtensionsPath;
    }

    public void setCustomExtensionsPath(String customExtensionsPath) {
        this.customExtensionsPath = customExtensionsPath;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void reset() {
        INSTANCE = new FlogoBuildConfig();
    }

    public void init(String flogoCLIPath, String flogoRuntimePath, String flogoConnectorsPath) throws Exception {

        this.flogoCLIPath = flogoCLIPath;
        this.flogoRuntimePath = flogoRuntimePath;
        this.flogoConnectorsPath = flogoConnectorsPath;
        initConfig();
    }



    private void initConfig() throws Exception {


    }
}
