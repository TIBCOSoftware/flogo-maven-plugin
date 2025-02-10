package com.tibco.flogo.maven.test;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import org.apache.commons.io.FilenameUtils;
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

    @Parameter(property = "appFilePath", defaultValue = "")
    private String appFilePath;

    @Parameter(property = "failIfNoTests", defaultValue = "false")
    private boolean failIfNoTests;

    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean skipTests;

    @Parameter(property = "testFailureIgnore", defaultValue = "false")
    private boolean testFailureIgnore;

    @Parameter(property = "saveActivityInputOutput", defaultValue = "false")
    private boolean preserveIO;

    @Parameter(property = "selectiveTestSuites", defaultValue = "")
    private String suites;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        FlogoTestConfig.INSTANCE.reset();
        getLog().info("FlogoTestMojo executed");
        try {

            if ( skipTests ) {
                getLog().info("Skip Test flag is set to true. Tests will be skipped.");
                return;
            }

            if (appFilePath == null || appFilePath.isEmpty()) {
                // App not provided explicitly. Check for flogo app in the base folder.
                appFilePath = Paths.get(projectBaseDir.getAbsolutePath(), artifactId+".flogo").toFile().getAbsolutePath();
                if ( new File(appFilePath).isFile() ) {
                } else {
                    throw new Exception( "No flogo app found with name => " + (artifactId+".flogo") + " in the project directory");
                }
            } else {
                File file = new File(appFilePath);
                if (file.isFile()) {
                    if (!file.isAbsolute()) {
                    } else {
                    }
                } else {
                    throw new Exception("Invalid Flogo App file path provided. Flogo path can be provided relative to the folder where the POM file is present or absolute path.");
                }
            }

            String appfileName = FilenameUtils.getBaseName( appFilePath);

            String appPath = new File( appFilePath).getParent();
            String testFilePath = Paths.get(appPath, appfileName+".flogotest").toFile().getAbsolutePath();
            if ( !new File(testFilePath).isFile() ) {
                if (failIfNoTests) {
                    throw new Exception( "No flogo tests found with name => " + (appfileName+".flogotest") + " in the project directory");
                } else {
                    getLog().info( "No flogo test found for the app. Tests will be skipped.");
                    return;
                }
            }

            FlogoTestConfig.INSTANCE.setAppBinary(Paths.get(outputDirectory.getAbsolutePath(), artifactId).toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestFilePath(testFilePath);
            FlogoTestConfig.INSTANCE.setSuites( suites);
            File testresult = new File(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            if (!testresult.exists()) {
                Files.createDirectory(testresult.toPath());
            }
            FlogoTestConfig.INSTANCE.setTestOutputDir(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestOutputFile(artifactId);
            FlogoTestConfig.INSTANCE.setPreserveIO(preserveIO);
            FlogoTestRunner runner = new FlogoTestRunner();
            runner.run();

        } catch (Exception e) {
            {

                if (e instanceof MojoFailureException) {
                    if (!testFailureIgnore) {
                        throw (MojoFailureException) e;
                    } else {
                        getLog().debug("Ignoring the exception for generating the report");
                    }
                } else {

                    e.printStackTrace();
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

}
