package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class InterestActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private Button btnNext;
    private String username;

    private final String[] topics = {
        "Algorithms", "Data Structures", "Web Development", "Testing",
        "Machine Learning", "Databases", "Networking", "Operating Systems",
        "Software Engineering", "Mobile Development", "Cybersecurity", "Cloud Computing"
    };

    private final List<String> selectedTopics = new ArrayList<>();
    private static final int MAX_SELECTION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);

        username = getIntent().getStringExtra("username");
        chipGroup = findViewById(R.id.chipGroup);
        btnNext = findViewById(R.id.btnNext);

        View card = findViewById(R.id.interestCard);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        card.startAnimation(fadeIn);

        buildChips();

        btnNext.setOnClickListener(v -> {
            if (selectedTopics.isEmpty()) {
                Toast.makeText(this, "Please select at least one topic", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(InterestActivity.this, HomeActivity.class);
            intent.putExtra("username", username);
            intent.putStringArrayListExtra("interests", new ArrayList<>(selectedTopics));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void buildChips() {
        for (String topic : topics) {
            Chip chip = new Chip(this);
            chip.setText(topic);
            chip.setCheckable(true);
            chip.setChecked(false);
            chip.setChipBackgroundColorResource(R.color.chip_unselected);
            chip.setTextColor(getResources().getColor(R.color.text_dark, null));
            chip.setTextSize(14f);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedTopics.size() >= MAX_SELECTION) {
                        chip.setChecked(false);
                        Toast.makeText(this, "Max 10 topics allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedTopics.add(topic);
                    chip.setChipBackgroundColorResource(R.color.chip_selected);
                } else {
                    selectedTopics.remove(topic);
                    chip.setChipBackgroundColorResource(R.color.chip_unselected);
                }
            });

            chipGroup.addView(chip);
        }
    }
}
