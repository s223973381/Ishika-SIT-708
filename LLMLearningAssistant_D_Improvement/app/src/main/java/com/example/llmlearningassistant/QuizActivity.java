package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    private TextView tvTaskTitle, tvQ1, tvQ2, tvQ3, tvPrompt, tvResponse, tvLoading, tvError;
    private RadioGroup rgQ1, rgQ2, rgQ3;
    private EditText etCustomHint;
    private Button btnGenerateHint, btnSubmit;

    private String username;
    private String topic;
    private QuizData.Question[] questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        username = getIntent().getStringExtra("username");
        topic    = getIntent().getStringExtra("topic");
        if (topic == null) topic = "Algorithms";

        // Load questions for the topic passed from HomeActivity
        questions = QuizData.getQuestions(topic);

        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvQ1        = findViewById(R.id.tvQ1);
        tvQ2        = findViewById(R.id.tvQ2);
        tvQ3        = findViewById(R.id.tvQ3);
        rgQ1        = findViewById(R.id.rgQ1);
        rgQ2        = findViewById(R.id.rgQ2);
        rgQ3        = findViewById(R.id.rgQ3);
        tvPrompt      = findViewById(R.id.tvPrompt);
        tvResponse    = findViewById(R.id.tvResponse);
        tvLoading     = findViewById(R.id.tvLoading);
        tvError       = findViewById(R.id.tvError);
        etCustomHint  = findViewById(R.id.etCustomHint);
        btnGenerateHint = findViewById(R.id.btnGenerateHint);
        btnSubmit       = findViewById(R.id.btnSubmit);

        tvTaskTitle.setText(topic);

        populateQuestion(tvQ1, rgQ1, questions[0]);
        populateQuestion(tvQ2, rgQ2, questions[1]);
        populateQuestion(tvQ3, rgQ3, questions[2]);

        View card = findViewById(R.id.quizCard);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        card.startAnimation(fadeIn);

        // LLM Utility 1 — Generate Hint
        // Uses the user's typed question; falls back to Q1 text if the field is empty
        btnGenerateHint.setOnClickListener(v -> {
            String userText = etCustomHint.getText().toString().trim();
            String q = userText.isEmpty() ? questions[0].text : userText;
            showLoading("Give a hint for: " + q);

            LLMService.generateHint(q, new LLMService.LLMCallback() {
                @Override public void onSuccess(String response) { runOnUiThread(() -> showResponse(response)); }
                @Override public void onError(String error)      { runOnUiThread(() -> showError(error)); }
            });
        });

        btnSubmit.setOnClickListener(v -> submitQuiz());
    }

    private void populateQuestion(TextView tvQuestion, RadioGroup rg, QuizData.Question q) {
        tvQuestion.setText(q.text);
        rg.removeAllViews();
        for (String opt : q.options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(opt);
            rb.setTextColor(getResources().getColor(R.color.text_dark, null));
            rb.setTextSize(15f);
            rb.setPadding(4, 6, 4, 6);
            rg.addView(rb);
        }
    }

    private void submitQuiz() {
        int[] selected = {
            getCheckedIndex(rgQ1),
            getCheckedIndex(rgQ2),
            getCheckedIndex(rgQ3)
        };

        for (int i = 0; i < selected.length; i++) {
            if (selected[i] == -1) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Build data to pass to ResultActivity
        ArrayList<String> questionTexts = new ArrayList<>();
        ArrayList<String> answerTexts   = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            questionTexts.add(questions[i].text);
            answerTexts.add(questions[i].options[selected[i]]);
        }

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("topic", topic);
        intent.putStringArrayListExtra("questions", questionTexts);
        intent.putStringArrayListExtra("answers", answerTexts);
        intent.putExtra("correct_0", selected[0] == questions[0].correctIndex);
        intent.putExtra("correct_1", selected[1] == questions[1].correctIndex);
        intent.putExtra("correct_2", selected[2] == questions[2].correctIndex);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private int getCheckedIndex(RadioGroup rg) {
        int id = rg.getCheckedRadioButtonId();
        if (id == -1) return -1;
        return rg.indexOfChild(rg.findViewById(id));
    }

    private void showLoading(String prompt) {
        tvPrompt.setVisibility(View.VISIBLE);
        tvPrompt.setText("Prompt: " + prompt);
        tvLoading.setVisibility(View.VISIBLE);
        tvResponse.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnGenerateHint.setEnabled(false);
    }

    private void showResponse(String response) {
        tvLoading.setVisibility(View.GONE);
        tvResponse.setVisibility(View.VISIBLE);
        tvResponse.setText(response);
        tvError.setVisibility(View.GONE);
        btnGenerateHint.setEnabled(true);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvResponse.startAnimation(fadeIn);
    }

    private void showError(String error) {
        tvLoading.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(error);
        btnGenerateHint.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
