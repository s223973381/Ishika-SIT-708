package com.example.lostfoundapp.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.database.LostFoundItem;
import com.example.lostfoundapp.utils.DateTimeUtils;
import com.example.lostfoundapp.utils.ImageUtils;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<LostFoundItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    public ItemAdapter(List<LostFoundItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<LostFoundItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        Chip chipType;
        TextView tvName, tvCategory, tvLocation, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail  = itemView.findViewById(R.id.iv_thumbnail);
            chipType     = itemView.findViewById(R.id.chip_type);
            tvName       = itemView.findViewById(R.id.tv_item_name);
            tvCategory   = itemView.findViewById(R.id.tv_item_category);
            tvLocation   = itemView.findViewById(R.id.tv_item_location);
            tvTime       = itemView.findViewById(R.id.tv_item_time);
        }

        void bind(LostFoundItem item, OnItemClickListener listener) {
            tvName.setText(item.getName());
            tvCategory.setText(item.getCategory());
            tvLocation.setText(item.getLocation());
            tvTime.setText(DateTimeUtils.getRelativeTime(item.getTimestamp()));

            chipType.setText(item.getPostType());
            if ("Lost".equals(item.getPostType())) {
                chipType.setChipBackgroundColorResource(R.color.lost_color);
            } else {
                chipType.setChipBackgroundColorResource(R.color.found_color);
            }
            chipType.setTextColor(itemView.getContext().getColor(R.color.chip_text_white));

            Bitmap bmp = ImageUtils.loadBitmap(item.getImagePath());
            if (bmp != null) {
                ivThumbnail.setImageBitmap(bmp);
            } else {
                ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
