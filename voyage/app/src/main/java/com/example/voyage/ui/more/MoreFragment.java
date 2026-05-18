package com.example.voyage.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.voyage.R;
import com.example.voyage.ui.auth.LoginActivity;
import com.example.voyage.util.SessionManager;

public class MoreFragment extends Fragment {

    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        setupUserHeader(view);
        setupMenuRows(view);
        animateEntrance(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user info if name was updated in ProfileActivity
        View view = getView();
        if (view != null) setupUserHeader(view);
    }

    private void setupUserHeader(View view) {
        TextView tvInitial = view.findViewById(R.id.tvUserInitial);
        TextView tvName = view.findViewById(R.id.tvUserName);
        TextView tvSub = view.findViewById(R.id.tvUserSub);
        LinearLayout userCardRow = view.findViewById(R.id.userCardRow);

        String name = session.getUserName();
        tvName.setText(name);
        tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

        String email = session.getUserEmail();
        tvSub.setText(email.isEmpty() ? "View Profile →" : email);

        userCardRow.setOnClickListener(v -> openProfile());

        // Show AI mode hint
        TextView tvAiHint = view.findViewById(R.id.tvAiModeHint);
        if (tvAiHint != null) {
            String mode = session.getAiMode();
            tvAiHint.setText("Mode: " + capitalize(mode));
        }
    }

    private void setupMenuRows(View view) {
        // Account
        view.findViewById(R.id.rowProfile).setOnClickListener(v -> openProfile());
        view.findViewById(R.id.rowPrivacy).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Privacy settings coming soon", Toast.LENGTH_SHORT).show());

        // Trip Tools
        view.findViewById(R.id.rowBudget).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), GlobalBudgetActivity.class)));
        view.findViewById(R.id.rowPackingLists).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Packing Lists coming soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.rowJournal).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), GlobalJournalActivity.class)));
        view.findViewById(R.id.rowOfflineData).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Offline Data coming soon", Toast.LENGTH_SHORT).show());

        // Safety
        view.findViewById(R.id.rowEmergency).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EmergencyActivity.class)));

        // AI
        view.findViewById(R.id.rowAiSettings).setOnClickListener(v -> openProfile());

        // App
        view.findViewById(R.id.rowHelpAbout).setOnClickListener(v ->
                showAboutDialog());
        view.findViewById(R.id.rowLogout).setOnClickListener(v ->
                showLogoutDialog());
    }

    private void openProfile() {
        startActivity(new Intent(requireContext(), ProfileActivity.class));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    session.logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Voyage v1.0")
                .setMessage("Voyage is your AI-powered travel companion.\n\nPlan trips, track budgets, journal memories, and stay safe — all in one place.\n\nMade with ♥ for travellers everywhere.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void animateEntrance(View view) {
        view.findViewById(R.id.headerLayout)
                .startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Auto";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
