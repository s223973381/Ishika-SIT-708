package com.example.istreamapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istreamapp.database.AppDatabase;
import com.example.istreamapp.database.User;
import com.example.istreamapp.utils.SessionManager;

import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword, etConfirmPassword;
    private TextView tvError;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = AppDatabase.getInstance(this);

        etFullName        = findViewById(R.id.etFullName);
        etUsername        = findViewById(R.id.etUsername);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError           = findViewById(R.id.tvError);

        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> attemptSignup());
        findViewById(R.id.btnBackLogin).setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String fullName  = etFullName.getText().toString().trim();
        String username  = etUsername.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirm   = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            tvError.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            tvError.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 4) {
            tvError.setText("Password must be at least 4 characters.");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Check if username already taken
            User existing = db.userDao().getUserByUsername(username);
            runOnUiThread(() -> {
                if (existing != null) {
                    tvError.setText("Username already taken. Choose another.");
                } else {
                    // Insert on background thread
                    Executors.newSingleThreadExecutor().execute(() -> {
                        User newUser = new User();
                        newUser.fullName = fullName;
                        newUser.username = username;
                        newUser.password = password;
                        db.userDao().insertUser(newUser);
                        runOnUiThread(() -> {
                            // Go back to login with success hint
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.putExtra("signup_success", true);
                            startActivity(intent);
                            finish();
                        });
                    });
                }
            });
        });
    }
}