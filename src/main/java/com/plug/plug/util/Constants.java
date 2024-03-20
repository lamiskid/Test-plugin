package com.plug.plug.util;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public class Constants  {

    @Parameter(property = "prompt", defaultValue = "defaultValue")
    public static final String PROMPT= "Write a unit  test  coverage for  this  jacoco report";
    @Parameter(property = "model", defaultValue = "gpt-3.5-turbo")
    public static final String MODEL = "gpt-3.5-turbopp";

    @Parameter(property = "property1", defaultValue = "defaultValue")
    public static final String OPENAI_API_ENDPOINT= "https://api.openai.com/v1/chat/completions";


    public static final String  TEMPERATURE= "";



}
