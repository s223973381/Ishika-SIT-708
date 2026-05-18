package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.JournalEntry;
import com.example.voyage.database.entities.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalJournalAdapter extends RecyclerView.Adapter<GlobalJournalAdapter.ViewHolder> {

    public interface Listener {
        void onEntryClick(JournalEntry entry, Trip trip);
    }

    private List<JournalEntry> entries = new ArrayList<>();
    private Map<Integer, Trip> tripMap = new HashMap<>();
    private final Listener listener;

    public GlobalJournalAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setEntries(List<JournalEntry> list) {
        entries = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setTrips(List<Trip> trips) {
        tripMap.clear();
        if (trips != null) {
            for (Trip t : trips) tripMap.put(t.tripId, t);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_global, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        JournalEntry entry = entries.get(pos);
        Trip trip = tripMap.get(entry.tripId);

        String moodEmoji = moodToEmoji(entry.mood);
        h.tvMood.setText(moodEmoji);

        String tripName = trip != null ? trip.destination : "Trip";
        h.tvTripName.setText(tripName);

        h.tvTitle.setText(entry.title != null ? entry.title : "Entry");

        String preview = entry.content != null ? entry.content : "";
        h.tvPreview.setText(preview.length() > 100 ? preview.substring(0, 100) + "…" : preview);

        h.tvDate.setText(entry.date != null ? entry.date : "");

        h.itemView.setOnClickListener(v -> listener.onEntryClick(entry, trip));
    }

    private String moodToEmoji(String mood) {
        if (mood == null) return "📝";
        switch (mood.toLowerCase()) {
            case "happy": return "😊";
            case "excited": return "🤩";
            case "calm": return "😌";
            case "tired": return "😴";
            case "adventurous": return "🧗";
            default: return "📝";
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMood, tvTripName, tvTitle, tvPreview, tvDate;

        ViewHolder(View v) {
            super(v);
            tvMood = v.findViewById(R.id.tvMood);
            tvTripName = v.findViewById(R.id.tvTripName);
            tvTitle = v.findViewById(R.id.tvEntryTitle);
            tvPreview = v.findViewById(R.id.tvEntryPreview);
            tvDate = v.findViewById(R.id.tvEntryDate);
        }
    }
}
