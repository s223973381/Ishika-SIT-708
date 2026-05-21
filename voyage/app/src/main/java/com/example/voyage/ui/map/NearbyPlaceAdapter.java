package com.example.voyage.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;

import java.util.List;

public class NearbyPlaceAdapter extends RecyclerView.Adapter<NearbyPlaceAdapter.VH> {

    public interface OnPlaceClickListener {
        void onPlaceClicked(NearbyPlace place);
    }

    private final List<NearbyPlace> places;
    private final OnPlaceClickListener listener;

    public NearbyPlaceAdapter(List<NearbyPlace> places, OnPlaceClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_place_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NearbyPlace place = places.get(position);
        holder.tvEmoji.setText(place.emoji);
        holder.tvName.setText(place.name);
        holder.tvDistance.setText(place.getFormattedDistance());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPlaceClicked(place);
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDistance;

        VH(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvPlaceEmoji);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvDistance = itemView.findViewById(R.id.tvPlaceDistance);
        }
    }
}
