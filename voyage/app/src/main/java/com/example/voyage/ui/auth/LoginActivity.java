package com.example.voyage.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.voyage.MainActivity;
import com.example.voyage.R;
import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.entities.User;
import com.example.voyage.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextInputLayout tilName;
    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnPrimary, btnGoogle;
    private TextView tvGuest, tvError;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tabLayout = findViewById(R.id.tabLayout);
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvGuest = findViewById(R.id.tvGuest);
        tvError = findViewById(R.id.tvError);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLoginMode = tab.getPosition() == 0;
                tilName.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);
                btnPrimary.setText(isLoginMode ? R.string.login : R.string.register);
                tvError.setVisibility(View.GONE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnPrimary.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
            if (isLoginMode) handleLogin();
            else handleRegister();
        });

        btnGoogle.setOnClickListener(v -> {
            // Google sign-in placeholder
            showError("Google sign-in coming soon");
        });

        tvGuest.setOnClickListener(v -> {
            new SessionManager(this).saveGuestSession();
            goToMain();
        });
    }

    private void handleLogin() {
        String email = getStr(etEmail);
        String password = getStr(etPassword);
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = AppDatabase.getInstance(this).userDao().getUserByEmail(email);
            runOnUiThread(() -> {
                if (user != null) {
                    new SessionManager(this).saveUserSession(user.userId, user.name, user.email);
                    goToMain();
                } else {
                    showError("No account found. Please register.");
                }
            });
        });
    }

    private void handleRegister() {
        String name = getStr(etName);
        String email = getStr(etEmail);
        String password = getStr(etPassword);
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        User user = new User();
        user.name = name;
        user.email = email;
        user.travelStyle = "Adventure";
        user.preferredAiMode = "auto";
        user.createdAt = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            long id = AppDatabase.getInstance(this).userDao().insert(user);
            runOnUiThread(() -> {
                new SessionManager(this).saveUserSession((int) id, name, email);
                goToMain();
            });
        });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private String getStr(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
