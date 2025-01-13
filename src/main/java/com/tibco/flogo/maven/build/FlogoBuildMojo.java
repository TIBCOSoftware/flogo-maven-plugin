package com.tibco.flogo.maven.build;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Mojo(name = "flogobuild", defaultPhase = LifecyclePhase.COMPILE)
public class FlogoBuildMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "project.basedir")
    private File projectBaseDir;

    @Parameter(property = "buildSource", defaultValue = "")
    private String buildSource;

    @Parameter(property = "flogoVSCodeExtensionPath", defaultValue = "")
    private String flogoVSCodeExtensionPath;

    @Parameter(property = "appFilePath", defaultValue = "")
    private String appFilePath;

    @Parameter(property = "customExtensionPath", defaultValue = "")
    private String customExtensionPath;

    @Parameter(property = "emsHome", defaultValue = "")
    private String emsHome;

    @Parameter(property = "mqHome", defaultValue = "")
    private String mqHome;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            FlogoBuildConfig.INSTANCE.reset();
            getLog().info("Flogo application binary build started");

            if (!outputDirectory.exists()) {
                Files.createDirectory(outputDirectory.toPath());
            }

            FlogoBuildConfig.INSTANCE.setOutputPath(outputDirectory.getPath());
            FlogoBuildConfig.INSTANCE.setArtifactId(artifactId);

            if (flogoVSCodeExtensionPath == null || flogoVSCodeExtensionPath.isEmpty()) {
                throw new Exception("VSIX File not provided. VSIX File required for building Flogo Application");
            }

            if (appFilePath == null || appFilePath.isEmpty()) {
                // App not provided explicitly. Check for flogo app in the base folder.
                String[] files = projectBaseDir.list((dir, name) -> name.endsWith(artifactId + ".flogo"));
                if (files.length > 0) {
                    if (Paths.get(projectBaseDir.getAbsolutePath(), files[0]).toFile().isFile()) {
                        FlogoBuildConfig.INSTANCE.setAppPath(Paths.get(projectBaseDir.getAbsolutePath(), files[0]).toFile().getAbsolutePath());
                    } else {
                        throw new Exception("Flogo app now found in the base folder. Please provide the path to the flogo app file");
                    }
                }
            } else {
                File file = new File(appFilePath);
                if (file.isFile()) {
                    if (!file.isAbsolute()) {
                        FlogoBuildConfig.INSTANCE.setAppPath(file.getCanonicalPath());
                    } else {
                        FlogoBuildConfig.INSTANCE.setAppPath(file.getAbsolutePath());
                    }

                } else {
                    throw new Exception("Invalid Flogo App file path provided. Flogo path can be provided relative to the folder where the POM file is present or absolute path.");
                }
            }

            VSIXExtractor.extract(flogoVSCodeExtensionPath);
            FlogoCLIRunner runner = new FlogoCLIRunner();
            runner.run();


        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }


    }

    boolean isValid(Class enumClass, String value) {
        return Arrays.stream(enumClass.getEnumConstants()).anyMatch((e) -> value.equals(
                e.toString()));
    }
}
