package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripCardAdapter extends RecyclerView.Adapter<TripCardAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private List<Trip> trips = new ArrayList<>();
    private OnTripClickListener listener;

    public TripCardAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips != null ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Trip trip = trips.get(position);

        h.tvDestination.setText(trip.destination != null ? trip.destination : "");
        h.tvTitle.setText(trip.title != null ? trip.title : "");
        h.tvDates.setText(formatDates(trip.startDate, trip.endDate));
        h.tvDays.setText(trip.days + " days");
        h.tvBudget.setText("Budget: $" + (int) trip.budget);
        h.tvStyle.setText(trip.travelStyle != null ? trip.travelStyle : "");
        h.tvEmoji.setText(styleEmoji(trip.travelStyle));

        h.tvViewTrip.setOnClickListener(v -> {
            if (listener != null) listener.onTripClick(trip);
        });
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTripClick(trip);
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    private String formatDates(String start, String end) {
        if (start == null || end == null) return "Dates TBD";
        return start + " – " + end;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDestination, tvTitle, tvDates, tvDays, tvBudget, tvStyle, tvEmoji, tvViewTrip;

        ViewHolder(View v) {
            super(v);
            tvDestination = v.findViewById(R.id.tvTripDestination);
            tvTitle = v.findViewById(R.id.tvTripTitle);
            tvDates = v.findViewById(R.id.tvTripDates);
            tvDays = v.findViewById(R.id.tvTripDays);
            tvBudget = v.findViewById(R.id.tvTripBudget);
            tvStyle = v.findViewById(R.id.tvTripStyle);
            tvEmoji = v.findViewById(R.id.tvTripEmoji);
            tvViewTrip = v.findViewById(R.id.tvViewTrip);
        }
    }
}
