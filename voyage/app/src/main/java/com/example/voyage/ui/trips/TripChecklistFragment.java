package com.example.voyage.ui.trips;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.ChecklistAdapter;
import com.example.voyage.database.entities.ChecklistItem;
import com.example.voyage.viewmodel.ChecklistViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TripChecklistFragment extends Fragment {

    private int tripId;
    private ChecklistViewModel viewModel;
    private ChecklistAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }
        if (tripId == -1) return;

        TextView tvCount = view.findViewById(R.id.tvChecklistCount);
        ProgressBar progress = view.findViewById(R.id.progressChecklist);
        RecyclerView rv = view.findViewById(R.id.rvChecklist);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);

        viewModel = new ViewModelProvider(this).get(ChecklistViewModel.class);

        adapter = new ChecklistAdapter(new ChecklistAdapter.OnCheckListener() {
            @Override
            public void onChecked(ChecklistItem item, boolean checked) {
                viewModel.setChecked(item.checklistId, checked);
            }

            @Override
            public void onDelete(ChecklistItem item) {
                viewModel.deleteItem(item.checklistId);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        viewModel.getItemsForTrip(tripId).observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            boolean empty = items == null || items.isEmpty();
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            updateProgress(tvCount, progress, items);
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    private void updateProgress(TextView tvCount, ProgressBar progress, List<ChecklistItem> items) {
        if (items == null || items.isEmpty()) {
            tvCount.setText("0 / 0");
            progress.setProgress(0);
            return;
        }
        int total = items.size();
        int checked = 0;
        for (ChecklistItem item : items) {
            if (item.isChecked) checked++;
        }
        tvCount.setText(checked + " / " + total);
        progress.setProgress(total > 0 ? (checked * 100) / total : 0);
    }

    private void showAddDialog() {
        final EditText etName = new EditText(requireContext());
        etName.setHint("Item name (e.g. Passport)");
        etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etName.setPadding(48, 24, 48, 8);

        final EditText etCategory = new EditText(requireContext());
        etCategory.setHint("Category (e.g. Documents)");
        etCategory.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etCategory.setPadding(48, 8, 48, 24);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etName);
        layout.addView(etCategory);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Checklist Item")
                .setView(layout)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    ChecklistItem item = new ChecklistItem();
                    item.tripId = tripId;
                    item.itemName = name;
                    item.category = etCategory.getText().toString().trim();
                    item.isChecked = false;
                    item.isAiGenerated = false;
                    viewModel.insertItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
