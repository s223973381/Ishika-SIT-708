package com.example.sportsnewsfeedapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.model.NewsItem;

import java.util.List;

public class RelatedStoriesAdapter extends RecyclerView.Adapter<RelatedStoriesAdapter.RelatedViewHolder> {

    private List<NewsItem> items;

    public RelatedStoriesAdapter(List<NewsItem> items) {
        this.items = items;
    }

    public static class RelatedViewHolder extends RecyclerView.ViewHolder {
        TextView tvRelatedTitle, tvRelatedCategory;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRelatedTitle = itemView.findViewById(R.id.tvRelatedTitle);
            tvRelatedCategory = itemView.findViewById(R.id.tvRelatedCategory);
        }
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_related_story, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvRelatedTitle.setText(item.getTitle());
        holder.tvRelatedCategory.setText(item.getCategory());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}