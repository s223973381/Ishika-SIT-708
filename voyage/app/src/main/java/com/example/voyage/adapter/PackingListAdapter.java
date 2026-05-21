package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.pojo.PackingListWithCount;

import java.util.ArrayList;
import java.util.List;

public class PackingListAdapter extends RecyclerView.Adapter<PackingListAdapter.ViewHolder> {

    public interface OnListClickListener {
        void onClick(PackingListWithCount list);
    }

    private List<PackingListWithCount> lists = new ArrayList<>();
    private final OnListClickListener listener;

    public PackingListAdapter(OnListClickListener listener) {
        this.listener = listener;
    }

    public void setLists(List<PackingListWithCount> lists) {
        this.lists = lists != null ? lists : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_packing_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PackingListWithCount item = lists.get(position);
        h.tvListName.setText(item.listName);

        if (item.totalItems == 0) {
            h.tvItemCount.setText("No items yet");
            h.progress.setProgress(0);
        } else {
            h.tvItemCount.setText(item.packedItems + " / " + item.totalItems + " packed");
            h.progress.setMax(item.totalItems);
            h.progress.setProgress(item.packedItems);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvListName, tvItemCount;
        ProgressBar progress;

        ViewHolder(View v) {
            super(v);
            tvListName = v.findViewById(R.id.tvListName);
            tvItemCount = v.findViewById(R.id.tvItemCount);
            progress = v.findViewById(R.id.progressPacking);
        }
    }
}
