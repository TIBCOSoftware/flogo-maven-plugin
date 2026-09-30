package com.tibco.flogo.maven.mojo;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mojo(name = "flogopackageextension", defaultPhase = LifecyclePhase.PACKAGE)
public class FlogoExtensionMojo extends AbstractMojo {

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

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            getLog().info( "package called");

            if (projectBaseDir == null || !projectBaseDir.isDirectory()) {
                throw new MojoExecutionException("projectBaseDir is not a valid directory: " + projectBaseDir);
            }

            File parentDir = projectBaseDir.getParentFile();
            if (parentDir == null || !parentDir.isDirectory()) {
                throw new MojoExecutionException("Parent directory is not a valid directory: " + parentDir);
            }

            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Failed to create output directory: " + outputDirectory);
            }

            String baseName = (artifactId != null && !artifactId.isEmpty()) ? artifactId : projectBaseDir.getName();
            File zipFile = new File(outputDirectory, baseName + ".flogoextension");

            zipDirectory(parentDir, projectBaseDir, zipFile);

            // The .flogoextension zip is the primary deliverable for the "flogoextension" packaging,
            // so set it as the project's main artifact rather than attaching it as a
            // secondary artifact. Attaching it with type "flogoextension" would collide with the
            // main artifact's id (groupId:artifactId:flogoextension:version) and fail with
            // "An attached artifact must have a different ID than its corresponding main artifact".
            session.getCurrentProject().getArtifact().setFile(zipFile);

            getLog().info("Zipped project base directory to " + zipFile.getAbsolutePath());
        }catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }
    }

    /**
     * Recursively zips the contents of {@code contentDir} into the target zip file, with each
     * zip entry named relative to {@code baseDir}. This lets us root the archive at the parent
     * directory while only including the {@code contentDir} (project base) folder — sibling
     * folders under {@code baseDir} are not walked and therefore excluded.
     * The output zip file itself is skipped if it happens to reside inside {@code contentDir}.
     */
    private void zipDirectory(File baseDir, File contentDir, File zipFile) throws IOException {
        Path basePath = baseDir.toPath();
        Path contentPath = contentDir.toPath();
        Path zipPath = zipFile.toPath().toAbsolutePath().normalize();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(contentPath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> !path.toAbsolutePath().normalize().equals(zipPath))
                    .forEach(path -> {
                        String entryName = basePath.relativize(path).toString().replace(File.separatorChar, '/');
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
