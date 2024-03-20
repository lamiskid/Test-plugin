package com.plug.plug;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;


@Mojo(name = "write-test", defaultPhase = LifecyclePhase.COMPILE)
public class TestGenerator  extends AbstractMojo {


        @Parameter(defaultValue = "${project.build.directory}/site/jacoco/jacoco.xml", property = "jacocoReportPath")
        private String jacocoReportPath;

        @Parameter(property = "apikey", required = true)
        private String apikey;

        @Parameter(property = "apiendpoint", defaultValue = "https://api.openai.com/v1/chat/completions")
        private String apiendpoint;

        @Parameter(defaultValue = "${project}", readonly = true, required = true)
        private MavenProject project;

        @Override
        public void execute() throws MojoExecutionException {
            if (this.apikey.isEmpty()) {
                getLog().error("API Key is empty. The API is not read successfully.");
                return; // Prevent the rest of the plugin from starting
            } else {
                getLog().info("!! API Key injected successfully !!");
            }

            // Generate JaCoCo report before proceeding
            generateJaCoCoReport();

            try {
                ChatGpt.writeTestClassUsingJacocoAndMML(this.jacocoReportPath);
            } catch (IOException e) {
                getLog().error("Jacoco report file not found: " + this.jacocoReportPath
                        + ". Please consider generating the Jacoco Report before injecting the Plugin");
                throw new MojoExecutionException("Jacoco report file not found: " + this.jacocoReportPath, e);
            } catch (Exception e) {
                throw new MojoExecutionException("Error generating tests", e);
            }
        }

        private void generateJaCoCoReport() throws MojoExecutionException {
            final File projectDirectory = this.project.getBasedir();
            final String title = this.project.getArtifactId();

            final File executionDataFile = new File(this.project.getBuild().getDirectory(), "jacoco.exec");
            final File classesDirectory = new File(this.project.getBuild().getOutputDirectory()); // 'target/classes'
            final File sourceDirectory = new File(this.project.getBuild().getSourceDirectory()); // 'src/main/java'
            final File reportDirectory = new File(this.project.getBuild().getDirectory(), "site/jacoco");

            if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
                throw new MojoExecutionException("Could not create report directory: " + reportDirectory.getAbsolutePath());
            }

            try {
                // Read the jacoco.exec file
                ExecFileLoader execFileLoader = new ExecFileLoader();
                execFileLoader.load(executionDataFile);

                // Analyze the structure and create the coverage model
                CoverageBuilder coverageBuilder = new CoverageBuilder();
                Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
                analyzer.analyzeAll(classesDirectory);

                IBundleCoverage bundleCoverage = coverageBuilder.getBundle(title);

                // Generate the report
                HTMLFormatter htmlFormatter = new HTMLFormatter();
                IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDirectory));
                visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(), execFileLoader.getExecutionDataStore().getContents());
                visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));
                visitor.visitEnd();

                getLog().info("JaCoCo report generated: " + reportDirectory.getAbsolutePath());
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to generate JaCoCo report", e);
            }
        }




}

