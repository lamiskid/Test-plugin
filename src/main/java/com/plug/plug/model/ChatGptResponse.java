package com.plug.plug.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatGptResponse implements Serializable{
    public ChatGptResponse() {
    }

    public ChatGptResponse(List<Choice> choices) {
        this.choices = choices;
    }

    @JsonProperty("choices")
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    // Getter and Setter for 'choices'


    @JsonIgnoreProperties(ignoreUnknown = true)

    public static class Choice implements Serializable {

        public Choice() {
        }

        public Choice(Messages message) {
            this.message = message;
        }

        @JsonProperty(value = "message")
        public   Messages message;

        public Messages getMessages() {
            return message;
        }

        public void setMessages(Messages message) {
            this.message = message;
        }
    }
}
