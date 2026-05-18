package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.ChecklistItem;

import java.util.ArrayList;
import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    public interface OnCheckListener {
        void onChecked(ChecklistItem item, boolean checked);
        void onDelete(ChecklistItem item);
    }

    private List<ChecklistItem> items = new ArrayList<>();
    private OnCheckListener listener;

    public ChecklistAdapter(OnCheckListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ChecklistItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ChecklistItem item = items.get(position);

        h.tvItemName.setText(item.itemName != null ? item.itemName : "");
        h.tvCategory.setText(item.category != null ? item.category : "");

        h.cbChecked.setOnCheckedChangeListener(null);
        h.cbChecked.setChecked(item.isChecked);

        float alpha = item.isChecked ? 0.5f : 1f;
        h.tvItemName.setAlpha(alpha);

        h.cbChecked.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onChecked(item, checked);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecked;
        TextView tvItemName, tvCategory, btnDelete;

        ViewHolder(View v) {
            super(v);
            cbChecked = v.findViewById(R.id.cbChecked);
            tvItemName = v.findViewById(R.id.tvItemName);
            tvCategory = v.findViewById(R.id.tvItemCategory);
            btnDelete = v.findViewById(R.id.btnDeleteItem);
        }
    }
}
