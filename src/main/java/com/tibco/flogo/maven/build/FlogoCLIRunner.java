package com.tibco.flogo.maven.build;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlogoCLIRunner {

    public void run() throws Exception {
        runBuild(getLaunchConfig());
//        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
//            runBuild(getLaunchConfigLinux());
//        }
    }

    public void runBuild(List<String> launchConfig) throws Exception {
        Process process = null;
        ProcessBuilder pb = new ProcessBuilder(launchConfig);
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        // set environment variable u
        env.put("IBM_MQ_HOME", FlogoBuildConfig.INSTANCE.getMqHome());
        env.put( "EMS_HOME" , FlogoBuildConfig.INSTANCE.getEmsHome());

        // Combine stdout and stderr
        process = pb.start();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        int exitCode = process.waitFor();
        System.out.println(result);
        if (!result.contains("copied the built")) {
            throw new Exception("Failed to build binary");
        }

        System.out.println("run completed");
    }


    private List<String> getLaunchConfig() {
        List<String> launchConfig = new ArrayList<String>();
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoCLIPath());
        launchConfig.add("app");
        launchConfig.add("build");
        launchConfig.add("-f");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getAppPath());
        launchConfig.add("-b");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoRuntimePath());
        launchConfig.add("-c");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoConnectorsPath());
        launchConfig.add("-e");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getCustomExtensionsPath());
        launchConfig.add("-o");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getOutputPath());
        launchConfig.add("-n");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getArtifactId());
        launchConfig.add("-d");

        return launchConfig;
    }

    private List<String> getLaunchConfigLinux() {
        List<String> launchConfig = new ArrayList<String>();
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoCLIPath());
        launchConfig.add("app");
        launchConfig.add("build");
        launchConfig.add("-f");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getAppPath());
        launchConfig.add("-b");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoRuntimePath());
        launchConfig.add("-c");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoConnectorsPath());
        launchConfig.add("-o");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getOutputPath());
        launchConfig.add("-n");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getArtifactId() + "-linux-amd64");
        launchConfig.add("-p");
        launchConfig.add("linux/amd64");
        launchConfig.add("-d");
        return launchConfig;
    }


}
