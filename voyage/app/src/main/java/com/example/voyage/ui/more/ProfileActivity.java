package com.example.voyage.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voyage.R;
import com.example.voyage.ui.auth.LoginActivity;
import com.example.voyage.util.SessionManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private EditText etName;
    private TextView tvAvatarInitial, tvProfileName;

    private String selectedAiMode;
    private Set<String> selectedStyles = new HashSet<>();

    private TextView chipAiAuto, chipAiOffline, chipAiOnline;
    private TextView chipBudget, chipNature, chipAdventure, chipCulture, chipLuxury, chipFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        tvAvatarInitial = findViewById(R.id.tvAvatarInitial);
        tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        etName = findViewById(R.id.etName);

        // Load current data
        String name = session.getUserName();
        String email = session.getUserEmail();
        selectedAiMode = session.getAiMode();

        String styleStr = session.getTravelStyle();
        if (!styleStr.isEmpty()) {
            selectedStyles.addAll(Arrays.asList(styleStr.split(",")));
        }

        // Bind basic profile
        tvProfileName.setText(name);
        tvAvatarInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
        tvEmail.setText(email.isEmpty() ? "Guest account" : email);
        etName.setText(name);

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String n = s.toString().trim();
                tvProfileName.setText(n.isEmpty() ? "Traveller" : n);
                tvAvatarInitial.setText(n.isEmpty() ? "T" : String.valueOf(n.charAt(0)).toUpperCase());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // AI Mode chips
        chipAiAuto = findViewById(R.id.chipAiAuto);
        chipAiOffline = findViewById(R.id.chipAiOffline);
        chipAiOnline = findViewById(R.id.chipAiOnline);

        refreshAiChips();
        chipAiAuto.setOnClickListener(v -> selectAiMode("auto"));
        chipAiOffline.setOnClickListener(v -> selectAiMode("offline"));
        chipAiOnline.setOnClickListener(v -> selectAiMode("online"));

        // Travel style chips
        chipBudget = findViewById(R.id.chipBudget);
        chipNature = findViewById(R.id.chipNature);
        chipAdventure = findViewById(R.id.chipAdventure);
        chipCulture = findViewById(R.id.chipCulture);
        chipLuxury = findViewById(R.id.chipLuxury);
        chipFood = findViewById(R.id.chipFood);

        refreshStyleChips();
        chipBudget.setOnClickListener(v -> toggleStyle("Budget", chipBudget));
        chipNature.setOnClickListener(v -> toggleStyle("Nature", chipNature));
        chipAdventure.setOnClickListener(v -> toggleStyle("Adventure", chipAdventure));
        chipCulture.setOnClickListener(v -> toggleStyle("Culture", chipCulture));
        chipLuxury.setOnClickListener(v -> toggleStyle("Luxury", chipLuxury));
        chipFood.setOnClickListener(v -> toggleStyle("Food", chipFood));

        // Buttons
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());

        // Account rows
        LinearLayout rowPreferences = findViewById(R.id.rowPreferences);
        LinearLayout rowPermissions = findViewById(R.id.rowPermissions);
        LinearLayout rowLogout = findViewById(R.id.rowLogout);

        rowPreferences.setOnClickListener(v ->
                startActivity(new Intent(this, PreferencesActivity.class)));
        rowPermissions.setOnClickListener(v -> openAppSettings());
        rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void selectAiMode(String mode) {
        selectedAiMode = mode;
        refreshAiChips();
    }

    private void refreshAiChips() {
        setChipSelected(chipAiAuto, "auto".equals(selectedAiMode));
        setChipSelected(chipAiOffline, "offline".equals(selectedAiMode));
        setChipSelected(chipAiOnline, "online".equals(selectedAiMode));
    }

    private void toggleStyle(String style, TextView chip) {
        if (selectedStyles.contains(style)) {
            selectedStyles.remove(style);
        } else {
            selectedStyles.add(style);
        }
        setChipSelected(chip, selectedStyles.contains(style));
    }

    private void refreshStyleChips() {
        setChipSelected(chipBudget, selectedStyles.contains("Budget"));
        setChipSelected(chipNature, selectedStyles.contains("Nature"));
        setChipSelected(chipAdventure, selectedStyles.contains("Adventure"));
        setChipSelected(chipCulture, selectedStyles.contains("Culture"));
        setChipSelected(chipLuxury, selectedStyles.contains("Luxury"));
        setChipSelected(chipFood, selectedStyles.contains("Food"));
    }

    private void setChipSelected(TextView chip, boolean selected) {
        if (chip == null) return;
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        chip.setTextColor(selected ? 0xFFFFFFFF : getResources().getColor(R.color.voyage_text_primary, null));
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            etName.setError("Name cannot be empty");
            return;
        }
        session.updateUserName(newName);
        session.setAiMode(selectedAiMode);
        StringBuilder sb = new StringBuilder();
        for (String s : selectedStyles) {
            if (sb.length() > 0) sb.append(",");
            sb.append(s);
        }
        session.setTravelStyle(sb.toString());

        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    session.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
