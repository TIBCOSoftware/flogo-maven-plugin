package com.tibco.flogo.maven.build;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlogoCLIRunner {

    public void run() throws Exception {

        if ( FlogoBuildConfig.INSTANCE.getLicenseFile() != null && !FlogoBuildConfig.INSTANCE.getLicenseFile().isEmpty() && !System.getProperty("os.name").toLowerCase().contains("linux") ) {
            throw new Exception( "License file can be only embedded on Linux platform");
        }
        runBuild(getLaunchConfig());


        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            if ( FlogoBuildConfig.INSTANCE.getCustomFQImage() != "" && FlogoBuildConfig.INSTANCE.getCrossPlatform() ) {
                runBuild(getLaunchConfigLinux());
            }  else {
            File file = Paths.get(FlogoBuildConfig.INSTANCE.getOutputPathPlatform(), FlogoBuildConfig.INSTANCE.getArtifactId()).toFile();
            file.createNewFile();
        }
        }
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
        List<String> launchConfig = new ArrayList<>();
//        launchConfig.add("/bin/sh -c /mnt/c/Codebase/Apps/Flogo/VS/rest-basic/build.sh");
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
        if (FlogoBuildConfig.INSTANCE.getLicenseFile() != "" && System.getProperty("os.name").toLowerCase().contains("linux")) {
            launchConfig.add("-l");
            launchConfig.add(FlogoBuildConfig.INSTANCE.getLicenseFile());
        } else {

        }

        launchConfig.add("-d");
        System.out.println( launchConfig.toString());
        return launchConfig;
    }

    private List<String> getLaunchConfigLinux() {
        List<String> launchConfig = new ArrayList<>();
        launchConfig.add("/bin/sh -c");
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
        launchConfig.add(FlogoBuildConfig.INSTANCE.getOutputPathPlatform());
        launchConfig.add("-n");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getArtifactId());
        launchConfig.add("-p");
        launchConfig.add("linux/amd64");
        launchConfig.add("-d");
        System.out.println( launchConfig.toString());
        return launchConfig;

    }


}
