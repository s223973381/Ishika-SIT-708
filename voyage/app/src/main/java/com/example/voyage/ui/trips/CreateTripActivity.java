package com.example.voyage.ui.trips;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voyage.R;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.TripViewModel;

import java.util.Calendar;

public class CreateTripActivity extends AppCompatActivity {

    private EditText etDestination, etTitle, etBudget;
    private TextView tvStartDate, tvEndDate, tvDaysCount;
    private TextView styleRelax, styleAdventure, styleCulture, styleBudget;
    private TextView aiModeAuto, aiModeOffline, aiModeOnline;

    private String selectedStyle = "Relax";
    private String selectedAiMode = "auto";
    private String startDate = null;
    private String endDate = null;

    private TripViewModel tripViewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        session = new SessionManager(this);

        etDestination = findViewById(R.id.etDestination);
        etTitle = findViewById(R.id.etTitle);
        etBudget = findViewById(R.id.etBudget);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvDaysCount = findViewById(R.id.tvDaysCount);

        styleRelax = findViewById(R.id.styleRelax);
        styleAdventure = findViewById(R.id.styleAdventure);
        styleCulture = findViewById(R.id.styleCulture);
        styleBudget = findViewById(R.id.styleBudget);

        aiModeAuto = findViewById(R.id.aiModeAuto);
        aiModeOffline = findViewById(R.id.aiModeOffline);
        aiModeOnline = findViewById(R.id.aiModeOnline);

        TextView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnStartDate = findViewById(R.id.btnStartDate);
        LinearLayout btnEndDate = findViewById(R.id.btnEndDate);
        TextView btnCreateTrip = findViewById(R.id.btnCreateTrip);

        btnBack.setOnClickListener(v -> finish());

        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));

        initStyleFromProfile();
        styleRelax.setOnClickListener(v -> setStyleChip(styleRelax, "Relax"));
        styleAdventure.setOnClickListener(v -> setStyleChip(styleAdventure, "Adventure"));
        styleCulture.setOnClickListener(v -> setStyleChip(styleCulture, "Culture"));
        styleBudget.setOnClickListener(v -> setStyleChip(styleBudget, "Budget"));

        initAiModeFromProfile();
        aiModeAuto.setOnClickListener(v -> setAiModeChip(aiModeAuto, "auto"));
        aiModeOffline.setOnClickListener(v -> setAiModeChip(aiModeOffline, "offline"));
        aiModeOnline.setOnClickListener(v -> setAiModeChip(aiModeOnline, "online"));

        btnCreateTrip.setOnClickListener(v -> createTrip());
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, day);
            String display = String.format("%s %02d, %04d",
                    monthName(month), day, year);
            if (isStart) {
                startDate = date;
                tvStartDate.setText(display);
            } else {
                endDate = date;
                tvEndDate.setText(display);
            }
            updateDaysCount();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDaysCount() {
        if (startDate == null || endDate == null) return;
        try {
            String[] s = startDate.split("-");
            String[] e = endDate.split("-");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.set(Integer.parseInt(s[0]), Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]));
            end.set(Integer.parseInt(e[0]), Integer.parseInt(e[1]) - 1, Integer.parseInt(e[2]));
            long diff = end.getTimeInMillis() - start.getTimeInMillis();
            int days = (int) (diff / (1000 * 60 * 60 * 24));
            if (days >= 0) {
                tvDaysCount.setText((days + 1) + " days");
            } else {
                tvDaysCount.setText("⚠️ End date must be after start");
            }
        } catch (Exception ignored) {}
    }

    private int calcDays() {
        if (startDate == null || endDate == null) return 1;
        try {
            String[] s = startDate.split("-");
            String[] e = endDate.split("-");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.set(Integer.parseInt(s[0]), Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]));
            end.set(Integer.parseInt(e[0]), Integer.parseInt(e[1]) - 1, Integer.parseInt(e[2]));
            long diff = end.getTimeInMillis() - start.getTimeInMillis();
            return Math.max(1, (int) (diff / (1000 * 60 * 60 * 24)) + 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private void initStyleFromProfile() {
        String styleStr = session.getTravelStyle();
        if (!styleStr.isEmpty()) {
            for (String s : styleStr.split(",")) {
                switch (s.trim()) {
                    case "Adventure": setStyleChip(styleAdventure, "Adventure"); return;
                    case "Culture":   setStyleChip(styleCulture, "Culture");     return;
                    case "Budget":    setStyleChip(styleBudget, "Budget");       return;
                }
            }
        }
        setStyleChip(styleRelax, "Relax");
    }

    private void initAiModeFromProfile() {
        String mode = session.getAiMode();
        if ("offline".equals(mode))     setAiModeChip(aiModeOffline, "offline");
        else if ("online".equals(mode)) setAiModeChip(aiModeOnline, "online");
        else                            setAiModeChip(aiModeAuto, "auto");
    }

    private void setStyleChip(TextView selected, String style) {
        selectedStyle = style;
        TextView[] chips = {styleRelax, styleAdventure, styleCulture, styleBudget};
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(0xFFFFFFFF);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip);
                chip.setTextColor(getColor(R.color.voyage_text_primary));
            }
        }
    }

    private void setAiModeChip(TextView selected, String mode) {
        selectedAiMode = mode;
        TextView[] chips = {aiModeAuto, aiModeOffline, aiModeOnline};
        for (TextView chip : chips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(0xFFFFFFFF);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip);
                chip.setTextColor(getColor(R.color.voyage_text_primary));
            }
        }
    }

    private void createTrip() {
        String destination = etDestination.getText().toString().trim();
        if (destination.isEmpty()) {
            etDestination.setError("Destination is required");
            etDestination.requestFocus();
            return;
        }

        double budget = 0;
        String budgetStr = etBudget.getText().toString().trim();
        if (!budgetStr.isEmpty()) {
            try { budget = Double.parseDouble(budgetStr); } catch (NumberFormatException ignored) {}
        }

        Trip trip = new Trip();
        trip.userId = session.getUserId();
        trip.destination = destination;
        trip.title = etTitle.getText().toString().trim();
        trip.startDate = startDate != null ? startDate : "";
        trip.endDate = endDate != null ? endDate : "";
        trip.days = calcDays();
        trip.budget = budget;
        trip.travelStyle = selectedStyle;
        trip.aiMode = selectedAiMode;
        trip.isCompleted = false;
        trip.isOfflineSaved = false;
        trip.createdAt = System.currentTimeMillis();

        tripViewModel.insertTrip(trip, tripId -> runOnUiThread(() -> {
            Intent intent = new Intent(this, TripDetailActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
            finish();
        }));
    }

    private String monthName(int month) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return months[month];
    }
}
