package com.example.voyage.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(JournalEntry entry);
    }

    private List<JournalEntry> entries = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public JournalEntryAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEntries(List<JournalEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JournalEntry entry = entries.get(position);

        h.tvMoodEmoji.setText(moodEmoji(entry.mood));
        h.tvMoodBadge.setText(entry.mood != null ? capitalize(entry.mood) : "");
        h.tvTitle.setText(entry.title != null ? entry.title : "");
        h.tvDate.setText(entry.date != null ? entry.date : "");
        h.tvPreview.setText(entry.content != null ? entry.content : "");

        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(entry);
        });

        String path = entry.imagePath;
        if (path != null && !path.isEmpty()) {
            h.ivPhoto.setVisibility(View.VISIBLE);
            h.ivPhoto.setTag(path);
            h.ivPhoto.setImageBitmap(null);
            new Thread(() -> {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                h.ivPhoto.post(() -> {
                    if (path.equals(h.ivPhoto.getTag())) {
                        h.ivPhoto.setImageBitmap(bmp);
                    }
                });
            }).start();
        } else {
            h.ivPhoto.setVisibility(View.GONE);
            h.ivPhoto.setImageBitmap(null);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private String moodEmoji(String mood) {
        if (mood == null) return "😊";
        switch (mood.toLowerCase()) {
            case "excited":     return "🤩";
            case "tired":       return "😴";
            case "reflective":  return "🤔";
            case "adventurous": return "🧗";
            default:            return "😊";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoodEmoji, tvTitle, tvDate, tvPreview, tvMoodBadge, btnDelete;
        ImageView ivPhoto;

        ViewHolder(View v) {
            super(v);
            tvMoodEmoji = v.findViewById(R.id.tvMoodEmoji);
            tvTitle     = v.findViewById(R.id.tvEntryTitle);
            tvDate      = v.findViewById(R.id.tvEntryDate);
            tvPreview   = v.findViewById(R.id.tvEntryPreview);
            tvMoodBadge = v.findViewById(R.id.tvMoodBadge);
            btnDelete   = v.findViewById(R.id.btnDeleteEntry);
            ivPhoto     = v.findViewById(R.id.ivEntryPhoto);
        }
    }
}
