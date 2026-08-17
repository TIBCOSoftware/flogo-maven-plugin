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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

            if (projectBaseDir == null || !projectBaseDir.isDirectory()) {
                throw new MojoExecutionException("projectBaseDir is not a valid directory: " + projectBaseDir);
            }

            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Failed to create output directory: " + outputDirectory);
            }

            String baseName = (artifactId != null && !artifactId.isEmpty()) ? artifactId : projectBaseDir.getName();
            File zipFile = new File(outputDirectory, baseName + ".flogolib");

            zipDirectory(projectBaseDir, zipFile);

            projectHelper.attachArtifact(
                    session.getCurrentProject(),
                    "zip",
                    zipFile
            );

            getLog().info("Zipped project base directory to " + zipFile.getAbsolutePath());
        }catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }
    }

    /**
     * Recursively zips the contents of the given source directory into the target zip file.
     * The output zip file itself is skipped if it happens to reside inside the source directory.
     */
    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        Path sourcePath = sourceDir.toPath();
        Path zipPath = zipFile.toPath().toAbsolutePath().normalize();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> !path.toAbsolutePath().normalize().equals(zipPath))
                    .forEach(path -> {
                        String entryName = sourcePath.relativize(path).toString().replace(File.separatorChar, '/');
                        try {
                            zos.putNextEntry(new ZipEntry(entryName));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to add file to zip: " + path, e);
                        }
                    });
        }
    }

}
