package com.example.llmchatbot.network;

import com.google.gson.annotations.SerializedName;

public class LLMResponse {

    @SerializedName("message")
    private Message message;

    @SerializedName("error")
    private String error;

    public Message getMessage() { return message; }
    public String getError() { return error; }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}
