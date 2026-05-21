package com.example.llmlearningassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        View header = findViewById(R.id.historyHeader);
        header.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        LinearLayout container = findViewById(R.id.historyContainer);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        DatabaseHelper db = new DatabaseHelper(this);
        List<HistoryModel> history = db.getAllHistory();

        if (history.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            for (HistoryModel item : history) {
                View card = LayoutInflater.from(this)
                        .inflate(R.layout.item_history, container, false);

                ((TextView) card.findViewById(R.id.tvHistoryTopic)).setText(item.topic);
                ((TextView) card.findViewById(R.id.tvHistoryDate)).setText(item.dateTime);
                ((TextView) card.findViewById(R.id.tvHistoryQuestion)).setText(item.question);
                ((TextView) card.findViewById(R.id.tvHistoryUserAnswer))
                        .setText("Your answer: " + item.userAnswer);
                ((TextView) card.findViewById(R.id.tvHistoryCorrectAnswer))
                        .setText("Correct: " + item.correctAnswer);

                TextView tvStatus = card.findViewById(R.id.tvHistoryStatus);
                if (item.isCorrect) {
                    tvStatus.setText("Correct");
                    tvStatus.setTextColor(getResources().getColor(R.color.mint_dark, null));
                } else {
                    tvStatus.setText("Incorrect");
                    tvStatus.setTextColor(getResources().getColor(R.color.error_red, null));
                }

                container.addView(card);
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }
}
