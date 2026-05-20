package com.tibco.flogo.maven.mojo;

import com.tibco.flogo.maven.build.VSIXExtractor;
import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import com.tibco.flogo.maven.test.FlogoTestConfig;
import com.tibco.flogo.maven.test.FlogoTestRunner;
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

        VSIXExtractor extractor = new VSIXExtractor();
        extractor.extract( flogoVSCodeExtensionPath);
        if (skipTests) {
                getLog().info( "-------------------------------------------------------" );
                getLog().info( "skipTests flag is set to true. Skipping Test phase.");
                getLog().info( "-------------------------------------------------------" );
                return;
        }
        FlogoTestConfig.INSTANCE.reset();
        getLog().info("FlogoTestMojo executed");
        try {

            if ( skipTests ) {
                getLog().info("Skip Test flag is set to true. Tests will be skipped.");
                return;
            }

            String appFile = null;
            String appTestFile = null;

            if (appFilePath == null || appFilePath.isEmpty()) {

                File base = new File(Paths.get( projectBaseDir.getAbsolutePath()).toFile().getAbsolutePath());
                File[] fdmdFiles = base.listFiles((dir, name) -> name.toLowerCase().endsWith(".fgmd"));
                //Check if the project is 2x or 3x
                if (fdmdFiles == null || fdmdFiles.length == 0) {
                    appFilePath = Paths.get(projectBaseDir.getAbsolutePath(), artifactId + ".flogo").toFile().getAbsolutePath();
                    if (new File(appFilePath).isFile()) {
                        appFile = appFilePath;
                        String appfileName = FilenameUtils.getBaseName( appFilePath);
                        String testFilePath = Paths.get(projectBaseDir.getAbsolutePath(), appfileName+".flogotest").toFile().getAbsolutePath();
                        appTestFile = testFilePath;

                    } else {
                        throw new Exception("Project is not a Flogo3 or Flogo 2 project.");
                    }
                } else {
                    File[] flogoFile = Paths.get(outputDirectory.getAbsolutePath()).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".flogo3"));
                    File[] flogotestFile = Paths.get(outputDirectory.getAbsolutePath()).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".flogotest3"));if  (flogoFile != null && flogoFile.length == 1) {appFile = flogoFile[0].getAbsolutePath();
                    }
                    if  (flogotestFile != null && flogotestFile.length == 1) {
                        appTestFile = flogotestFile[0].getAbsolutePath();
                    }
                }

            } else {
                File file = new File(appFilePath);
                if (file.isDirectory()) {
                    File[] fdmdFiles = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".fgmd"));
                    if (fdmdFiles == null || fdmdFiles.length == 0) {
                        throw new Exception("No Flogo app file with extension .fgmd found in directory => " + file.getAbsolutePath());
                    }
                    File[] flogotestFile = Paths.get(outputDirectory.getAbsolutePath()).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".flogotest3"));
                    if  (flogotestFile != null && flogotestFile.length == 1) {
                        appTestFile = flogotestFile[0].getAbsolutePath();
                    }

                } else {
                    String appfileName = FilenameUtils.getBaseName( appFilePath);
                    String appPath = new File( appFilePath).getParent();
                    appTestFile = Paths.get(appPath, appfileName+".flogotest").toFile().getAbsolutePath();
                }
            }

            if ( !new File(appTestFile).isFile() ) {
                if (failIfNoTests) {
                    throw new Exception( "No flogo tests in the project directory");
                } else {
                    getLog().info( "-------------------------------------------------------" );
                    getLog().info( "No flogo test file found for the app. Tests will be skipped.");
                    getLog().info( "-------------------------------------------------------" );

                    return;
                }
            }

            FlogoTestConfig.INSTANCE.setAppBinary(Paths.get(outputDirectory.getAbsolutePath(), artifactId).toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestFilePath(appTestFile);
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
