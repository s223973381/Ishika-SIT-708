package com.example.voyage.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voyage.MainActivity;
import com.example.voyage.R;
import com.example.voyage.ui.auth.LoginActivity;
import com.example.voyage.ui.onboarding.OnboardingActivity;
import com.example.voyage.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent intent;
            if (!session.isOnboardingDone()) {
                intent = new Intent(this, OnboardingActivity.class);
            } else if (!session.isLoggedIn()) {
                intent = new Intent(this, LoginActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000);
    }
}
