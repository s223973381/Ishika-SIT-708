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

public class LatestNewsAdapter extends RecyclerView.Adapter<LatestNewsAdapter.LatestNewsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private List<NewsItem> items;
    private OnItemClickListener listener;

    public LatestNewsAdapter(List<NewsItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class LatestNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLatestImage;
        TextView tvLatestTitle, tvLatestCategory;

        public LatestNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLatestImage = itemView.findViewById(R.id.ivLatestImage);
            tvLatestTitle = itemView.findViewById(R.id.tvLatestTitle);
            tvLatestCategory = itemView.findViewById(R.id.tvLatestCategory);
        }
    }

    @NonNull
    @Override
    public LatestNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_latest_news, parent, false);
        return new LatestNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LatestNewsViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvLatestTitle.setText(item.getTitle());
        holder.tvLatestCategory.setText(item.getCategory());
        holder.ivLatestImage.setImageResource(item.getImageResId());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<NewsItem> newList) {
        items = newList;
        notifyDataSetChanged();
    }
}