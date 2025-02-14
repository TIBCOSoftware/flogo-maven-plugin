package com.tibco.flogo.maven.build;

import com.tibco.flogo.maven.build.helpers.FlogoBuildConfig;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.FileSystems.newFileSystem;

public class VSIXExtractor {
    public static void extract(String vsixfile) {
        try {

            if (Files.isDirectory(Paths.get(vsixfile))) {
                String flogoBinaryPath ;
                if (SystemUtils.IS_OS_WINDOWS) {
                    flogoBinaryPath = Paths.get(vsixfile + File.separator + "bin" + File.separator + "flogo-vscode-cli.exe").toString();
                } else {
                    flogoBinaryPath = Paths.get(vsixfile + File.separator + "bin" + File.separator + "flogo-vscode-cli").toString();
                }
                String flogoRuntimePath = Paths.get(vsixfile, "media", "flogo-runtime").toString();
                String flogoConnPath = Paths.get(vsixfile, "media", "flogo-contributions", "wistudio", "v1", "contributions").toString();
                FlogoBuildConfig.INSTANCE.init(flogoBinaryPath, flogoRuntimePath, flogoConnPath);
                return;
            }


            File directory = new File(System.getProperty("user.home") + File.separator + ".flogo");
            if (!directory.exists()) {
                Files.createDirectory(directory.toPath());
            }
            Files.deleteIfExists(Paths.get(directory.getAbsolutePath() + File.separator + "extension.vsixmanifest"));
            extractSingleFile(Paths.get(vsixfile), "extension.vsixmanifest", Paths.get(directory.getAbsolutePath() + File.separator + "extension.vsixmanifest"));
            String vsixVersion = findVSIXVersion(Paths.get(directory.getAbsolutePath() + File.separator + "extension.vsixmanifest").toFile());
            Files.deleteIfExists(Paths.get(directory.getAbsolutePath() + File.separator + "extension.vsixmanifest"));
            if (Files.isDirectory(Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion))) {
                String flogoBinaryPath;
                if (SystemUtils.IS_OS_WINDOWS) {
                    flogoBinaryPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "bin" + File.separator + "flogo-vscode-cli.exe").toString();
                } else {
                    flogoBinaryPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "bin" + File.separator + "flogo-vscode-cli").toString();
                }
                String flogoRuntimePath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-runtime").toString();
                String flogoConnPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-contributions").toString();
                FlogoBuildConfig.INSTANCE.init(flogoBinaryPath, flogoRuntimePath, flogoConnPath);
                return;
            }

            Path tmpdir = Files.createTempDirectory(Paths.get(directory.getAbsolutePath()), "tmpDirPrefix");
            Files.createDirectory(Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion));
            unzip(vsixfile, tmpdir.toFile().getAbsolutePath());
            copyDir(Paths.get(tmpdir.toFile().getAbsolutePath() + File.separator + "extension" + File.separator + "media" + File.separator + "flogo-runtime"), Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-runtime"));
            copyDir(Paths.get(tmpdir.toFile().getAbsolutePath() + File.separator + "extension" + File.separator + "media" + File.separator + "flogo-contributions" + File.separator + "wistudio" + File.separator + "v1" + File.separator + "contributions"), Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-contributions"));
            copyDir(Paths.get(tmpdir.toFile().getAbsolutePath() + File.separator + "extension" + File.separator + "bin"), Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "bin"));
            FileUtils.deleteDirectory(tmpdir.toFile());


            String flogoBinaryPath;
            if (SystemUtils.IS_OS_WINDOWS) {
                flogoBinaryPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "bin" + File.separator + "flogo-vscode-cli.exe").toString();
            } else {
                flogoBinaryPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "bin" + File.separator + "flogo-vscode-cli").toString();
            }

            File binary = new File(flogoBinaryPath);
            binary.setExecutable(true);
            String flogoRuntimePath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-runtime").toString();
            String flogoConnPath = Paths.get(directory.getAbsolutePath() + File.separator + vsixVersion + File.separator + "flogo-contributions").toString();
            FlogoBuildConfig.INSTANCE.init(flogoBinaryPath, flogoRuntimePath, flogoConnPath);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String findVSIXVersion(File vsixfile) throws Exception {
        DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = db.newDocumentBuilder();
        Document xml = build.parse(vsixfile);
        xml.getDocumentElement().normalize();

        NodeList nodes = xml.getElementsByTagName("Identity");
        Element e = (Element) nodes.item(0);
        System.out.println(e.getAttribute("Version"));
        return e.getAttribute("Version");
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs(); // Create destination directory if it does not exist
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (entry.isDirectory()) {
                    // If the entry is a directory, make the directory
                    new File(filePath).mkdirs();
                } else {
                    // If the entry is a file, extract it
                    extractFile(zipIn, filePath);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    public static void copyDir(Path sourceDir, Path destinationDir) throws IOException {
        Files.walk(sourceDir)
                .forEach(sourcePath -> {
                    try {
                        Path targetPath = destinationDir.resolve(sourceDir.relativize(sourcePath));
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        System.out.format("I/O error: %s%n", ex);
                    }
                });
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File file = new File(filePath);
        // Ensure parent directories exist
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = zipIn.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    public static void extractSingleFile(Path zipFile, String fileName, Path outputFile) throws IOException {
        // Wrap the file system in a try-with-resources statement
        // to auto-close it when finished and prevent a memory leak

        final URI uri = URI.create("jar:" + zipFile.toUri());
        final Map<String,String> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem fileSystem = newFileSystem(uri,env)) {
            Path fileToExtract = fileSystem.getPath(fileName);
            Files.copy(fileToExtract, outputFile);
        }
    }
}

