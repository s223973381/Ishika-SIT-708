package com.example.llmlearningassistant;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UpgradeActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private String currentPlan;
    private LinearLayout plansContainer;
    private TextView tvCurrentPlan;

    private static final String[] PLAN_NAMES    = {"Free", "Starter", "Intermediate", "Advanced"};
    private static final String[] PLAN_PRICES   = {"$0 / month", "$4.99 / month", "$9.99 / month", "$19.99 / month"};
    private static final String[] PLAN_FEATURES = {
            "5 AI explanations per day\nBasic quiz history (7 days)\n3 topic categories",
            "50 AI explanations per day\nQuiz history (30 days)\nEmail support",
            "Unlimited AI explanations\nFull quiz history\nCustom topics\nProgress analytics",
            "Everything in Intermediate\nOffline mode\nExport progress reports\nEarly access features"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        prefs = getSharedPreferences("llm_prefs", MODE_PRIVATE);
        currentPlan = prefs.getString("plan", "Free");

        LinearLayout card = findViewById(R.id.upgradeCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        tvCurrentPlan = findViewById(R.id.tvCurrentPlan);
        tvCurrentPlan.setText("Current plan: " + currentPlan);

        plansContainer = findViewById(R.id.plansContainer);
        populatePlans();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void populatePlans() {
        plansContainer.removeAllViews();
        for (int i = 0; i < PLAN_NAMES.length; i++) {
            View card = LayoutInflater.from(this)
                    .inflate(R.layout.item_plan, plansContainer, false);

            ((TextView) card.findViewById(R.id.tvPlanName)).setText(PLAN_NAMES[i]);
            ((TextView) card.findViewById(R.id.tvPlanPrice)).setText(PLAN_PRICES[i]);
            ((TextView) card.findViewById(R.id.tvPlanFeatures)).setText(PLAN_FEATURES[i]);

            Button btn = card.findViewById(R.id.btnBuyPlan);
            TextView badge = card.findViewById(R.id.tvActiveBadge);
            final String planName  = PLAN_NAMES[i];
            final String planPrice = PLAN_PRICES[i];

            if (planName.equals(currentPlan)) {
                badge.setVisibility(View.VISIBLE);
                card.setAlpha(1f);

                if (planName.equals("Free")) {
                    // Free plan is active — no way to downgrade further
                    btn.setText("Current Plan");
                    btn.setEnabled(false);
                    btn.getBackground().mutate()
                            .setTint(getResources().getColor(R.color.loading_grey, null));
                } else {
                    // Paid plan is active — allow deactivation
                    btn.setText("Deactivate");
                    btn.setEnabled(true);
                    btn.getBackground().mutate()
                            .setTint(getResources().getColor(R.color.error_red, null));
                    btn.setOnClickListener(v -> confirmDeactivate(planName));
                }
            } else {
                badge.setVisibility(View.GONE);
                card.setAlpha(1f);

                if (planName.equals("Free") && !currentPlan.equals("Free")) {
                    // Offer downgrade to Free from a paid plan
                    btn.setText("Switch to Free");
                    btn.setEnabled(true);
                    btn.getBackground().mutate()
                            .setTint(getResources().getColor(R.color.text_medium, null));
                    btn.setOnClickListener(v -> confirmDeactivate(currentPlan));
                } else if (!planName.equals("Free")) {
                    btn.setText("Upgrade");
                    btn.setEnabled(true);
                    btn.getBackground().mutate()
                            .setTint(getResources().getColor(R.color.coral, null));
                    btn.setOnClickListener(v -> showPaymentSheet(planName, planPrice));
                }
            }

            plansContainer.addView(card);
        }
    }

    private void confirmDeactivate(String planName) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel " + planName + " Plan")
                .setMessage("Are you sure you want to deactivate your " + planName
                        + " plan? You will be moved to the Free plan and lose your paid features.")
                .setPositiveButton("Deactivate", (d, w) -> activatePlan("Free", "Downgrade"))
                .setNegativeButton("Keep Plan", null)
                .show();
    }

    // ── Payment method selection ──────────────────────────────────────────────

    private void showPaymentSheet(String planName, String price) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_payment, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        ((TextView) view.findViewById(R.id.tvPayPlanName)).setText(planName + " Plan");
        ((TextView) view.findViewById(R.id.tvPayPlanPrice)).setText(price);

        view.findViewById(R.id.btnPayGooglePay).setOnClickListener(v -> {
            dialog.dismiss();
            showGooglePaySheet(planName, price);
        });

        view.findViewById(R.id.btnPayCard).setOnClickListener(v -> {
            dialog.dismiss();
            showCardPaymentSheet(planName, price);
        });

        view.findViewById(R.id.tvPayCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ── Google Pay mock sheet ─────────────────────────────────────────────────

    private void showGooglePaySheet(String planName, String price) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_gpay, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        ((TextView) view.findViewById(R.id.tvGPayAmount)).setText(price);
        ((TextView) view.findViewById(R.id.tvGPayPlanName))
                .setText(planName + " Plan — LLM Learning Assistant");

        view.findViewById(R.id.btnGPayConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            activatePlan(planName, "Google Pay");
        });

        view.findViewById(R.id.tvGPayCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ── Credit / Debit card sheet ─────────────────────────────────────────────

    private void showCardPaymentSheet(String planName, String price) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_card_payment, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        ((TextView) view.findViewById(R.id.tvCardPlanSummary))
                .setText(planName + " Plan — " + price);

        EditText etNumber = view.findViewById(R.id.etCardNumber);
        EditText etExpiry = view.findViewById(R.id.etCardExpiry);
        EditText etCvv    = view.findViewById(R.id.etCardCvv);

        view.findViewById(R.id.btnCardPay).setOnClickListener(v -> {
            String number = etNumber.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvv    = etCvv.getText().toString().trim();

            if (number.length() < 16) {
                etNumber.setError("Enter a valid 16-digit card number");
                return;
            }
            if (expiry.length() < 4) {
                etExpiry.setError("Enter expiry as MMYY");
                return;
            }
            if (cvv.length() < 3) {
                etCvv.setError("Enter a valid CVV");
                return;
            }
            dialog.dismiss();
            activatePlan(planName, "Card");
        });

        view.findViewById(R.id.tvCardCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ── Activate plan after payment ───────────────────────────────────────────

    private void activatePlan(String planName, String method) {
        prefs.edit().putString("plan", planName).apply();
        currentPlan = planName;
        tvCurrentPlan.setText("Current plan: " + currentPlan);
        populatePlans();
        String msg = method.equals("Downgrade")
                ? "Plan cancelled. Switched to Free."
                : planName + " plan activated via " + method + "!";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
