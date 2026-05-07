package com.example.llmchatbot.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LLMRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("stream")
    private boolean stream;

    public LLMRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
        this.stream = false;
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
