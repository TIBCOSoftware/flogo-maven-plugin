package com.tibco.flogo.maven.pack;

import com.tibco.flogo.maven.build.FlogoCLIRunner;
import com.tibco.flogo.maven.build.VSIXExtractor;
import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import org.apache.commons.lang3.SystemUtils;
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

@Mojo(name = "flogopackage", defaultPhase = LifecyclePhase.PACKAGE)
public class FlogoPackageMojo extends AbstractMojo {

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


    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;


    @Parameter(property = "deploymentTarget", defaultValue = "")
    private String deployTarget;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            FlogoBuildConfig.INSTANCE.reset();
            getLog().info("Flogo packaging started");

            if (deployTarget.equals("") || (!deployTarget.equals("tibco-platform") )) {
                throw new MojoFailureException("The deploy target is not supported. Only supported deploy targets are platform and docker.");
            }

            FlogoBuildConfig.INSTANCE.setOutputPath(outputDirectory.getPath());
            FlogoBuildConfig.INSTANCE.setOutputPathPlatform(Paths.get(outputDirectory.getPath(), "platform").toFile().getAbsolutePath());
            FlogoBuildConfig.INSTANCE.setArtifactId(artifactId);

            if (flogoVSCodeExtensionPath == null || flogoVSCodeExtensionPath.isEmpty()) {
                throw new Exception("VSIX File not provided. VSIX File required for building Flogo Application");
            }

            if (appFilePath == null || appFilePath.isEmpty()) {
                // App not provided explicitly. Check for flogo app in the base folder.
                appFilePath = Paths.get(projectBaseDir.getAbsolutePath(), artifactId + ".flogo").toFile().getAbsolutePath();
                if (new File(appFilePath).isFile()) {
                    FlogoBuildConfig.INSTANCE.setAppPath(appFilePath);
                } else {
                    throw new Exception("No flogo app found with name => " + (artifactId + ".flogo") + " in the project directory");
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

            boolean crossPlatform = false;
            if (!SystemUtils.IS_OS_LINUX) {
                crossPlatform = true;
            }

            FlogoBuildConfig.INSTANCE.setCrossPlatform(crossPlatform);
            FlogoPackageRunner runner = new FlogoPackageRunner();
            runner.run();

        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }
    }
}
