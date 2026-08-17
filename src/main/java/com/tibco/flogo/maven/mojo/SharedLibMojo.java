package com.tibco.flogo.maven.mojo;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

@Mojo(name = "flogopackagelib", defaultPhase = LifecyclePhase.PACKAGE)
public class SharedLibMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "project.basedir")
    private File projectBaseDir;

    @Parameter(property = "buildSource", defaultValue = "")
    private String buildSource;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            getLog().info( "package called");
        }catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }
    }

}
