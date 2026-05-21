package com.example.voyage.ui.trips;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.ChecklistAdapter;
import com.example.voyage.database.entities.ChecklistItem;
import com.example.voyage.viewmodel.ChecklistViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TripChecklistFragment extends Fragment {

    private static final Map<String, List<String>> TEMPLATES = new LinkedHashMap<>();
    static {
        TEMPLATES.put("Documents",   Arrays.asList("Passport", "Visa", "Travel Insurance", "Boarding Pass", "Hotel Confirmation", "Emergency Contacts"));
        TEMPLATES.put("Clothes",     Arrays.asList("T-Shirts", "Pants / Jeans", "Underwear", "Socks", "Jacket", "Shoes", "Swimwear", "Pyjamas"));
        TEMPLATES.put("Toiletries",  Arrays.asList("Toothbrush", "Toothpaste", "Shampoo", "Body Wash", "Deodorant", "Sunscreen", "Moisturiser"));
        TEMPLATES.put("Electronics", Arrays.asList("Phone Charger", "Power Bank", "Headphones", "Travel Adapter", "Camera", "Laptop"));
        TEMPLATES.put("Health",      Arrays.asList("Prescription Meds", "Pain Relievers", "Antihistamine", "Band-aids", "Hand Sanitiser", "Insect Repellent"));
        TEMPLATES.put("Misc",        Arrays.asList("Wallet", "Cash / Cards", "Keys", "Snacks", "Water Bottle", "Umbrella", "Book / Journal"));
    }

    private int tripId = -1;
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

        if (getArguments() != null) tripId = getArguments().getInt("trip_id", -1);
        if (tripId == -1) return;

        TextView tvCount      = view.findViewById(R.id.tvChecklistCount);
        ProgressBar progress  = view.findViewById(R.id.progressChecklist);
        RecyclerView rv       = view.findViewById(R.id.rvChecklist);
        LinearLayout empty    = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);

        viewModel = new ViewModelProvider(this).get(ChecklistViewModel.class);

        adapter = new ChecklistAdapter(new ChecklistAdapter.OnCheckListener() {
            @Override
            public void onChecked(ChecklistItem item, boolean checked) {
                viewModel.setChecked(item.checklistId, checked);
            }

            @Override
            public void onEdit(ChecklistItem item) {
                showEditBottomSheet(item);
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
            boolean isEmpty = items == null || items.isEmpty();
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            updateProgress(tvCount, progress, items);
        });

        fab.setOnClickListener(v -> showAddBottomSheet());
    }

    // ── Progress ──────────────────────────────────────────────────

    private void updateProgress(TextView tvCount, ProgressBar progress, List<ChecklistItem> items) {
        if (items == null || items.isEmpty()) {
            tvCount.setText("0 / 0");
            progress.setProgress(0);
            return;
        }
        int total = items.size();
        int checked = 0;
        for (ChecklistItem item : items) if (item.isChecked) checked++;
        tvCount.setText(checked + " / " + total);
        progress.setProgress(total > 0 ? (checked * 100) / total : 0);
    }

    // ── Bottom sheet ──────────────────────────────────────────────

    private void showAddBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_add_checklist, null);
        dialog.setContentView(sheet);

        LinearLayout llCategoryChips  = sheet.findViewById(R.id.llCategoryChips);
        ChipGroup    chipGroupPresets  = sheet.findViewById(R.id.chipGroupPresets);
        EditText     etCustomItem      = sheet.findViewById(R.id.etCustomItem);
        TextView     btnAddCustom      = sheet.findViewById(R.id.btnAddCustom);

        final String[] selectedCategory = {"Documents"};
        final TextView[] selectedChipView = {null};

        // Build category chip row
        for (String category : TEMPLATES.keySet()) {
            TextView chip = new TextView(requireContext());
            chip.setText(category);
            chip.setTextSize(13f);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
            chip.setClickable(true);
            chip.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                if (selectedChipView[0] != null) {
                    selectedChipView[0].setBackground(
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
                    selectedChipView[0].setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
                }
                chip.setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_selected));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                selectedCategory[0] = category;
                selectedChipView[0] = chip;
                loadPresets(chipGroupPresets, category);
            });

            llCategoryChips.addView(chip);
        }

        // Auto-select first category
        if (llCategoryChips.getChildCount() > 0) {
            llCategoryChips.getChildAt(0).performClick();
        }

        // Custom item — "Add" button
        btnAddCustom.setOnClickListener(v -> {
            String name = etCustomItem.getText().toString().trim();
            if (name.isEmpty()) return;
            addItem(name, selectedCategory[0]);
            dialog.dismiss();
        });

        // Custom item — keyboard "Done"
        etCustomItem.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String name = etCustomItem.getText().toString().trim();
                if (!name.isEmpty()) {
                    addItem(name, selectedCategory[0]);
                    dialog.dismiss();
                }
                return true;
            }
            return false;
        });

        dialog.show();
    }

    private void loadPresets(ChipGroup chipGroup, String category) {
        chipGroup.removeAllViews();
        List<String> presets = TEMPLATES.get(category);
        if (presets == null) return;
        for (String preset : presets) {
            Chip chip = new Chip(requireContext());
            chip.setText(preset);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> addItem(preset, category));
            chipGroup.addView(chip);
        }
    }

    private void addItem(String name, String category) {
        ChecklistItem item = new ChecklistItem();
        item.tripId       = tripId;
        item.itemName     = name;
        item.category     = category;
        item.isChecked    = false;
        item.isAiGenerated = false;
        viewModel.insertItem(item);
    }

    // ── Edit sheet ────────────────────────────────────────────────

    private void showEditBottomSheet(ChecklistItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_edit_checklist, null);
        dialog.setContentView(sheet);

        LinearLayout llCategoryChips = sheet.findViewById(R.id.llEditCategoryChips);
        EditText     etItemName      = sheet.findViewById(R.id.etEditItemName);
        TextView     btnSave         = sheet.findViewById(R.id.btnSaveEdit);

        etItemName.setText(item.itemName);
        etItemName.setSelection(etItemName.getText().length());

        final String currentCat = item.category != null ? item.category : "Misc";
        final String[] selectedCategory = {currentCat};
        final TextView[] selectedChipView = {null};

        for (String category : TEMPLATES.keySet()) {
            TextView chip = new TextView(requireContext());
            chip.setText(category);
            chip.setTextSize(13f);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            chip.setClickable(true);
            chip.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            if (category.equalsIgnoreCase(currentCat)) {
                chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_selected));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                selectedChipView[0] = chip;
            } else {
                chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
            }

            chip.setOnClickListener(v -> {
                if (selectedChipView[0] != null) {
                    selectedChipView[0].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
                    selectedChipView[0].setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
                }
                chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_selected));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                selectedCategory[0] = category;
                selectedChipView[0] = chip;
            });

            llCategoryChips.addView(chip);
        }

        btnSave.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            if (name.isEmpty()) return;
            item.itemName = name;
            item.category = selectedCategory[0];
            viewModel.updateItem(item);
            dialog.dismiss();
        });

        etItemName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String name = etItemName.getText().toString().trim();
                if (!name.isEmpty()) {
                    item.itemName = name;
                    item.category = selectedCategory[0];
                    viewModel.updateItem(item);
                    dialog.dismiss();
                }
                return true;
            }
            return false;
        });

        dialog.show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
