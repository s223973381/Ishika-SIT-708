package com.example.istreamapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istreamapp.database.AppDatabase;
import com.example.istreamapp.database.User;
import com.example.istreamapp.utils.SessionManager;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvError;
    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        // If already logged in, skip straight to home
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvError    = findViewById(R.id.tvError);

        if (getIntent().getBooleanExtra("signup_success", false)) {
            Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnGoSignup).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            tvError.setText("Please fill in all fields.");
            return;
        }

        // Room DB calls must be off the main thread
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    session.saveSession(user.id, user.username);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    tvError.setText("Invalid username or password.");
                }
            });
        });
    }
}