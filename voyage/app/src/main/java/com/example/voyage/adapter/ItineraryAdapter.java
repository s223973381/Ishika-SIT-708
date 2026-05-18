package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.ItineraryItem;

import java.util.ArrayList;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(ItineraryItem item);
    }

    private List<ItineraryItem> items = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public ItineraryAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setItems(List<ItineraryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itinerary_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ItineraryItem item = items.get(position);

        // Show day header when day changes
        boolean showHeader = (position == 0) ||
                (items.get(position - 1).dayNumber != item.dayNumber);
        h.tvDayHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        h.tvDayHeader.setText("Day " + item.dayNumber);

        h.tvTimeSlotEmoji.setText(slotEmoji(item.timeSlot));
        h.tvTimeSlot.setText(slotLabel(item.timeSlot));
        h.tvItemTitle.setText(item.title != null ? item.title : "");
        h.tvItemLocation.setText(item.locationName != null && !item.locationName.isEmpty()
                ? "📍 " + item.locationName : "");
        h.tvItemTime.setText(formatTime(item.startTime, item.endTime));

        if (item.estimatedCost > 0) {
            h.tvItemCost.setText("$" + (int) item.estimatedCost);
            h.tvItemCost.setVisibility(View.VISIBLE);
        } else {
            h.tvItemCost.setVisibility(View.GONE);
        }

        h.btnDeleteItem.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String slotEmoji(String slot) {
        if (slot == null) return "🌅";
        switch (slot) {
            case "afternoon": return "☀️";
            case "evening": return "🌙";
            default: return "🌅";
        }
    }

    private String slotLabel(String slot) {
        if (slot == null) return "AM";
        switch (slot) {
            case "afternoon": return "PM";
            case "evening": return "Eve";
            default: return "AM";
        }
    }

    private String formatTime(String start, String end) {
        if (start == null || start.isEmpty()) return "";
        if (end == null || end.isEmpty()) return start;
        return start + " – " + end;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayHeader, tvTimeSlotEmoji, tvTimeSlot, tvItemTitle,
                tvItemLocation, tvItemTime, tvItemCost, btnDeleteItem;

        ViewHolder(View v) {
            super(v);
            tvDayHeader = v.findViewById(R.id.tvDayHeader);
            tvTimeSlotEmoji = v.findViewById(R.id.tvTimeSlotEmoji);
            tvTimeSlot = v.findViewById(R.id.tvTimeSlot);
            tvItemTitle = v.findViewById(R.id.tvItemTitle);
            tvItemLocation = v.findViewById(R.id.tvItemLocation);
            tvItemTime = v.findViewById(R.id.tvItemTime);
            tvItemCost = v.findViewById(R.id.tvItemCost);
            btnDeleteItem = v.findViewById(R.id.btnDeleteItem);
        }
    }
}
