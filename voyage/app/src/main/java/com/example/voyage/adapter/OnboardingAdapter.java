package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private static final String[] EMOJIS = {"🗺️", "📡", "✨"};
    private static final int[] TITLES = {
            R.string.onboard_title_1,
            R.string.onboard_title_2,
            R.string.onboard_title_3
    };
    private static final int[] DESCS = {
            R.string.onboard_desc_1,
            R.string.onboard_desc_2,
            R.string.onboard_desc_3
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvEmoji.setText(EMOJIS[position]);
        holder.tvTitle.setText(TITLES[position]);
        holder.tvDescription.setText(DESCS[position]);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
