package com.tibco.flogo.maven.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibco.flogo.maven.test.FlogoTestConfig;
import com.tibco.flogo.maven.test.dto.Root;
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

import static com.tibco.flogo.maven.test.FlogoTestRunner.readFile;

@Mojo(name = "flogoreport", inheritByDefault = false)
@Execute(lifecycle = "flogotestreport", phase = LifecyclePhase.TEST)
public class FlogoTestReport extends AbstractMavenReport {

    @Component
    ProjectDependenciesResolver resolver;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    @Parameter(property = "showFailureDetails", defaultValue = "true")
    private boolean showFailureDetails;
    @Parameter(property = "testSuiteName", defaultValue = "")
    private String testSuiteName;


    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "project.basedir")
    private File projectBaseDir;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(property = "buildSource", defaultValue = "false")
    private String buildSource;

    @Parameter(property = "vsixPath", defaultValue = "false")
    private String vsixPath;

    @Parameter(property = "appFilePath", defaultValue = "false")
    private String appFilePath;

    @Parameter(property = "testFailureIgnore", defaultValue = "false")
    private boolean testFailureIgnore;

    @Override
    public String getDescription(Locale arg0) {
        return "Flogo Test Report";
    }

    @Override
    public String getName(Locale arg0) {

        return "flogotest";
    }

    @Override
    public String getOutputName() {
        return "flogotest";
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

            String appPath = projectBaseDir.toPath().toAbsolutePath().toString();
            if (appFilePath != null && !appFilePath.isEmpty()) {
                File file = new File(appFilePath);
                if (file.isFile()) {
                    if (!file.isAbsolute()) {
                        appPath = file.getCanonicalFile().getParentFile().getAbsolutePath();
                    } else {
                        appPath = file.getParentFile().getAbsolutePath();
                    }

                }
            }
            String[] files = new File(appPath).list((dir, name) -> name.endsWith(".flogotest"));
            if (files.length == 0) {
                throw new Exception("No tests files found in the base folder");
            }


            FlogoTestConfig.INSTANCE.setAppBinary(Paths.get(outputDirectory.getAbsolutePath(), artifactId).toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestFilePath(Paths.get(appPath, files[0]).toFile().getAbsolutePath());
            File testresult = new File(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            if (!testresult.exists()) {
                Files.createDirectory(testresult.toPath());
            }
            FlogoTestConfig.INSTANCE.setTestOutputDir(Paths.get(outputDirectory.getAbsolutePath(), "testresult").toFile().getAbsolutePath());
            FlogoTestConfig.INSTANCE.setTestOutputFile(artifactId);
            FlogoTestConfig.INSTANCE.setPreserveIO(true);

            getLog().info("Generating report");
            FlogoTestReportGenerator gen = new FlogoTestReportGenerator();
            String content = readFile(Paths.get(FlogoTestConfig.INSTANCE.getTestOutputDir(), FlogoTestConfig.INSTANCE.getTestOutputFile() + ".testresult").toFile().getAbsolutePath(), Charset.defaultCharset());

            ObjectMapper mapper = new ObjectMapper();
            Root root = mapper.readValue(content, Root.class);
            gen.generateReport(root, getSink());

        } catch (Exception e) {
            throw new MavenReportException(e.getMessage());
        }

    }
}
