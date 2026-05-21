package com.example.voyage.ui.trips;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.voyage.R;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.ui.island.SmartIsland;
import com.example.voyage.util.AppContext;
import com.example.voyage.viewmodel.BudgetViewModel;
import com.example.voyage.viewmodel.TripViewModel;

public class TripOverviewFragment extends Fragment {

    private int tripId;
    private Trip currentTrip;
    private TripViewModel tripViewModel;
    private TextView tvAiMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }
        if (tripId == -1) return;

        TextView tvStatDays = view.findViewById(R.id.tvStatDays);
        TextView tvStatBudget = view.findViewById(R.id.tvStatBudget);
        TextView tvStatStyle = view.findViewById(R.id.tvStatStyle);
        TextView tvStatStyleName = view.findViewById(R.id.tvStatStyleName);
        TextView tvStartDate = view.findViewById(R.id.tvStartDate);
        TextView tvEndDate = view.findViewById(R.id.tvEndDate);
        TextView tvBudgetSummary = view.findViewById(R.id.tvBudgetSummary);
        ProgressBar progressBudget = view.findViewById(R.id.progressBudget);
        TextView tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        tvAiMode = view.findViewById(R.id.tvAiMode);
        TextView btnChangeAiMode = view.findViewById(R.id.btnChangeAiMode);
        TextView btnMarkComplete = view.findViewById(R.id.btnMarkComplete);
        TextView btnDeleteTrip = view.findViewById(R.id.btnDeleteTrip);

        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);
        BudgetViewModel budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        tripViewModel.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            if (trip == null) return;
            currentTrip = trip;

            tvStatDays.setText(String.valueOf(trip.days));
            tvStatBudget.setText("$" + (int) trip.budget);
            tvStatStyle.setText(styleEmoji(trip.travelStyle));
            tvStatStyleName.setText(trip.travelStyle != null ? trip.travelStyle : "");
            tvStartDate.setText(trip.startDate != null ? trip.startDate : "—");
            tvEndDate.setText(trip.endDate != null ? trip.endDate : "—");
            tvAiMode.setText(aiModeLabel(trip.aiMode));
            btnMarkComplete.setText(trip.isCompleted ? "Mark Upcoming" : "Mark Complete");

            // Island: rain warning for active trip
            if (!trip.isCompleted && AppContext.weatherCode >= 51) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isAdded()) return;
                    SmartIsland.show(requireActivity(), new SmartIsland.Config()
                            .icon("☔").title("Rain detected")
                            .subtitle("Switch evening plan indoors?")
                            .action("Regenerate", () -> {
                                ViewPager2 pager = requireActivity().findViewById(R.id.viewPager);
                                if (pager != null) pager.setCurrentItem(1, true);
                            })
                            .autoDismiss(10000));
                }, 1000);
            }
        });

        budgetViewModel.getTotalSpentForTrip(tripId).observe(getViewLifecycleOwner(), spent -> {
            if (currentTrip == null) return;
            double spentVal = spent != null ? spent : 0;
            double budget = currentTrip.budget;
            tvBudgetSummary.setText(String.format("$%.0f / $%.0f", spentVal, budget));
            int progress = budget > 0 ? (int) ((spentVal / budget) * 100) : 0;
            progressBudget.setProgress(Math.min(progress, 100));
            double remaining = budget - spentVal;
            tvBudgetRemaining.setText(remaining >= 0
                    ? String.format("$%.2f remaining", remaining)
                    : String.format("$%.2f over budget", -remaining));
        });

        btnChangeAiMode.setOnClickListener(v -> showAiModeDialog());

        btnMarkComplete.setOnClickListener(v -> {
            if (currentTrip == null) return;
            currentTrip.isCompleted = !currentTrip.isCompleted;
            tripViewModel.updateTrip(currentTrip);
        });

        btnDeleteTrip.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip? All data will be lost.")
                    .setPositiveButton("Delete", (d, w) -> {
                        tripViewModel.deleteTrip(tripId);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showAiModeDialog() {
        if (currentTrip == null) return;
        String[] labels = {"🤖 Auto  (switch based on network)", "🌐 Online  (ChatGPT)", "📴 Offline  (Ollama)"};
        String[] values = {"auto", "online", "offline"};

        int checkedItem = 0;
        if (currentTrip.aiMode != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equalsIgnoreCase(currentTrip.aiMode)) { checkedItem = i; break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Change AI Mode")
                .setSingleChoiceItems(labels, checkedItem, null)
                .setPositiveButton("Apply", (d, w) -> {
                    int sel = ((AlertDialog) d).getListView().getCheckedItemPosition();
                    if (sel >= 0) {
                        currentTrip.aiMode = values[sel];
                        tripViewModel.updateTrip(currentTrip);
                        tvAiMode.setText(aiModeLabel(currentTrip.aiMode));
                        // Island: confirm mode switch
                        String[] icons = {"🤖", "🌐", "📴"};
                        String[] descs = {"Smart routing based on network", "ChatGPT online", "Ollama on-device"};
                        SmartIsland.show(requireActivity(), new SmartIsland.Config()
                                .icon(icons[sel]).title("AI mode updated")
                                .subtitle(descs[sel])
                                .autoDismiss(4000));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String styleEmoji(String style) {
        if (style == null) return "✈️";
        switch (style.toLowerCase()) {
            case "relax": return "😌";
            case "adventure": return "🧗";
            case "culture": return "🏛️";
            case "budget": return "💸";
            default: return "✈️";
        }
    }

    private String aiModeLabel(String mode) {
        if (mode == null) return "🤖 Auto";
        switch (mode.toLowerCase()) {
            case "offline": return "📴 Offline";
            case "online": return "🌐 Online";
            default: return "🤖 Auto";
        }
    }
}
