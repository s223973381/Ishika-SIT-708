package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting, tvTaskDue, tvTaskTitle, tvTaskDesc;
    private TextView tvPrompt, tvResponse, tvLoading, tvError;
    private EditText etCustomPrompt;
    private Button btnGenerateHint, btnExplainAnswer, btnStartQuiz;

    private String username;
    private String currentTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        username = getIntent().getStringExtra("username");
        if (username == null) username = "Student";

        ArrayList<String> interests = getIntent().getStringArrayListExtra("interests");
        currentTopic = (interests != null && !interests.isEmpty())
                ? interests.get(0) : "Algorithms";

        tvGreeting       = findViewById(R.id.tvGreeting);
        tvTaskDue        = findViewById(R.id.tvTaskDue);
        tvTaskTitle      = findViewById(R.id.tvTaskTitle);
        tvTaskDesc       = findViewById(R.id.tvTaskDesc);
        tvPrompt         = findViewById(R.id.tvPrompt);
        tvResponse       = findViewById(R.id.tvResponse);
        tvLoading        = findViewById(R.id.tvLoading);
        tvError          = findViewById(R.id.tvError);
        etCustomPrompt   = findViewById(R.id.etCustomPrompt);
        btnGenerateHint  = findViewById(R.id.btnGenerateHint);
        btnExplainAnswer = findViewById(R.id.btnExplainAnswer);
        btnStartQuiz     = findViewById(R.id.btnStartQuiz);

        tvGreeting.setText("Hello,\n" + username);
        tvTaskDue.setText("You have 1 task due");
        tvTaskTitle.setText(currentTopic);
        tvTaskDesc.setText(QuizData.getDescription(currentTopic));

        View card = findViewById(R.id.homeCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        btnStartQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("topic", currentTopic);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // LLM Utility 1 — Generate Hint
        // Uses whatever the user typed; falls back to first question of current topic
        btnGenerateHint.setOnClickListener(v -> {
            String userText = etCustomPrompt.getText().toString().trim();
            String question = userText.isEmpty()
                    ? QuizData.getQuestions(currentTopic)[0].text
                    : userText;

            String promptDisplay = "Generate a hint for: " + question;
            showLoading(promptDisplay);

            LLMService.generateHint(question, new LLMService.LLMCallback() {
                @Override public void onSuccess(String r) { runOnUiThread(() -> showResponse(r)); }
                @Override public void onError(String e)   { runOnUiThread(() -> showError(e)); }
            });
        });

        // LLM Utility 2 — Explain Answer
        // Uses whatever the user typed; falls back to first question + correct answer
        btnExplainAnswer.setOnClickListener(v -> {
            String userText = etCustomPrompt.getText().toString().trim();
            String question;
            String answer;

            if (userText.isEmpty()) {
                QuizData.Question q = QuizData.getQuestions(currentTopic)[0];
                question = q.text;
                answer   = q.options[q.correctIndex];
            } else {
                question = userText;
                answer   = "the correct option";
            }

            String promptDisplay = "Explain the answer for: " + question;
            showLoading(promptDisplay);

            LLMService.explainAnswer(question, answer, new LLMService.LLMCallback() {
                @Override public void onSuccess(String r) { runOnUiThread(() -> showResponse(r)); }
                @Override public void onError(String e)   { runOnUiThread(() -> showError(e)); }
            });
        });
    }

    private void showLoading(String prompt) {
        tvPrompt.setVisibility(View.VISIBLE);
        tvPrompt.setText("Prompt: " + prompt);
        tvLoading.setVisibility(View.VISIBLE);
        tvResponse.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnGenerateHint.setEnabled(false);
        btnExplainAnswer.setEnabled(false);
    }

    private void showResponse(String response) {
        tvLoading.setVisibility(View.GONE);
        tvResponse.setVisibility(View.VISIBLE);
        tvResponse.setText(response);
        tvError.setVisibility(View.GONE);
        btnGenerateHint.setEnabled(true);
        btnExplainAnswer.setEnabled(true);
        tvResponse.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }

    private void showError(String error) {
        tvLoading.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(error);
        btnGenerateHint.setEnabled(true);
        btnExplainAnswer.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
