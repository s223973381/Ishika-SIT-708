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
    private Button btnExplainAnswer, btnContinue;

    private ArrayList<String> questions, answers;
    private boolean[] correct = new boolean[3];
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        username  = getIntent().getStringExtra("username");
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
        btnExplainAnswer = findViewById(R.id.btnExplainAnswer);
        btnContinue      = findViewById(R.id.btnContinue);

        View card = findViewById(R.id.resultCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        populateResults();

        // LLM Utility 2 — Explain wrong answers
        btnExplainAnswer.setOnClickListener(v -> explainWrongAnswers());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.putExtra("username", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void explainWrongAnswers() {
        if (questions == null || answers == null) return;

        // Collect every question the student got wrong
        StringBuilder wrongItems = new StringBuilder();
        int wrongCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (!correct[i]) {
                wrongCount++;
                wrongItems.append(wrongCount)
                        .append(". Question: ").append(questions.get(i))
                        .append("\n   Student answered: ").append(answers.get(i))
                        .append("\n\n");
            }
        }

        if (wrongCount == 0) {
            // All correct — still show a congratulatory explanation
            showLoading("All answers were correct — explain why each answer is right.");
            String prompt = "The student answered all of these correctly:\n";
            for (int i = 0; i < questions.size(); i++) {
                prompt += (i + 1) + ". " + questions.get(i)
                        + " — Answer: " + answers.get(i) + "\n";
            }
            prompt += "\nBriefly confirm why each answer is correct in simple terms.";
            final String finalPrompt = prompt;
            LLMService.query(finalPrompt, new LLMService.LLMCallback() {
                @Override public void onSuccess(String r) { runOnUiThread(() -> showResponse(r)); }
                @Override public void onError(String e)   { runOnUiThread(() -> showError(e)); }
            });
        } else {
            // Build a specific prompt for each wrong answer
            String prompt = "The student got the following question(s) wrong. "
                    + "For each one, explain clearly why their answer is incorrect "
                    + "and what the correct answer should be, in simple student-friendly language:\n\n"
                    + wrongItems;
            showLoading("Explaining " + wrongCount + " wrong answer(s): " + wrongItems.toString().split("\n")[0]);
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
