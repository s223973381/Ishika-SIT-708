package com.example.istreamapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.istreamapp.R;
import com.example.istreamapp.database.PlaylistItem;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PlaylistItem item);
    }

    private final List<PlaylistItem> items;
    private final OnItemClickListener listener;

    public PlaylistAdapter(List<PlaylistItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistItem item = items.get(position);
        holder.tvUrl.setText(item.videoUrl);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUrl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrl = itemView.findViewById(R.id.tvPlaylistUrl);
        }
    }
}