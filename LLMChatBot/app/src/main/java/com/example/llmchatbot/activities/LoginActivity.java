package com.example.llmchatbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmchatbot.R;
import com.example.llmchatbot.database.ChatDatabase;
import com.example.llmchatbot.database.User;
import com.example.llmchatbot.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnAction;
    private TextView tvCardTitle, tvCardSubtitle, tvTogglePrompt, tvToggleAction;

    private boolean isRegisterMode = false;

    private SessionManager sessionManager;
    private ChatDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        database = ChatDatabase.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnAction = findViewById(R.id.btnAction);
        tvCardTitle = findViewById(R.id.tvCardTitle);
        tvCardSubtitle = findViewById(R.id.tvCardSubtitle);
        tvTogglePrompt = findViewById(R.id.tvTogglePrompt);
        tvToggleAction = findViewById(R.id.tvToggleAction);

        btnAction.setOnClickListener(v -> {
            if (isRegisterMode) performRegister();
            else performLogin();
        });

        tvToggleAction.setOnClickListener(v -> toggleMode());
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        executor.execute(() -> {
            User user = database.userDao().findByUsername(username);
            runOnUiThread(() -> {
                if (user == null) {
                    etUsername.setError("No account found. Please register.");
                    etUsername.requestFocus();
                } else if (!user.getPassword().equals(password)) {
                    etPassword.setError("Incorrect password");
                    etPassword.requestFocus();
                } else {
                    sessionManager.saveUsername(username);
                    startActivity(new Intent(this, ChatActivity.class));
                    finish();
                }
            });
        });
    }

    private void performRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        executor.execute(() -> {
            int exists = database.userDao().usernameExists(username);
            runOnUiThread(() -> {
                if (exists > 0) {
                    etUsername.setError("Username already taken");
                    etUsername.requestFocus();
                } else {
                    executor.execute(() -> {
                        database.userDao().insertUser(new User(username, password));
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show();
                            toggleMode();
                        });
                    });
                }
            });
        });
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            tvCardTitle.setText("Create Account");
            tvCardSubtitle.setText("Register to start chatting with AI");
            btnAction.setText("Create Account");
            etConfirmPassword.setVisibility(View.VISIBLE);
            tvTogglePrompt.setText("Already have an account? ");
            tvToggleAction.setText("Sign In");
        } else {
            tvCardTitle.setText("Sign In");
            tvCardSubtitle.setText("Welcome back! Please sign in.");
            btnAction.setText("Sign In");
            etConfirmPassword.setVisibility(View.GONE);
            etConfirmPassword.setText("");
            tvTogglePrompt.setText("Don't have an account? ");
            tvToggleAction.setText("Create one");
        }
        etUsername.setText("");
        etPassword.setText("");
        etUsername.setError(null);
        etPassword.setError(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
