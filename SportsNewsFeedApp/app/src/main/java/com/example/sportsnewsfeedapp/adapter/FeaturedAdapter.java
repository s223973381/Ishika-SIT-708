package com.example.sportsnewsfeedapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.model.NewsItem;

import java.util.List;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private List<NewsItem> items;
    private OnItemClickListener listener;

    public FeaturedAdapter(List<NewsItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFeaturedImage;
        TextView tvFeaturedTitle;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFeaturedImage = itemView.findViewById(R.id.ivFeaturedImage);
            tvFeaturedTitle = itemView.findViewById(R.id.tvFeaturedTitle);
        }
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvFeaturedTitle.setText(item.getTitle());
        holder.ivFeaturedImage.setImageResource(item.getImageResId());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}