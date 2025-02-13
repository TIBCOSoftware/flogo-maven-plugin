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
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;



@Mojo(name = "flogocoveragereport", inheritByDefault = false)
public class FlogoCoverageReport extends AbstractMavenReport {

    @Component
    ProjectDependenciesResolver resolver;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    @Parameter(property = "showFailureDetails", defaultValue = "true")
    private boolean showFailureDetails;
    @Parameter(property = "testSuiteName", defaultValue = "")
    private String testSuiteName;

    @Parameter(property = "preserveIO", defaultValue = "false")
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
        return "Flogo Coverage Report";
    }

    @Override
    public String getName(Locale arg0) {

        return "Flogo Coverage Report";
    }

    @Override
    public String getOutputName() {
        return "flogo Coverage Report";
    }


    @Override
    public void execute() throws MojoExecutionException {

        super.execute();
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

        FlogoTestConfig.INSTANCE.reset();
        getLog().info("FlogoTestMojo executed");
        try {

            if (appFilePath == null || appFilePath.isEmpty()) {
                // App not provided explicitly. Check for flogo app in the base folder.
                appFilePath = Paths.get(projectBaseDir.getAbsolutePath(), artifactId + ".flogo").toFile().getAbsolutePath();
                if (new File(appFilePath).isFile()) {
                } else {
                    throw new Exception("No flogo app found with name => " + (artifactId + ".flogo") + " in the project directory");
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
            FlogoCoverageReportGenerator report = new FlogoCoverageReportGenerator();

            File testReport = new File(Paths.get(outputDirectory.getAbsolutePath(), "testresult", appfileName + ".testresult").toString());
            if (!testReport.exists()) {
                report.generateReportEmpty( getSink());
                return;
            }

            FlogoTestConfig.INSTANCE.setTestOutputDir(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestOutputFile(appfileName);



            getLog().info("Generating report ..");
            String content = FileHelper.readFile(Paths.get(FlogoTestConfig.INSTANCE.getTestOutputDir(), FlogoTestConfig.INSTANCE.getTestOutputFile() + ".testresult").toFile().getAbsolutePath(), Charset.defaultCharset());

            ObjectMapper mapper = new ObjectMapper();
            Root root = mapper.readValue(content, Root.class);

            AppParser parser = new AppParser();
            parser.parse(appFilePath, root);

            report.generateReport( parser, getSink());

        } catch (Exception e) {
            throw new MavenReportException(e.getMessage());
        }
    }

    }
