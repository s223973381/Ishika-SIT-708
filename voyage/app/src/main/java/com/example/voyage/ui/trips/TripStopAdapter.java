package com.example.voyage.ui.trips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.ItineraryItem;

import java.util.List;

public class TripStopAdapter extends RecyclerView.Adapter<TripStopAdapter.VH> {

    public interface OnStopClickListener {
        void onStopClicked(ItineraryItem item);
    }

    private final List<ItineraryItem> items;
    private final OnStopClickListener listener;

    public TripStopAdapter(List<ItineraryItem> items, OnStopClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_stop_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItineraryItem item = items.get(position);
        holder.tvBadge.setText(String.format("%02d", position + 1));
        holder.tvSlot.setText(slotEmoji(item.timeSlot));
        holder.tvTitle.setText(item.title != null && !item.title.isEmpty()
                ? item.title : item.locationName);
        holder.tvDay.setText("Day " + item.dayNumber);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !(item.latitude == 0 && item.longitude == 0)) {
                listener.onStopClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvBadge, tvSlot, tvTitle, tvDay;

        VH(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tvStopBadge);
            tvSlot = itemView.findViewById(R.id.tvStopSlot);
            tvTitle = itemView.findViewById(R.id.tvStopTitle);
            tvDay = itemView.findViewById(R.id.tvStopDay);
        }
    }

    private static String slotEmoji(String slot) {
        if (slot == null) return "📍";
        switch (slot.toLowerCase()) {
            case "morning":   return "🌅";
            case "afternoon": return "☀";
            case "evening":   return "🌙";
            default:          return "📍";
        }
    }
}
