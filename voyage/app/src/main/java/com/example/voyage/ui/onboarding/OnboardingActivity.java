package com.example.voyage.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.voyage.R;
import com.example.voyage.adapter.OnboardingAdapter;
import com.example.voyage.ui.auth.LoginActivity;
import com.example.voyage.util.SessionManager;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnGetStarted;
    private TextView tvSkip;
    private View dot1, dot2, dot3;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvSkip = findViewById(R.id.tvSkip);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        viewPager.setAdapter(new OnboardingAdapter());
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateIndicators(position);
                btnGetStarted.setText(position == 2 ? R.string.get_started : R.string.next);
                tvSkip.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
            }
        });

        btnGetStarted.setOnClickListener(v -> {
            btnGetStarted.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
            if (currentPage < 2) {
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                goToLogin();
            }
        });

        tvSkip.setOnClickListener(v -> goToLogin());
    }

    private void updateIndicators(int position) {
        View[] dots = {dot1, dot2, dot3};
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setBackgroundResource(R.drawable.bg_onboarding_indicator_active);
                dots[i].getLayoutParams().width = (int) (24 * getResources().getDisplayMetrics().density);
            } else {
                dots[i].setBackgroundResource(R.drawable.bg_onboarding_indicator_inactive);
                dots[i].getLayoutParams().width = (int) (8 * getResources().getDisplayMetrics().density);
            }
            dots[i].requestLayout();
        }
    }

    private void goToLogin() {
        new SessionManager(this).setOnboardingDone();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    /** Smooth zoom-out page transition */
    private static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();
            if (position < -1 || position > 1) {
                page.setAlpha(0f);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    page.setTranslationX(horizMargin - vertMargin / 2);
                } else {
                    page.setTranslationX(-horizMargin + vertMargin / 2);
                }
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            }
        }
    }
}
