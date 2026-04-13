package com.example.quizzapp31c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class CategoryActivity extends AppCompatActivity {

    CardView scienceCard, geographyCard, literatureCard;
    ScrollView rootLayout;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        userName = getIntent().getStringExtra("USERNAME");
        rootLayout = findViewById(R.id.categoryMain);
        scienceCard = findViewById(R.id.scienceCard);
        geographyCard = findViewById(R.id.geographyCard);
        literatureCard = findViewById(R.id.literatureCard);

        applyThemeColors(isDarkMode);

        if (scienceCard != null) scienceCard.setOnClickListener(v -> openQuiz("Science"));
        if (geographyCard != null) geographyCard.setOnClickListener(v -> openQuiz("Geography"));
        if (literatureCard != null) literatureCard.setOnClickListener(v -> openQuiz("Literature"));
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
        if (scienceCard != null) scienceCard.setCardBackgroundColor(cardColor);
        if (geographyCard != null) geographyCard.setCardBackgroundColor(cardColor);
        if (literatureCard != null) literatureCard.setCardBackgroundColor(cardColor);
    }

    private void openQuiz(String category) {
        Intent intent = new Intent(CategoryActivity.this, QuizActivity.class);
        intent.putExtra("USERNAME", userName);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }
}