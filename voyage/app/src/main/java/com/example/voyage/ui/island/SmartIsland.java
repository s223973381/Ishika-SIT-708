package com.example.voyage.ui.island;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Smart Travel Island — a Dynamic Island–inspired floating pill overlay.
 *
 * Usage:
 *   SmartIsland.show(activity, new SmartIsland.Config()
 *       .icon("☔").title("Rain detected").subtitle("Switch evening plan?")
 *       .action("Regenerate", () -> { ... }).autoDismiss(8000));
 *
 *   SmartIsland.dismiss();
 */
public class SmartIsland {

    // ── Config builder ────────────────────────────────────────────

    public static class Config {
        String icon;
        String title;
        String subtitle;
        String actionLabel;
        Runnable onAction;
        long autoDismissMs = 6000;

        public Config icon(String v)   { icon = v; return this; }
        public Config title(String v)  { title = v; return this; }
        public Config subtitle(String v) { subtitle = v; return this; }
        public Config action(String label, Runnable r) {
            actionLabel = label; onAction = r; return this;
        }
        /** 0 = never auto-dismiss */
        public Config autoDismiss(long ms) { autoDismissMs = ms; return this; }
    }

    // ── Internal state ────────────────────────────────────────────

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static WeakReference<SmartIslandView> activeRef;
    private static Runnable scheduledDismiss;

    // ── Public API ────────────────────────────────────────────────

    public static void show(Activity activity, Config config) {
        if (activity == null || activity.isFinishing()) return;
        handler.post(() -> {
            removeActive();

            SmartIslandView island = new SmartIslandView(activity, SmartIsland::removeActive);
            island.configure(config, SmartIsland::removeActive);

            FrameLayout root = activity.findViewById(android.R.id.content);
            float density = activity.getResources().getDisplayMetrics().density;
            int statusBar = getStatusBarHeight(activity);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            params.topMargin = statusBar + Math.round(8 * density);

            root.addView(island, params);
            activeRef = new WeakReference<>(island);
            island.animateIn();

            if (config.autoDismissMs > 0) {
                scheduledDismiss = SmartIsland::removeActive;
                handler.postDelayed(scheduledDismiss, config.autoDismissMs);
            }
        });
    }

    /** Immediately dismisses the island with its exit animation. */
    public static void dismiss() {
        handler.post(SmartIsland::removeActive);
    }

    // ── Internal ──────────────────────────────────────────────────

    private static void removeActive() {
        if (scheduledDismiss != null) {
            handler.removeCallbacks(scheduledDismiss);
            scheduledDismiss = null;
        }
        if (activeRef == null) return;
        SmartIslandView v = activeRef.get();
        activeRef = null;
        if (v != null && v.getParent() != null) {
            v.animateOut(() -> {
                ViewGroup parent = (ViewGroup) v.getParent();
                if (parent != null) parent.removeView(v);
            });
        }
    }

    private static int getStatusBarHeight(Activity activity) {
        int id = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) return activity.getResources().getDimensionPixelSize(id);
        return Math.round(24 * activity.getResources().getDisplayMetrics().density);
    }
}
