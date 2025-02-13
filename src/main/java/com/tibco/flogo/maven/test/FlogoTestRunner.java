package com.tibco.flogo.maven.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.flogo.maven.test.dto.Root;
import com.tibco.flogo.maven.utils.FileHelper;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FlogoTestRunner {




    public void run() throws Exception {
        Process process = null;
        ProcessBuilder pb = new ProcessBuilder(getLaunchConfig());
        pb.redirectErrorStream(true);  // Combine stdout and stderr
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


        String content = FileHelper.readFile(Paths.get(FlogoTestConfig.INSTANCE.getTestOutputDir(), FlogoTestConfig.INSTANCE.getTestOutputFile() + ".testresult").toFile().getAbsolutePath(), Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Root root = mapper.readValue(content, Root.class);
        if (root.result.failedSuites > 0) {
            throw new MojoFailureException("Tests failed");
        }
        System.out.println("tests completed");
    }

    private List<String> getLaunchConfig() {
        List<String> launchConfig = new ArrayList<String>();
        File binary = new File(FlogoTestConfig.INSTANCE.getAppBinary());
        binary.setExecutable(true);
        launchConfig.add(FlogoTestConfig.INSTANCE.getAppBinary());
        launchConfig.add("--test");
        launchConfig.add("--test-file");
        launchConfig.add(FlogoTestConfig.INSTANCE.getTestFilePath());
        launchConfig.add("--output-dir");
        launchConfig.add(FlogoTestConfig.INSTANCE.getTestOutputDir());
        launchConfig.add("--result-filename");
        launchConfig.add(FlogoTestConfig.INSTANCE.getTestOutputFile());
        launchConfig.add("--test-suites");
        launchConfig.add(FlogoTestConfig.INSTANCE.getSuites());
        if (FlogoTestConfig.INSTANCE.getPreserveIO()) {
            launchConfig.add("--test-preserve-io");
        } else {
            launchConfig.add("--collect-coverage");
        }
        return launchConfig;
    }
}
