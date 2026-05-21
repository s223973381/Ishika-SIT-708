package com.example.voyage.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voyage.R;
import com.example.voyage.util.SessionManager;

public class PreferencesActivity extends AppCompatActivity {

    private SessionManager session;

    private TextView tvCurrency, tvDistance, tvDateFormat;
    private TextView tvOllamaHost, tvOllamaModel;
    private Switch switchNotif, switchReminders, switchTips;

    private static final String[] CURRENCIES = {
            "USD ($)", "AUD (A$)", "EUR (€)", "GBP (£)", "JPY (¥)", "SGD (S$)", "CAD (C$)"
    };
    private static final String[] CURRENCY_CODES = {
            "USD", "AUD", "EUR", "GBP", "JPY", "SGD", "CAD"
    };
    private static final String[] DISTANCE_LABELS = {"Kilometres (km)", "Miles (mi)"};
    private static final String[] DISTANCE_VALUES = {"km", "mi"};
    private static final String[] DATE_FORMAT_LABELS = {"DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        session = new SessionManager(this);

        tvCurrency = findViewById(R.id.tvCurrency);
        tvDistance = findViewById(R.id.tvDistance);
        tvDateFormat = findViewById(R.id.tvDateFormat);
        tvOllamaHost = findViewById(R.id.tvOllamaHost);
        tvOllamaModel = findViewById(R.id.tvOllamaModel);
        switchNotif = findViewById(R.id.switchNotif);
        switchReminders = findViewById(R.id.switchReminders);
        switchTips = findViewById(R.id.switchTips);

        loadCurrentValues();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ((LinearLayout) findViewById(R.id.rowCurrency)).setOnClickListener(v -> showCurrencyDialog());
        ((LinearLayout) findViewById(R.id.rowDistance)).setOnClickListener(v -> showDistanceDialog());
        ((LinearLayout) findViewById(R.id.rowDateFormat)).setOnClickListener(v -> showDateFormatDialog());
        ((LinearLayout) findViewById(R.id.rowOllamaHost)).setOnClickListener(v -> showEditDialog(
                "Ollama Host",
                "Enter host URL (e.g. http://10.0.2.2:11434)",
                session.getOllamaHost(),
                value -> {
                    session.setOllamaHost(value);
                    tvOllamaHost.setText(value);
                }
        ));
        ((LinearLayout) findViewById(R.id.rowOllamaModel)).setOnClickListener(v -> showEditDialog(
                "Ollama Model",
                "Enter model name (e.g. llama3.2:1b)",
                session.getOllamaModel(),
                value -> {
                    session.setOllamaModel(value);
                    tvOllamaModel.setText(value);
                }
        ));

        switchNotif.setOnCheckedChangeListener((btn, checked) -> session.setNotifEnabled(checked));
        switchReminders.setOnCheckedChangeListener((btn, checked) -> session.setNotifReminders(checked));
        switchTips.setOnCheckedChangeListener((btn, checked) -> session.setNotifTips(checked));
    }

    private void loadCurrentValues() {
        tvCurrency.setText(currencyLabel(session.getCurrency()));
        tvDistance.setText(distanceLabel(session.getDistanceUnit()));
        tvDateFormat.setText(session.getDateFormat());
        tvOllamaHost.setText(session.getOllamaHost());
        tvOllamaModel.setText(session.getOllamaModel());
        switchNotif.setChecked(session.isNotifEnabled());
        switchReminders.setChecked(session.isNotifReminders());
        switchTips.setChecked(session.isNotifTips());
    }

    private String currencyLabel(String code) {
        for (int i = 0; i < CURRENCY_CODES.length; i++) {
            if (CURRENCY_CODES[i].equals(code)) return CURRENCIES[i];
        }
        return code;
    }

    private String distanceLabel(String unit) {
        return "mi".equals(unit) ? DISTANCE_LABELS[1] : DISTANCE_LABELS[0];
    }

    private void showCurrencyDialog() {
        String current = session.getCurrency();
        int checked = 0;
        for (int i = 0; i < CURRENCY_CODES.length; i++) {
            if (CURRENCY_CODES[i].equals(current)) { checked = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setSingleChoiceItems(CURRENCIES, checked, (dialog, which) -> {
                    session.setCurrency(CURRENCY_CODES[which]);
                    tvCurrency.setText(CURRENCIES[which]);
                    dialog.dismiss();
                    Toast.makeText(this, "Currency updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDistanceDialog() {
        String current = session.getDistanceUnit();
        int checked = "mi".equals(current) ? 1 : 0;
        new AlertDialog.Builder(this)
                .setTitle("Distance Unit")
                .setSingleChoiceItems(DISTANCE_LABELS, checked, (dialog, which) -> {
                    session.setDistanceUnit(DISTANCE_VALUES[which]);
                    tvDistance.setText(DISTANCE_LABELS[which]);
                    dialog.dismiss();
                    Toast.makeText(this, "Distance unit updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDateFormatDialog() {
        String current = session.getDateFormat();
        int checked = 0;
        for (int i = 0; i < DATE_FORMAT_LABELS.length; i++) {
            if (DATE_FORMAT_LABELS[i].equals(current)) { checked = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle("Date Format")
                .setSingleChoiceItems(DATE_FORMAT_LABELS, checked, (dialog, which) -> {
                    session.setDateFormat(DATE_FORMAT_LABELS[which]);
                    tvDateFormat.setText(DATE_FORMAT_LABELS[which]);
                    dialog.dismiss();
                    Toast.makeText(this, "Date format updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(String title, String hint, String currentValue, OnSaveCallback callback) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText etInput = dialogView.findViewById(R.id.etDialogInput);
        etInput.setText(currentValue);
        etInput.setSelection(currentValue.length());
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(hint)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String value = etInput.getText().toString().trim();
                    if (!value.isEmpty()) {
                        callback.onSave(value);
                        Toast.makeText(this, title + " updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private interface OnSaveCallback {
        void onSave(String value);
    }
}
