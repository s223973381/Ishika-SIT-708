package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private LinearLayout llResults;
    private TextView tvPrompt, tvResponse, tvLoading, tvError;
    private TextView tvScore, tvScoreMessage;
    private Button btnExplainAnswer, btnContinue, btnViewProfile;

    private ArrayList<String> questions, answers;
    private boolean[] correct = new boolean[3];
    private String username, topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        username  = getIntent().getStringExtra("username");
        topic     = getIntent().getStringExtra("topic");
        if (topic == null) topic = "Algorithms";
        questions = getIntent().getStringArrayListExtra("questions");
        answers   = getIntent().getStringArrayListExtra("answers");
        correct[0] = getIntent().getBooleanExtra("correct_0", false);
        correct[1] = getIntent().getBooleanExtra("correct_1", false);
        correct[2] = getIntent().getBooleanExtra("correct_2", false);

        llResults        = findViewById(R.id.llResults);
        tvPrompt         = findViewById(R.id.tvPrompt);
        tvResponse       = findViewById(R.id.tvResponse);
        tvLoading        = findViewById(R.id.tvLoading);
        tvError          = findViewById(R.id.tvError);
        tvScore          = findViewById(R.id.tvScore);
        tvScoreMessage   = findViewById(R.id.tvScoreMessage);
        btnExplainAnswer = findViewById(R.id.btnExplainAnswer);
        btnContinue      = findViewById(R.id.btnContinue);
        btnViewProfile   = findViewById(R.id.btnViewProfile);

        View card = findViewById(R.id.resultCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        populateResults();
        showScore();
        saveHistoryToDatabase();

        // LLM Utility 2 — Explain wrong answers
        btnExplainAnswer.setOnClickListener(v -> explainWrongAnswers());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, InterestActivity.class);
            intent.putExtra("username", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void showScore() {
        int total = correct.length;
        int score = 0;
        for (boolean b : correct) if (b) score++;

        tvScore.setText(score + " / " + total);

        String message;
        if (score == total) {
            message = "Perfect score! Outstanding! 🌟";
        } else if (score >= total / 2.0) {
            message = "Great job! Keep it up! 💪";
        } else if (score == 1) {
            message = "Good effort! Keep practising! 📚";
        } else {
            message = "Don't give up! Review and try again! 😊";
        }
        tvScoreMessage.setText(message);
    }

    private void saveHistoryToDatabase() {
        if (questions == null || answers == null) return;
        DatabaseHelper db = new DatabaseHelper(this);
        QuizData.Question[] quizQuestions = QuizData.getQuestions(topic);
        for (int i = 0; i < questions.size(); i++) {
            String correctAnswer = quizQuestions[i].options[quizQuestions[i].correctIndex];
            db.insertHistory(questions.get(i), answers.get(i), correctAnswer, correct[i], topic);
        }
    }

    private void explainWrongAnswers() {
        if (questions == null || answers == null) return;

        QuizData.Question[] quizQuestions = QuizData.getQuestions(topic);

        StringBuilder wrongItems = new StringBuilder();
        StringBuilder allItems   = new StringBuilder();
        int wrongCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            String correctAnswer = quizQuestions[i].options[quizQuestions[i].correctIndex];
            if (!correct[i]) {
                wrongCount++;
                wrongItems.append(wrongCount)
                        .append(". Question: ").append(questions.get(i))
                        .append("\n   Student answered: ").append(answers.get(i))
                        .append("\n   Correct answer: ").append(correctAnswer)
                        .append("\n\n");
            }
            allItems.append(i + 1)
                    .append(". ").append(questions.get(i))
                    .append("\n   Answer: ").append(correctAnswer)
                    .append("\n\n");
        }

        if (wrongCount == 0) {
            showLoading("All correct — explaining why each answer is right.");
            String prompt = "The student got all answers correct. "
                    + "Briefly explain why each answer is right in simple terms:\n\n"
                    + allItems;
            final String finalPrompt = prompt;
            LLMService.query(finalPrompt, new LLMService.LLMCallback() {
                @Override public void onSuccess(String r) { runOnUiThread(() -> showResponse(r)); }
                @Override public void onError(String e)   { runOnUiThread(() -> showError(e)); }
            });
        } else {
            showLoading("Explaining " + wrongCount + " wrong answer(s)...");
            String prompt = "The student got the following question(s) wrong. "
                    + "For each one, explain in simple student-friendly language "
                    + "why their answer is incorrect and why the correct answer is right:\n\n"
                    + wrongItems;
            LLMService.query(prompt, new LLMService.LLMCallback() {
                @Override public void onSuccess(String r) { runOnUiThread(() -> showResponse(r)); }
                @Override public void onError(String e)   { runOnUiThread(() -> showError(e)); }
            });
        }
    }

    private void populateResults() {
        if (questions == null || answers == null) return;
        llResults.removeAllViews();

        for (int i = 0; i < questions.size(); i++) {
            TextView tvQuestion = new TextView(this);
            tvQuestion.setText((i + 1) + ". " + questions.get(i));
            tvQuestion.setTextColor(getResources().getColor(R.color.text_dark, null));
            tvQuestion.setTextSize(15f);
            tvQuestion.setPadding(0, 12, 0, 4);

            TextView tvAnswer = new TextView(this);
            String status = correct[i] ? "✓ Correct" : "✗ Incorrect";
            int color = correct[i]
                    ? getResources().getColor(R.color.mint_dark, null)
                    : getResources().getColor(R.color.error_red, null);
            tvAnswer.setText("Your answer: " + answers.get(i) + "  —  " + status);
            tvAnswer.setTextColor(color);
            tvAnswer.setTextSize(14f);
            tvAnswer.setPadding(0, 0, 0, 8);

            llResults.addView(tvQuestion);
            llResults.addView(tvAnswer);
        }
    }

    private void showLoading(String prompt) {
        tvPrompt.setVisibility(View.VISIBLE);
        tvPrompt.setText("Prompt: " + prompt);
        tvLoading.setVisibility(View.VISIBLE);
        tvResponse.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnExplainAnswer.setEnabled(false);
    }

    private void showResponse(String response) {
        tvLoading.setVisibility(View.GONE);
        tvResponse.setVisibility(View.VISIBLE);
        tvResponse.setText(response);
        tvError.setVisibility(View.GONE);
        btnExplainAnswer.setEnabled(true);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvResponse.startAnimation(fadeIn);
    }

    private void showError(String error) {
        tvLoading.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(error);
        tvResponse.setVisibility(View.GONE);
        btnExplainAnswer.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
