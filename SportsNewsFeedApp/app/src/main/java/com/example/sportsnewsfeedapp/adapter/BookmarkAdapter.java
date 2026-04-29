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

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<NewsItem> items;

    public BookmarkAdapter(List<NewsItem> items) {
        this.items = items;
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookmarkImage;
        TextView tvBookmarkTitle, tvBookmarkCategory;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookmarkImage = itemView.findViewById(R.id.ivBookmarkImage);
            tvBookmarkTitle = itemView.findViewById(R.id.tvBookmarkTitle);
            tvBookmarkCategory = itemView.findViewById(R.id.tvBookmarkCategory);
        }
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvBookmarkTitle.setText(item.getTitle());
        holder.tvBookmarkCategory.setText(item.getCategory());
        holder.ivBookmarkImage.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}