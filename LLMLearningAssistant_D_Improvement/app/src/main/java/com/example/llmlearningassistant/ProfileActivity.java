package com.example.llmlearningassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvPlan, tvTotal, tvCorrect, tvIncorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("llm_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", getIntent().getStringExtra("username"));
        if (username == null || username.isEmpty()) username = "Student";
        String email = prefs.getString("email", "—");
        String plan = prefs.getString("plan", "Free");

        LinearLayout card = findViewById(R.id.profileCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        TextView tvAvatar = findViewById(R.id.tvAvatar);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        tvPlan = findViewById(R.id.tvProfilePlan);
        tvTotal = findViewById(R.id.tvTotalQuestions);
        tvCorrect = findViewById(R.id.tvCorrectAnswers);
        tvIncorrect = findViewById(R.id.tvIncorrectAnswers);

        tvAvatar.setText(username.substring(0, 1).toUpperCase());
        tvName.setText(username);
        tvEmail.setText(email);
        tvPlan.setText(plan + " Plan");
        refreshStats();

        final String finalUsername = username;

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnViewHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class)
                    .putExtra("username", finalUsername));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.btnShareProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ShareProfileActivity.class)
                    .putExtra("username", finalUsername));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.btnUpgrade).setOnClickListener(v -> {
            startActivity(new Intent(this, UpgradeActivity.class)
                    .putExtra("username", finalUsername));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("llm_prefs", MODE_PRIVATE);
        if (tvPlan != null) tvPlan.setText(prefs.getString("plan", "Free") + " Plan");
        refreshStats();
    }

    private void refreshStats() {
        DatabaseHelper db = new DatabaseHelper(this);
        UserStatsModel stats = db.getStats();
        tvTotal.setText(String.valueOf(stats.totalQuestions));
        tvCorrect.setText(String.valueOf(stats.correctAnswers));
        tvIncorrect.setText(String.valueOf(stats.incorrectAnswers));
    }
}
