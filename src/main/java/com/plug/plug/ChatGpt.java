package com.plug.plug;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plug.plug.model.ChatGPTRequest;
import com.plug.plug.model.ChatGptResponse;
import com.plug.plug.model.Messages;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static com.plug.plug.util.Constants.OPENAI_API_ENDPOINT;


public class ChatGpt {


    public static void writeTestClassUsingJacocoAndMML(String jacocoReportPath) throws URISyntaxException, IOException {

        String gptAnswers =  getClassesFromJacoco(jacocoReportPath);

        String[] classPaths = gptAnswers.split(";");



        for(String classes : classPaths){
            String  className =classes.substring(classes.lastIndexOf("/"));
            testPromptRequest("src/main/java/"+classes+".java",className);
        }

    }

    public static void writeAllTest() throws URISyntaxException, IOException {
        List<File> classesList=  FileExtractor.getAllFileInDirectories("src/main/java/");
        for(File file : classesList){
           // testPromptRequest(file.getAbsolutePath(), file.getName(),);
        }

    }

    public static void testPromptRequest(String location, String fileName) throws URISyntaxException, IOException {

        String classContent = FileExtractor.getTextFromFile(location);

        String prompt = "Write a unit  test  coverage for  this  jacoco report" +"\n" +classContent +"\n";


        Messages messages =new Messages("user",prompt);
        List<Messages> messagesList =new ArrayList<>();
        messagesList.add(messages);
        ChatGPTRequest gptRequest =new ChatGPTRequest("gpt-3.5-turbo",messagesList);


        ChatGptResponse chatGptResponse=makeOpenAIAPIRequest(gptRequest);

        String unitTestResponse= chatGptResponse.getChoices().get(0).getMessages().getContent();

        // Remove the markdown code block syntax if present
        unitTestResponse = unitTestResponse.replaceAll("```java\\s*(.*?)\\s*```", "").trim();
        unitTestResponse = unitTestResponse.replaceAll("```\\s*(.*?)\\s*```|`", "").trim();

        // Remove the first occurrence of "java" only if it appears at the beginning followed by a newline or space
        // This is to prevent accidentally removing "java" from important parts of the code
        unitTestResponse = unitTestResponse.replaceFirst("^java\\s+", "").trim();

        System.out.println("Unit Test for Class: " + fileName);
        System.out.println(unitTestResponse);
        System.out.println("-----------------------------------");

        writeToFile(fileName,unitTestResponse);

    }

    public static ChatGptResponse makeOpenAIAPIRequest(ChatGPTRequest gptRequest) {



        // Use Jackson ObjectMapper to convert the object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonBody = objectMapper.writeValueAsString(gptRequest);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(OPENAI_API_ENDPOINT);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "");
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




    private static void writeToFile(String fileName, String content) {

        File testClassesDirectory = new File("GPTGeneratedTestClasses");
        if (!testClassesDirectory.exists()) {
            testClassesDirectory.mkdir(); // Create the directory if it doesn't exist
        }
        String testClassFileName = fileName + "Test.java";
        File testClassFile = new File(testClassesDirectory, testClassFileName);

        try (FileWriter writer = new FileWriter(testClassFile)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + testClassFile.getAbsolutePath());
            e.printStackTrace();
        }
    }





    public static String getClassesFromJacoco(String reportPath) throws URISyntaxException {

       // String htmlContent = FileExtractor.getTextFromFile(location);

        // Sanitize HTML using JSoup
        //Document document = Jsoup.parse(htmlContent);
     //   String textContent = document.text();
        String extractReportContent = FileExtractor.getTextFromFile(reportPath);

        String prompt = "Given the following Jacoco report, list all the classes that need to be tested. Format the class names separated by semicolons without any extra text."
                + "\n" + extractReportContent + "\n";
        Messages messages =new Messages("user",prompt);
        List<Messages> messagesList =new ArrayList<>();
        messagesList.add(messages);
        ChatGPTRequest gptRequest =new ChatGPTRequest("gpt-3.5-turbo",messagesList);


        ChatGptResponse chatGptResponse=makeOpenAIAPIRequest(gptRequest);

        String content= chatGptResponse.getChoices().get(0).getMessages().getContent();

        return content;

      //  System.out.println(content);
      //  writeToFile(fileName,content);
    }



}
