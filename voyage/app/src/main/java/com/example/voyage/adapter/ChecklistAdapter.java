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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private static final List<String> CATEGORY_ORDER = Arrays.asList(
            "Documents", "Clothes", "Toiletries", "Electronics", "Health", "Misc", "Other"
    );

    public interface OnCheckListener {
        void onChecked(ChecklistItem item, boolean checked);
        void onEdit(ChecklistItem item);
        void onDelete(ChecklistItem item);
    }

    private List<Object> displayList = new ArrayList<>();
    private final OnCheckListener listener;

    public ChecklistAdapter(OnCheckListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ChecklistItem> items) {
        displayList = buildDisplayList(items);
        notifyDataSetChanged();
    }

    private List<Object> buildDisplayList(List<ChecklistItem> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        Map<String, List<ChecklistItem>> grouped = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) grouped.put(cat, new ArrayList<>());

        for (ChecklistItem item : items) {
            String cat = normalize(item.category);
            grouped.get(grouped.containsKey(cat) ? cat : "Other").add(item);
        }

        List<Object> result = new ArrayList<>();
        for (Map.Entry<String, List<ChecklistItem>> entry : grouped.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.add(entry.getKey());
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    private String normalize(String category) {
        if (category == null || category.trim().isEmpty()) return "Other";
        for (String cat : CATEGORY_ORDER) {
            if (cat.equalsIgnoreCase(category.trim())) return cat;
        }
        return "Other";
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_checklist_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = inflater.inflate(R.layout.item_checklist_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText((String) displayList.get(position));
            return;
        }

        ChecklistItem item = (ChecklistItem) displayList.get(position);
        ItemViewHolder h = (ItemViewHolder) holder;

        h.tvItemName.setText(item.itemName != null ? item.itemName : "");
        h.tvCategory.setText(item.category != null ? item.category : "");

        h.cbChecked.setOnCheckedChangeListener(null);
        h.cbChecked.setChecked(item.isChecked);
        h.tvItemName.setAlpha(item.isChecked ? 0.45f : 1f);
        h.tvItemName.setPaintFlags(item.isChecked
                ? h.tvItemName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                : h.tvItemName.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        h.cbChecked.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onChecked(item, checked);
        });
        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(View v) {
            super(v);
            tvHeader = (TextView) v;
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecked;
        TextView tvItemName, tvCategory, btnEdit, btnDelete;

        ItemViewHolder(View v) {
            super(v);
            cbChecked  = v.findViewById(R.id.cbChecked);
            tvItemName = v.findViewById(R.id.tvItemName);
            tvCategory = v.findViewById(R.id.tvItemCategory);
            btnEdit    = v.findViewById(R.id.btnEditItem);
            btnDelete  = v.findViewById(R.id.btnDeleteItem);
        }
    }
}
