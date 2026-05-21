package com.example.voyage.ui.island;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.example.voyage.R;

public class SmartIslandView extends LinearLayout {

    private final TextView tvIcon;
    private final TextView tvTitle;
    private final TextView tvSubtitle;
    private final TextView tvAction;
    private final TextView tvChevron;
    private final LinearLayout expandedContent;
    private boolean isExpanded = false;

    public SmartIslandView(Context context, Runnable onDismiss) {
        super(context);
        setOrientation(VERTICAL);
        setMinimumWidth(dp(220));

        LayoutInflater.from(context).inflate(R.layout.view_smart_island, this, true);

        // Dark warm pill background — no XML drawable needed
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A1008"));
        bg.setCornerRadius(dp(28));
        setBackground(bg);
        setElevation(dp(12));
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);

        int padH = dp(16);
        int padV = dp(12);
        setPadding(padH, padV, padH, padV);

        tvIcon          = findViewById(R.id.tvIslandIcon);
        tvTitle         = findViewById(R.id.tvIslandTitle);
        tvSubtitle      = findViewById(R.id.tvIslandSubtitle);
        tvAction        = findViewById(R.id.tvIslandAction);
        tvChevron       = findViewById(R.id.tvIslandChevron);
        expandedContent = findViewById(R.id.islandExpandedContent);

        // Warm white pill for the action button
        GradientDrawable actionBg = new GradientDrawable();
        actionBg.setColor(Color.parseColor("#FFFDF8"));
        actionBg.setCornerRadius(dp(8));
        tvAction.setBackground(actionBg);

        setOnClickListener(v -> toggle());
        findViewById(R.id.tvIslandDismiss).setOnClickListener(v -> {
            if (onDismiss != null) onDismiss.run();
        });
    }

    public void configure(SmartIsland.Config config, Runnable onDismiss) {
        tvIcon.setText(config.icon != null ? config.icon : "ℹ️");
        tvTitle.setText(config.title != null ? config.title : "");

        if (config.subtitle != null && !config.subtitle.isEmpty()) {
            tvSubtitle.setText(config.subtitle);
        }

        if (config.actionLabel != null && config.onAction != null) {
            tvAction.setText(config.actionLabel);
            tvAction.setVisibility(View.VISIBLE);
            tvAction.setOnClickListener(v -> {
                if (onDismiss != null) onDismiss.run();
                config.onAction.run();
            });
        } else {
            tvAction.setVisibility(View.GONE);
        }

        // Re-wire dismiss button with the updated callback
        findViewById(R.id.tvIslandDismiss).setOnClickListener(v -> {
            if (onDismiss != null) onDismiss.run();
        });
    }

    private void toggle() {
        isExpanded = !isExpanded;
        TransitionManager.beginDelayedTransition(this, new ChangeBounds());
        expandedContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        tvChevron.setText(isExpanded ? "↑" : "›");
    }

    public void animateIn() {
        setAlpha(0f);
        setScaleX(0.82f);
        setScaleY(0.82f);
        setTranslationY(-dp(28));
        animate()
                .alpha(1f).scaleX(1f).scaleY(1f).translationY(0)
                .setDuration(380)
                .setInterpolator(new DecelerateInterpolator(1.8f))
                .start();
    }

    public void animateOut(Runnable onEnd) {
        animate()
                .alpha(0f).scaleX(0.82f).scaleY(0.82f).translationY(-dp(28))
                .setDuration(220)
                .setInterpolator(new AccelerateInterpolator(1.5f))
                .withEndAction(onEnd)
                .start();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
