package com.example.voyage.ai;

public class OpenAiRequest {
    public String model;
    public String instructions;
    public String input;

    public OpenAiRequest(String model, String instructions, String input) {
        this.model = model;
        this.instructions = instructions;
        this.input = input;
    }
}
