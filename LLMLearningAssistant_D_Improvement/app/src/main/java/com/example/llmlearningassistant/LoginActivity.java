package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvNeedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvNeedAccount = findViewById(R.id.tvNeedAccount);

        // Fade-in animation on the card
        View card = findViewById(R.id.loginCard);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        card.startAnimation(fadeIn);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvNeedAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        // Check registered credentials first, then fall back to demo account
        android.content.SharedPreferences prefs = getSharedPreferences("llm_prefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", null);
        String savedPassword = prefs.getString("password", null);

        boolean validRegistered = savedUsername != null
                && username.equals(savedUsername)
                && password.equals(savedPassword);
        boolean validDemo = username.equals("student") && password.equals("password");

        if (validRegistered || validDemo) {
            prefs.edit().putString("username", username).apply();
            Intent intent = new Intent(LoginActivity.this, InterestActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG).show();
        }
    }
}
