package com.example.voyage.ui.more;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.PackingListItemAdapter;
import com.example.voyage.database.entities.PackingListItem;
import com.example.voyage.viewmodel.PackingListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PackingListDetailActivity extends AppCompatActivity {

    private static final Map<String, String[]> TEMPLATES = new LinkedHashMap<>();
    static {
        TEMPLATES.put("Documents", new String[]{
                "Passport", "Visa", "Travel Insurance", "Boarding Pass", "Hotel Booking Confirmation"
        });
        TEMPLATES.put("Clothes", new String[]{
                "T-Shirts", "Pants / Jeans", "Underwear", "Socks", "Jacket", "Shoes", "Swimwear"
        });
        TEMPLATES.put("Toiletries", new String[]{
                "Toothbrush", "Toothpaste", "Shampoo", "Body Wash", "Deodorant", "Sunscreen"
        });
        TEMPLATES.put("Electronics", new String[]{
                "Phone Charger", "Power Bank", "Headphones", "Travel Adapter", "Camera"
        });
        TEMPLATES.put("Medications", new String[]{
                "Prescription Medications", "Pain Relievers", "Antihistamine", "Band-aids", "Hand Sanitiser"
        });
        TEMPLATES.put("Misc", new String[]{
                "Wallet", "Cash / Cards", "Keys", "Snacks", "Water Bottle", "Umbrella"
        });
    }

    private PackingListViewModel viewModel;
    private PackingListItemAdapter adapter;
    private int listId;
    private ProgressBar progressItems;
    private TextView tvProgress;
    private LinearLayout emptyState;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list_detail);

        listId = getIntent().getIntExtra("list_id", -1);
        String listName = getIntent().getStringExtra("list_name");
        if (listId == -1) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(PackingListViewModel.class);

        ((TextView) findViewById(R.id.tvListTitle)).setText(listName != null ? listName : "Packing List");
        progressItems = findViewById(R.id.progressItems);
        tvProgress = findViewById(R.id.tvProgress);
        emptyState = findViewById(R.id.emptyState);
        rv = findViewById(R.id.rvItems);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDeleteList).setOnClickListener(v -> confirmDeleteList(listName));

        adapter = new PackingListItemAdapter(new PackingListItemAdapter.Listener() {
            @Override
            public void onPacked(PackingListItem item, boolean packed) {
                viewModel.setPacked(item.itemId, packed);
            }

            @Override
            public void onDelete(PackingListItem item) {
                viewModel.deleteItem(item.itemId);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);

        viewModel.getItemsForList(listId).observe(this, items -> {
            adapter.setItems(items);
            boolean empty = items == null || items.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            updateProgress(items);
        });

        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> showAddItemDialog());

        setupQuickAddChips();
    }

    private void setupQuickAddChips() {
        findViewById(R.id.chipDocuments).setOnClickListener(v -> addTemplate("Documents"));
        findViewById(R.id.chipClothes).setOnClickListener(v -> addTemplate("Clothes"));
        findViewById(R.id.chipToiletries).setOnClickListener(v -> addTemplate("Toiletries"));
        findViewById(R.id.chipElectronics).setOnClickListener(v -> addTemplate("Electronics"));
        findViewById(R.id.chipMedications).setOnClickListener(v -> addTemplate("Medications"));
        findViewById(R.id.chipMisc).setOnClickListener(v -> addTemplate("Misc"));
    }

    private void addTemplate(String category) {
        String[] itemNames = TEMPLATES.get(category);
        if (itemNames == null) return;

        List<PackingListItem> currentItems = adapter.getItems();
        List<PackingListItem> toAdd = new ArrayList<>();
        for (String name : itemNames) {
            boolean alreadyExists = false;
            for (PackingListItem existing : currentItems) {
                if (existing.itemName.equalsIgnoreCase(name)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) {
                PackingListItem item = new PackingListItem();
                item.listId = listId;
                item.itemName = name;
                item.category = category;
                item.isPacked = false;
                toAdd.add(item);
            }
        }

        if (toAdd.isEmpty()) {
            Toast.makeText(this, category + " items already added", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.insertAll(toAdd);
            Toast.makeText(this, toAdd.size() + " " + category + " items added", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_packing_item, null);
        EditText etName = dialogView.findViewById(R.id.etItemName);
        EditText etCategory = dialogView.findViewById(R.id.etItemCategory);

        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    PackingListItem item = new PackingListItem();
                    item.listId = listId;
                    item.itemName = name;
                    item.category = etCategory.getText().toString().trim();
                    item.isPacked = false;
                    viewModel.insertItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteList(String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete List")
                .setMessage("Delete \"" + name + "\" and all its items?")
                .setPositiveButton("Delete", (d, w) -> {
                    viewModel.deleteList(listId);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProgress(List<PackingListItem> items) {
        if (items == null || items.isEmpty()) {
            tvProgress.setText("0 / 0");
            progressItems.setProgress(0);
            return;
        }
        int total = items.size();
        int packed = 0;
        for (PackingListItem item : items) {
            if (item.isPacked) packed++;
        }
        tvProgress.setText(packed + " / " + total);
        progressItems.setMax(total);
        progressItems.setProgress(packed);
    }
}
