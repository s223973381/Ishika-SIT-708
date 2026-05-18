package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.database.pojo.TripSpentSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripBudgetAdapter extends RecyclerView.Adapter<TripBudgetAdapter.ViewHolder> {

    public interface Listener {
        void onViewTrip(Trip trip);
    }

    private List<Trip> trips = new ArrayList<>();
    private Map<Integer, Double> spentMap = new HashMap<>();
    private final Listener listener;

    public TripBudgetAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> list) {
        trips = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSpentTotals(List<TripSpentSummary> summaries) {
        spentMap.clear();
        if (summaries != null) {
            for (TripSpentSummary s : summaries) {
                spentMap.put(s.tripId, s.totalSpent);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Trip trip = trips.get(pos);
        double spent = spentMap.containsKey(trip.tripId) ? spentMap.get(trip.tripId) : 0.0;
        double budget = trip.budget;
        double remaining = budget - spent;

        h.tvDestination.setText(trip.destination != null ? trip.destination : trip.title);
        h.tvDays.setText(trip.days + " days");
        h.tvSpent.setText(String.format("$%.0f spent", spent));
        h.tvBudgetTotal.setText(String.format("of $%.0f", budget));
        h.tvRemaining.setText(String.format("$%.0f remaining", Math.max(0, remaining)));

        int progress = budget > 0 ? (int) ((spent / budget) * 100) : 0;
        h.budgetProgress.setProgress(Math.min(progress, 100));

        if (remaining < 0) {
            h.tvRemaining.setText(String.format("$%.0f over budget!", Math.abs(remaining)));
            h.tvRemaining.setTextColor(0xFFC62828);
        }

        h.tvViewTrip.setOnClickListener(v -> listener.onViewTrip(trip));
        h.itemView.setOnClickListener(v -> listener.onViewTrip(trip));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDestination, tvDays, tvSpent, tvBudgetTotal, tvRemaining, tvViewTrip;
        ProgressBar budgetProgress;

        ViewHolder(View v) {
            super(v);
            tvDestination = v.findViewById(R.id.tvTripDestination);
            tvDays = v.findViewById(R.id.tvTripDays);
            tvSpent = v.findViewById(R.id.tvSpent);
            tvBudgetTotal = v.findViewById(R.id.tvBudgetTotal);
            tvRemaining = v.findViewById(R.id.tvRemaining);
            budgetProgress = v.findViewById(R.id.budgetProgress);
            tvViewTrip = v.findViewById(R.id.tvViewTrip);
        }
    }
}
