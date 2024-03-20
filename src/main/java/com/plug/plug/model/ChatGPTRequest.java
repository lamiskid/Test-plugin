package com.plug.plug.model;

import java.util.List;


public class ChatGPTRequest {

    public String model;

    public List<Messages> messages;


    public ChatGPTRequest(String model, List<Messages> messages) {
        this.model = model;
        this.messages = messages;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Messages> getMessages() {
        return messages;
    }

    public void setMessages(List<Messages> messages) {
        this.messages = messages;
    }
}