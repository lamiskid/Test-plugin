package com.plug.plug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import com.plug.plug.util.Constants;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Counts the number of maven dependencies of a project.
 * <p>
 * It can be filtered by scope.
 */
@Mojo(name = "dependency-counter", defaultPhase = LifecyclePhase.COMPILE)
public class counter extends AbstractMojo {

    private Properties properties;



    /**
     * Scope to filter the dependencies.
     */
    @Parameter(property = "scope")
    String scope;

    /**
     * Gives access to the Maven project information.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/jacoco-reports")
    private String jacocoReportsDirectory;

    @Parameter(defaultValue = "${project.build.directory}/surefire-reports")
    private String reportsDirectory;




    @Parameter(property = "property1", defaultValue = "defaultValue")
    private String property1;

    @Parameter(property = "property2", defaultValue = "42")
    private int property2;

   /* @Parameter(property = Constants.MODEL, defaultValue = "42")
    private String model;*/

    public void execute() throws MojoExecutionException, MojoFailureException {

       // loadProperties();
        // Access properties as needed
       /* String property1 = properties.getProperty("property1");
        String property2 = properties.getProperty("property2");*/
        getLog().info("Property 1: " + property1);
        getLog().info("Property 2: " + property2);
        getLog().info("model: " + Constants.MODEL);

        ResourceBundle rd
                = ResourceBundle.getBundle("application");

        String r= rd.getString("property1");
        getLog().info("r: " + r);

        String model = Constants.MODEL;

        List<Dependency> dependencies = project.getDependencies();

        long numDependencies = dependencies.stream()
                                           .filter(d -> (scope==null || scope.isEmpty()) || scope.equals(d.getScope()))
                                           .count();

        getLog().info("Number of dependencies: " + numDependencies);


        String command = "mvn surefire:test -DreportsDirectory=" + reportsDirectory;
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to run tests", e);
        }

        runJaCoCoAnalysis();
    }

    private void runJaCoCoAnalysis() throws MojoExecutionException {


        String jacocoCommand = "mvn jacoco:prepare-agent jacoco:report -Djacoco.destFile=" + jacocoReportsDirectory + "/jacoco.xml";


        getLog().info("jacoco running ********");
        File projectDirectory = new File("/Users/macbook/Downloads/maven-test");


        String[] commands = {"sh", "-c", jacocoCommand};

        try {
            Process process = Runtime.getRuntime().exec(jacocoCommand,null, projectDirectory);
            process.waitFor(); // Wait for the process to finish
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to run JaCoCo analysis", e);
        }
    }

    /*private void loadProperties() throws MojoExecutionException {
        properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to load properties file", e);
        }
    }*/

}