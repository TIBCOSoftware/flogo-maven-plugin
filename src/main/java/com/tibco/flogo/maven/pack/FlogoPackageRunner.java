package com.tibco.flogo.maven.pack;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlogoPackageRunner {

    public void run() throws Exception {
        runBuild(getLaunchConfig());
    }

    public void runBuild(List<String> launchConfig) throws Exception {
        Process process = null;
        ProcessBuilder pb = new ProcessBuilder(launchConfig);
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();

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
        if (!result.contains("Build Zip "+ Paths.get(FlogoBuildConfig.INSTANCE.getOutputPath(), FlogoBuildConfig.INSTANCE.getArtifactId() + ".zip").toFile().getAbsolutePath() + " built successfully")) {
            throw new Exception("Failed to build binary");
        }
        FileUtils.deleteDirectory( FlogoBuildConfig.INSTANCE.getOutputPathPlatform());

        System.out.println("run completed");
    }


    private List<String> getLaunchConfig() {
        List<String> launchConfig = new ArrayList<>();
        launchConfig.add(FlogoBuildConfig.INSTANCE.getFlogoCLIPath());
        launchConfig.add("app");
        launchConfig.add("export-build");
        launchConfig.add("-f");
        launchConfig.add(FlogoBuildConfig.INSTANCE.getAppPath());
        launchConfig.add("-b");
        launchConfig.add(Paths.get(FlogoBuildConfig.INSTANCE.getOutputPathPlatform(),"flogo-engine").toFile().getAbsolutePath());
        launchConfig.add("-o");
        launchConfig.add(Paths.get(FlogoBuildConfig.INSTANCE.getOutputPath(), FlogoBuildConfig.INSTANCE.getArtifactId() + ".zip").toFile().getAbsolutePath());

        return launchConfig;
    }


}
