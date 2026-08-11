package com.tibco.flogo.maven.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.flogo.maven.coverage.AppParser;
import com.tibco.flogo.maven.test.FlogoTestConfig;
import com.tibco.flogo.maven.test.dto.Root;
import com.tibco.flogo.maven.utils.FileHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Locale;


@Mojo(name = "activityioreport", inheritByDefault = false)
public class ActivityIOReport extends AbstractMavenReport {

    @Component
    ProjectDependenciesResolver resolver;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    @Parameter(property = "showFailureDetails", defaultValue = "true")
    private boolean showFailureDetails;
    @Parameter(property = "testSuiteName", defaultValue = "")
    private String testSuiteName;


    @Parameter(property = "saveActivityInputOutput", defaultValue = "false")
    private boolean preserveIO;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "project.basedir")
    private File projectBaseDir;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(property = "appFilePath", defaultValue = "")
    private String appFilePath;



    @Override
    public String getDescription(Locale arg0) {
        return "Activity IO Report";
    }

    @Override
    public String getName(Locale arg0) {

        return "Activity IO Report";
    }

    @Override
    public String getOutputName() {
        return "Activity IO Report";
    }


    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

        FlogoTestConfig.INSTANCE.reset();
        getLog().info("FlogoTestMojo executed");
        try {

            if (appFilePath == null || appFilePath.isEmpty()) {
                // App not provided explicitly. Check for flogo app in the base folder.
                File base = new File(Paths.get( projectBaseDir.getAbsolutePath()).toFile().getAbsolutePath());
                File[] fdmdFiles = base.listFiles((dir, name) -> name.toLowerCase().endsWith(".fgmd"));
                //Check if the project is 2x or 3x
                if (fdmdFiles == null || fdmdFiles.length == 0) {

                        appFilePath = Paths.get(projectBaseDir.getAbsolutePath(), artifactId + ".flogo").toFile().getAbsolutePath();
                        if (new File(appFilePath).isFile()) {

                        } else {
                            throw new Exception("Project is not a Flogo3 or Flogo 2 project.");

                        }

                } else {
                    File[] flogoFile = Paths.get(outputDirectory.getAbsolutePath()).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".flogo3"));
                    appFilePath = flogoFile[0].getAbsolutePath();
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

            String appfileName = FilenameUtils.getBaseName(appFilePath);
            FlogoActivityIOReportGenerator report = new FlogoActivityIOReportGenerator();

            File testReport = new File(Paths.get(outputDirectory.getAbsolutePath(), "testresult", artifactId + ".testresult").toString());
            if (!testReport.exists()) {
                report.generateReportEmptytestFile( getSink());
                return;
            }
            if (!preserveIO) {
                report.generateReportEmpty( getSink());
                return;
            }

            FlogoTestConfig.INSTANCE.setTestOutputDir(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestOutputFile(appfileName);

            getLog().info("Generating report ..");
            String content = FileHelper.readFile(Paths.get(FlogoTestConfig.INSTANCE.getTestOutputDir(), artifactId + ".testresult").toFile().getAbsolutePath(), Charset.defaultCharset());

            ObjectMapper mapper = new ObjectMapper();
            Root root = mapper.readValue(content, Root.class);

            AppParser parser = new AppParser();
            parser.parse(appFilePath, root);

            if ( preserveIO) {
                report.generateReport( parser, getSink());
            }


        } catch (Exception e) {
            throw new MavenReportException(e.getMessage());
        }
    }

    }
