package com.example.llmlearningassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etConfirmEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        View card = findViewById(R.id.registerCard);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        card.startAnimation(fadeIn);

        btnCreateAccount.setOnClickListener(v -> attemptRegister());

        // Back button support
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String confirmEmail = etConfirmEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username)) { etUsername.setError("Required"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Required"); return; }
        if (!email.equals(confirmEmail)) { etConfirmEmail.setError("Emails do not match"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Required"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Required"); return; }

        // Registration success — persist user data and go to interests screen
        getSharedPreferences("llm_prefs", MODE_PRIVATE).edit()
                .putString("username", username)
                .putString("email", email)
                .putString("password", password)
                .putString("plan", "Free")
                .apply();
        Intent intent = new Intent(RegisterActivity.this, InterestActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
