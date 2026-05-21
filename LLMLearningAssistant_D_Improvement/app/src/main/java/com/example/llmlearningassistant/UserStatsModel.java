package com.example.llmlearningassistant;

public class UserStatsModel {
    public final int totalQuestions, correctAnswers, incorrectAnswers;

    public UserStatsModel(int total, int correct) {
        this.totalQuestions = total;
        this.correctAnswers = correct;
        this.incorrectAnswers = total - correct;
    }
}
