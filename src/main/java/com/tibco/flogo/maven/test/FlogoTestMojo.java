package com.tibco.flogo.maven.test;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mojo(name = "flogotest", defaultPhase = LifecyclePhase.TEST)
public class FlogoTestMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "project.basedir")
    private File projectBaseDir;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(property = "buildSource", defaultValue = "false")
    private String buildSource;

    @Parameter(property = "flogoVSCodeExtensionPath", defaultValue = "")
    private String flogoVSCodeExtensionPath;

    @Parameter(property = "appFilePath", defaultValue = "false")
    private String appFilePath;

    @Parameter(property = "testFailureIgnore", defaultValue = "false")
    private boolean testFailureIgnore;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        FlogoTestConfig.INSTANCE.reset();
        getLog().info("FlogoTestMojo executed");
        try {

            String appPath = projectBaseDir.toPath().toAbsolutePath().toString();
            if (appFilePath != null && !appFilePath.isEmpty()) {
                File file = new File(appFilePath);
                if (file.isFile()) {
                    if (!file.isAbsolute()) {
                        appPath = file.getCanonicalFile().getParentFile().getAbsolutePath();
                    } else {
                        appPath = file.getParentFile().getAbsolutePath();
                    }

                }
            }

            String[] files = new File(appPath).list((dir, name) -> name.endsWith(artifactId + ".flogotest"));
            if (files.length == 0) {
                throw new Exception("No tests files found in the base folder");
            }


            FlogoTestConfig.INSTANCE.setAppBinary(Paths.get(outputDirectory.getAbsolutePath(), artifactId).toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestFilePath(Paths.get(appPath, files[0]).toFile().getAbsolutePath());
            File testresult = new File(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            if (!testresult.exists()) {
                Files.createDirectory(testresult.toPath());
            }
            FlogoTestConfig.INSTANCE.setTestOutputDir(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestOutputFile(artifactId);
            FlogoTestConfig.INSTANCE.setPreserveIO(true);
            FlogoTestRunner runner = new FlogoTestRunner();
            runner.run();

        } catch (Exception e) {
            {

                if (e instanceof MojoFailureException) {
                    if (!testFailureIgnore) {
                        throw (MojoFailureException) e;

                    } else {
                        System.out.println("Ignoring the exception for generating the report");
                    }
                } else {

                    e.printStackTrace();
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

}
