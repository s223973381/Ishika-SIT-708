package com.example.quizzapp31c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    EditText nameInput;
    Button startButton;
    Switch themeSwitch;
    LinearLayout rootLayout;
    CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.main);
        cardView = findViewById(R.id.mainCard);
        nameInput = findViewById(R.id.nameInput);
        startButton = findViewById(R.id.startButton);
        themeSwitch = findViewById(R.id.themeSwitch);

        applyThemeColors(isDarkMode);

        if (themeSwitch != null) {
            themeSwitch.setChecked(isDarkMode);
            themeSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("dark_mode", checked);
                editor.apply();
                AppCompatDelegate.setDefaultNightMode(checked ?
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                // configChanges="uiMode" means onConfigurationChanged fires instead of recreate
            });
        }

        String returnedName = getIntent().getStringExtra("USERNAME");
        if (returnedName != null && nameInput != null) {
            nameInput.setText(returnedName);
        }

        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                String name = nameInput != null ? nameInput.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                    intent.putExtra("USERNAME", name);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        applyThemeColors(isDark);
        if (themeSwitch != null) themeSwitch.setChecked(isDark);
    }

    private void applyThemeColors(boolean isDark) {
        int bgColor = ContextCompat.getColor(this, isDark ? R.color.dark_background : R.color.off_white);
        int cardColor = ContextCompat.getColor(this, isDark ? R.color.dark_card_bg : R.color.white);
        if (rootLayout != null) rootLayout.setBackgroundColor(bgColor);
        if (cardView != null) cardView.setCardBackgroundColor(cardColor);
    }
}