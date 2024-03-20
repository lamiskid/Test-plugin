/*
package com.plug.plug.util;

import com.plug.plug.FileExtractor;
import com.plug.plug.model.ChatGPTRequest;
import com.plug.plug.model.ChatGptResponse;
import com.plug.plug.model.Messages;
import org.apache.maven.plugin.MojoExecutionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ToBeUsed {
   */
/* public static String getClassesTobeTested() throws URISyntaxException {

        String extractReportContent = FileExtractor.getTextFromFile("target/site/jacoco/jacoco.xml");

        String prompt = "list  all the source file in the jacoco coverage with no additional word added and bullet or numbers"
                +"\n" +extractReportContent +"\n";

        Messages messages =new Messages("user",prompt);
        List<Messages> messagesList =new ArrayList<>();
        messagesList.add(messages);
        ChatGPTRequest gptRequest =new ChatGPTRequest("gpt-3.5-turbo",messagesList);


        ChatGptResponse chatGptResponse=makeOpenAIAPIRequest(gptRequest);
        String content= chatGptResponse.getChoices().get(0).getMessages().getContent();

        return content;
    }



    public static void writeTest() throws URISyntaxException, IOException {

        String allClasses =  getClassesTobeTested();


        List<String> classesList = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(allClasses));
        String line;

        // Read each line from the reader
        while ((line = reader.readLine()) != null) {
            classesList.add(line);
        }

        // Close the reader
        reader.close();


        for(String classes : classesList){
            testPromptRequest("target/site/jacoco/com.plug.plug/"+classes+".html",classes);
        }

    }

    public static void testPromptRequest(String location, String fileName) throws URISyntaxException, IOException {

        String htmlContent = FileExtractor.getTextFromFile(location);

        // Sanitize HTML using JSoup
        Document document = Jsoup.parse(htmlContent);
        String textContent = document.text();

        String prompt = "Write a unit  test  coverage for  this  jacoco report" +"\n" +textContent +"\n";


        Messages messages =new Messages("user",prompt);
        List<Messages> messagesList =new ArrayList<>();
        messagesList.add(messages);
        ChatGPTRequest gptRequest =new ChatGPTRequest("gpt-3.5-turbo",messagesList);


        ChatGptResponse chatGptResponse=makeOpenAIAPIRequest(gptRequest);

        String content= chatGptResponse.getChoices().get(0).getMessages().getContent();

        System.out.println(content);
        writeToFile(fileName,content);

    }*//*


    private void runJaCoCoAnalysis() throws MojoExecutionException {



        String jacocoCommand = "mvn clean install jacoco:prepare-agent jacoco:report";
        String[] commands = {"sh", "-c", jacocoCommand};

        try {
            Process process = Runtime.getRuntime().exec(commands);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new MojoExecutionException("JaCoCo analysis failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to run JaCoCo analysis", e);
        }





    }


    @Mojo(name = "generate-tests", defaultPhase = LifecyclePhase.VERIFY)
   public class ChatGpt extends AbstractMojo {

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
               String classesToBeTested = this.processJacocoReportForTestClassNames(this.jacocoReportPath);
               this.generateTestClasses(classesToBeTested);
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

       public String processJacocoReportForTestClassNames(String jacocoReportPath) throws URISyntaxException, IOException {

           String jacocoReportContent = getTextFromFile(jacocoReportPath);

           String prompt = "Give me the full name of the classes which have to be tested as they are not covered enough from this jacoco report. Give out only the class names with the full path (For example path/to/class/) without any extra text and split them with a semicolon. Don't add any whitespace after or before the semicolon:\n"
                   + jacocoReportContent;

           Messages messages = new Messages("user", prompt);
           List<Messages> messagesList = new ArrayList<>();
           messagesList.add(messages);
           ChatGPTRequest gptRequest = new ChatGPTRequest("gpt-3.5-turbo", messagesList);
           ChatGptResponse chatGptResponse = makeOpenAIAPIRequest(gptRequest, this.apikey, this.apiendpoint);

           String classNames = chatGptResponse.getChoices().get(0).getMessages().getContent();

           System.out.println("\nGPT Answer: Class names to be tested: " + classNames);

           return classNames;
       }

       public void generateTestClasses(String classesToBeTested) throws URISyntaxException, IOException {

           String[] classPaths = classesToBeTested.split(";");

           // Define the directory where the test classes will be saved
           File testClassesDirectory = new File("GPTGeneratedTestClasses");

           cleanUpDirectory(testClassesDirectory);

           if (!testClassesDirectory.exists()) {
               testClassesDirectory.mkdir(); // Create the directory if it doesn't exist
           }

           // Print each class path on its own line
           System.out.println("\nClass paths to be processed:");
           for (String classPath : classPaths) {
               System.out.println(classPath.trim());
           }

           for (String classPath : classPaths) {
               classPath = classPath.trim(); // Trim the classPath to remove any leading or trailing spaces
               String classFilePath = "src/main/java/" + classPath + ".java";
               System.out.println("\nClassFilePath:\n" + classFilePath);
               String classContent = getTextFromFile(classFilePath);
               String className = new File(classPath).getName();
               String prompt = "Design a comprehensive set of unit tests for the given class to achieve higher test coverage. Provide a complete implementation of the test class, including the needed imports, that can be directly executed. Additionally, include a JavaDoc for the test class. Dont add any Placeholders. Your Answer must not include any other text, just the test class:\n"
                       + classContent;

               System.out.println("\nPROMPT:\n");
               System.out.println(prompt);

               Messages messages = new Messages("user", prompt);
               List<Messages> messagesList = new ArrayList<>();
               messagesList.add(messages);
               ChatGPTRequest gptRequest = new ChatGPTRequest("gpt-3.5-turbo", messagesList);
               ChatGptResponse chatGptResponse = makeOpenAIAPIRequest(gptRequest, this.apikey, this.apiendpoint);

               String unitTestResponse = chatGptResponse.getChoices().get(0).getMessages().getContent();

               // Remove the markdown code block syntax if present
               unitTestResponse = unitTestResponse.replaceAll("```java\\s*(.*?)\\s*```", "").trim();
               unitTestResponse = unitTestResponse.replaceAll("```\\s*(.*?)\\s*```|`", "").trim();

               // Remove the first occurrence of "java" only if it appears at the beginning followed by a newline or space
               // This is to prevent accidentally removing "java" from important parts of the code
               unitTestResponse = unitTestResponse.replaceFirst("^java\\s+", "").trim();

               System.out.println("\nRESULT:\n");
               System.out.println(unitTestResponse);
               System.out.println("-----------------------------------");

               // Define the test class file name and path
               String testClassFileName = className + "Test.java";
               File testClassFile = new File(testClassesDirectory, testClassFileName);

               // Write the unit test class content to the file
               boolean isSaved = writeToFile(testClassFileName, unitTestResponse);
               if (isSaved) {
                   System.out.println("Test class saved: " + testClassFile.getAbsolutePath());
               }
           }

       }

       public ChatGptResponse makeOpenAIAPIRequest(ChatGPTRequest gptRequest, String apiKey, String apiEndpoint) {
           // Use Jackson ObjectMapper to convert the object to JSON
           ObjectMapper objectMapper = new ObjectMapper();
           try {
               String jsonBody = objectMapper.writeValueAsString(gptRequest);

               CloseableHttpClient httpClient = HttpClients.createDefault();
               HttpPost httpPost = new HttpPost(apiEndpoint);

               httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
               httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

               httpPost.setEntity(new StringEntity(jsonBody));

               HttpResponse httpResponse = httpClient.execute(httpPost);
               HttpEntity httpEntity = httpResponse.getEntity();

               if (httpEntity != null) {
                   return objectMapper.readValue(EntityUtils.toString(httpEntity), ChatGptResponse.class);
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
           return null;
       }

       public static String getTextFromFile(String fileLocation) throws IOException {
           File file = new File(fileLocation);
           if (!file.exists()) {
               throw new IOException("File not found: " + fileLocation);
           }
           StringBuilder classText = new StringBuilder();
           try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
               String line;
               while ((line = reader.readLine()) != null) {
                   classText.append(line).append("\n");
               }
           }
           return classText.toString();
       }

       private static boolean writeToFile(String fileName, String content) {

           File testClassesDirectory = new File("GPTGeneratedTestClasses");
           if (!testClassesDirectory.exists()) {
               testClassesDirectory.mkdir(); // Create the directory if it doesn't exist
           }
           String testClassFileName = fileName;
           File testClassFile = new File(testClassesDirectory, testClassFileName);

           try (FileWriter writer = new FileWriter(testClassFile)) {
               writer.write(content);
               return true; // File is saved
           } catch (IOException e) {
               System.err.println("Error writing to file: " + testClassFile.getAbsolutePath());
               e.printStackTrace();
               return false; // File is not Saved
           }
       }

       private static void cleanUpDirectory(File directory) {
           if (directory.exists() && directory.isDirectory()) {
               File[] files = directory.listFiles();
               if (files != null) {
                   for (File file : files) {
                       file.delete();
                   }
               }
           }
       }

   }


}
*/
