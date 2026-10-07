package com.tibco.flogo.maven.mojo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
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

    /** Name of the Flogo shared-library manifest file. */
    private static final String FGMD_FILE_NAME = "app.fgmd";

    /**
     * Subfolders whose contents must be enumerated into the matching
     * {@code app.fgmd} array. The map key is the folder name (relative to the
     * project base dir) and it is also the JSON property name in the manifest.
     */
    private static final String[] RESOURCE_FOLDERS = {"flows", "connections", "schemas", "specs"};

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
            File zipFile = new File(outputDirectory, baseName + ".fglib");

            // Enrich the app.fgmd manifest with the flows/connections/schemas/specs
            // present in the project. The generated content is written into the zip
            // entry; the source app.fgmd on disk is left untouched.
            byte[] fgmdContent = buildAppFgmd(projectBaseDir);

            zipDirectory(projectBaseDir, zipFile, fgmdContent);

            // The .flogolib zip is the primary deliverable for the "flogolib" packaging,
            // so set it as the project's main artifact rather than attaching it as a
            // secondary artifact. Attaching it with type "flogolib" would collide with the
            // main artifact's id (groupId:artifactId:flogolib:version) and fail with
            // "An attached artifact must have a different ID than its corresponding main artifact".
            session.getCurrentProject().getArtifact().setFile(zipFile);

            getLog().info("Zipped project base directory to " + zipFile.getAbsolutePath());
        }catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to build Flogo Application ", e);
        }
    }

    /**
     * Recursively zips the contents of the given source directory into the target zip file.
     * The output zip file itself is skipped if it happens to reside inside the source directory.
     *
     * @param fgmdContent the enriched {@code app.fgmd} bytes to write for the manifest entry
     *                    (in place of the on-disk file); {@code null} to copy the file as-is.
     */
    private void zipDirectory(File sourceDir, File zipFile, byte[] fgmdContent) throws IOException {
        Path sourcePath = sourceDir.toPath();
        Path zipPath = zipFile.toPath().toAbsolutePath().normalize();
        Path targetPath = outputDirectory.toPath().toAbsolutePath().normalize();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> !path.toAbsolutePath().normalize().equals(zipPath))
                    .filter(path -> !path.toAbsolutePath().normalize().startsWith(targetPath))
                    .forEach(path -> {
                        String entryName = sourcePath.relativize(path).toString().replace(File.separatorChar, '/');
                        try {
                            zos.putNextEntry(new ZipEntry(entryName));
                            if (fgmdContent != null && FGMD_FILE_NAME.equals(entryName)) {
                                zos.write(fgmdContent);
                            } else {
                                Files.copy(path, zos);
                            }
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to add file to zip: " + path, e);
                        }
                    });
        }
    }

    /**
     * Reads the project's {@code app.fgmd} manifest and returns it enriched with the
     * {@code flows}, {@code connections}, {@code schemas} and {@code specs} arrays,
     * each populated with the base-dir-relative paths of the files found in the
     * matching subfolder. Existing manifest metadata (name, version, etc.) is preserved.
     *
     * @return the serialized manifest bytes.
     * @throws MojoExecutionException if no {@code app.fgmd} exists in the base dir.
     */
    private byte[] buildAppFgmd(File baseDir) throws IOException, MojoExecutionException {
        File fgmdFile = new File(baseDir, FGMD_FILE_NAME);
        if (!fgmdFile.isFile()) {
            throw new MojoExecutionException(FGMD_FILE_NAME + " not found in " + baseDir + ". The flogo project is not valid");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode manifest = (ObjectNode) mapper.readTree(fgmdFile);

        Path basePath = baseDir.toPath();
        for (String folder : RESOURCE_FOLDERS) {
            ArrayNode entries = manifest.putArray(folder);
            for (String relativePath : listResourceFiles(basePath, folder)) {
                entries.add(relativePath);
            }
        }

        String content = mapper.writer(new ManifestPrettyPrinter()).writeValueAsString(manifest);
        getLog().info("Enriched " + FGMD_FILE_NAME + " with "
                + RESOURCE_FOLDERS.length + " resource sections for packaging.");
        return content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the sorted, base-dir-relative paths of every file under {@code baseDir/folder}.
     * Paths use forward slashes so they match the zip entry names (which always use '/'),
     * allowing the Flogo tooling to resolve them inside the {@code .fglib} archive
     * (e.g. {@code flows/Logger.fgflow}). Returns an empty list when the folder is absent or empty.
     */
    private List<String> listResourceFiles(Path basePath, String folder) throws IOException {
        Path folderPath = basePath.resolve(folder);
        if (!Files.isDirectory(folderPath)) {
            return new ArrayList<>();
        }
        try (Stream<Path> files = Files.walk(folderPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(path -> basePath.relativize(path).toString().replace(File.separatorChar, '/'))
                    .sorted(Comparator.naturalOrder())
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * Pretty printer that mirrors JavaScript's {@code JSON.stringify(obj, null, 2)}, which is the
     * format the Flogo tooling produces for {@code app.fgmd}: two-space indentation with LF line
     * endings, each array element on its own line, and empty arrays rendered as {@code []}
     * (rather than Jackson's default {@code [ ]}).
     */
    private static class ManifestPrettyPrinter extends DefaultPrettyPrinter {

        ManifestPrettyPrinter() {
            DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
            indentObjectsWith(indenter);
            indentArraysWith(indenter);
        }

        ManifestPrettyPrinter(ManifestPrettyPrinter base) {
            super(base);
            _objectIndenter = base._objectIndenter;
            _arrayIndenter = base._arrayIndenter;
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new ManifestPrettyPrinter(this);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            // JSON.stringify uses "key": value (no space before the colon), unlike
            // Jackson's default "key" : value.
            g.writeRaw(": ");
        }

        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            if (nrOfValues == 0) {
                // Keep nesting balanced (writeStartArray incremented it) and emit "[]".
                if (!_arrayIndenter.isInline()) {
                    --_nesting;
                }
                g.writeRaw(']');
                return;
            }
            super.writeEndArray(g, nrOfValues);
        }
    }

}
