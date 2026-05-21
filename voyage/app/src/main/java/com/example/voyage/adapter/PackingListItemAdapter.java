package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.PackingListItem;

import java.util.ArrayList;
import java.util.List;

public class PackingListItemAdapter extends RecyclerView.Adapter<PackingListItemAdapter.ViewHolder> {

    public interface Listener {
        void onPacked(PackingListItem item, boolean packed);
        void onDelete(PackingListItem item);
    }

    private List<PackingListItem> items = new ArrayList<>();
    private final Listener listener;

    public PackingListItemAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PackingListItem> items) {
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
        PackingListItem item = items.get(position);

        h.tvItemName.setText(item.itemName != null ? item.itemName : "");
        h.tvCategory.setText(item.category != null ? item.category : "");
        h.tvCategory.setVisibility(
                item.category != null && !item.category.isEmpty() ? View.VISIBLE : View.GONE);

        h.cbChecked.setOnCheckedChangeListener(null);
        h.cbChecked.setChecked(item.isPacked);
        h.tvItemName.setAlpha(item.isPacked ? 0.5f : 1f);

        h.cbChecked.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onPacked(item, checked);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    public List<PackingListItem> getItems() {
        return items;
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
