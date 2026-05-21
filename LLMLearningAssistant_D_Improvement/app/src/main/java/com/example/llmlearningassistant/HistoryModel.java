package com.example.llmlearningassistant;

public class HistoryModel {
    public final int id;
    public final String question, userAnswer, correctAnswer, topic, dateTime;
    public final boolean isCorrect;

    public HistoryModel(int id, String question, String userAnswer, String correctAnswer,
                        boolean isCorrect, String topic, String dateTime) {
        this.id = id;
        this.question = question;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.topic = topic;
        this.dateTime = dateTime;
    }
}
