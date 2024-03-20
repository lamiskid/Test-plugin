package com.plug.plug;

import java.io.*;
import java.net.URISyntaxException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class FileExtractor {
    public static String getTextFromFile(String fileLocation) throws URISyntaxException {
        //File sourceDirectory = new File("");
        //"target/site/jacoco/com.plug.plug/Examples.java.html"
        Path targetPath =
                Paths.get(fileLocation).toAbsolutePath();
        System.out.println(targetPath.toString());
        StringBuilder classText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(targetPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {

                classText.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classText.toString();
    }





    public static List<File> getAllFileInDirectories(String filePath) {

        Path targetPath =
                Paths.get(filePath).toAbsolutePath();
        System.out.println(targetPath.toString());

        List<File> files = new ArrayList<>();


        File file = new File(targetPath.toAbsolutePath().toUri());
        Stack<File> s = new Stack<>();
        s.push(file);
        // initially stack is not empty
        System.out.println("Content of Directory " + targetPath
                + " is");
        while (!s.empty()) {
            File tmpF = s.pop();
            // check if it is a file or not
            if (tmpF.isFile()) {

                files.add(tmpF);

                System.out.println(tmpF.getName());
            } else if (tmpF.isDirectory()) {

                File[] f = tmpF.listFiles();
                for (File fpp : f) {
                    s.push(fpp);
                }
            }
        }
        return files;
    }
}
