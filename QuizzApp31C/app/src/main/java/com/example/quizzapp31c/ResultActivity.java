package com.example.quizzapp31c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class ResultActivity extends AppCompatActivity {

    TextView scoreText, commentText, resultTitle;
    Button newQuizButton, finishButton;
    LinearLayout rootLayout;
    CardView resultCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        rootLayout = findViewById(R.id.resultLayout);
        resultCard = findViewById(R.id.resultCard);
        scoreText = findViewById(R.id.scoreText);
        commentText = findViewById(R.id.commentText);
        resultTitle = findViewById(R.id.resultTitle);
        newQuizButton = findViewById(R.id.newQuizButton);
        finishButton = findViewById(R.id.finishButton);

        applyThemeColors(isDarkMode);

        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);
        String userName = getIntent().getStringExtra("USERNAME");
        String category = getIntent().getStringExtra("CATEGORY");

        if (resultTitle != null) resultTitle.setText("Well done, " + userName + "!");
        if (scoreText != null) scoreText.setText(score + " / " + total + " in " + category);

        String comment;
        if (score == total) comment = "Outstanding! Perfect score!";
        else if (score >= 5) comment = "Great job! You know this topic well.";
        else if (score >= 3) comment = "Nice effort! Keep practicing.";
        else comment = "Good try! Take another quiz and improve.";

        if (commentText != null) commentText.setText(comment);

        if (newQuizButton != null) {
            newQuizButton.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", userName);
                startActivity(intent);
                finish();
            });
        }

        if (finishButton != null) finishButton.setOnClickListener(v -> finishAffinity());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        applyThemeColors(isDark);
    }

    private void applyThemeColors(boolean isDark) {
        int bgColor = ContextCompat.getColor(this, isDark ? R.color.dark_background : R.color.off_white);
        int cardColor = ContextCompat.getColor(this, isDark ? R.color.dark_card_bg : R.color.white);
        if (rootLayout != null) rootLayout.setBackgroundColor(bgColor);
        if (resultCard != null) resultCard.setCardBackgroundColor(cardColor);
    }
}