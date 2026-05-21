package com.example.voyage.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.PackingListAdapter;
import com.example.voyage.database.entities.PackingList;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.database.pojo.PackingListWithCount;
import com.example.voyage.viewmodel.PackingListViewModel;
import com.example.voyage.viewmodel.TripViewModel;

import java.util.ArrayList;
import java.util.List;

public class PackingListsActivity extends AppCompatActivity {

    private PackingListViewModel viewModel;
    private TripViewModel tripViewModel;
    private PackingListAdapter adapter;
    private LinearLayout emptyState;
    private LinearLayout llTripChips;
    private RecyclerView rv;

    private List<PackingListWithCount> allLists = new ArrayList<>();
    private List<Trip> allTrips = new ArrayList<>();
    private int selectedTripId = -1; // -1 = show All

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_lists);

        viewModel = new ViewModelProvider(this).get(PackingListViewModel.class);
        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        emptyState = findViewById(R.id.emptyState);
        rv = findViewById(R.id.rvPackingLists);
        llTripChips = findViewById(R.id.llTripChips);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewList).setOnClickListener(v -> showCreateListDialog());

        adapter = new PackingListAdapter(item -> {
            Intent intent = new Intent(this, PackingListDetailActivity.class);
            intent.putExtra("list_id", item.listId);
            intent.putExtra("list_name", item.listName);
            startActivity(intent);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        viewModel.getAllLists().observe(this, lists -> {
            allLists = lists != null ? lists : new ArrayList<>();
            filterLists();
        });

        tripViewModel.getAllTrips().observe(this, trips -> {
            allTrips = trips != null ? trips : new ArrayList<>();
            buildTripChips();
        });
    }

    private void buildTripChips() {
        llTripChips.removeAllViews();

        llTripChips.addView(makeChip("All", selectedTripId == -1, v -> {
            selectedTripId = -1;
            buildTripChips();
            filterLists();
        }));

        for (Trip trip : allTrips) {
            final int tid = trip.tripId;
            boolean selected = selectedTripId == tid;
            llTripChips.addView(makeChip(trip.destination, selected, v -> {
                selectedTripId = tid;
                buildTripChips();
                filterLists();
            }));
        }
    }

    private TextView makeChip(String label, boolean selected, View.OnClickListener onClick) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMarginEnd(dp(8));
        chip.setLayoutParams(lp);
        chip.setText(label);
        chip.setTextSize(13f);
        chip.setPadding(dp(14), dp(6), dp(14), dp(6));
        chip.setBackground(ContextCompat.getDrawable(this,
                selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip));
        chip.setTextColor(ContextCompat.getColor(this,
                selected ? android.R.color.white : R.color.voyage_text_secondary));
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(onClick);
        return chip;
    }

    private void filterLists() {
        List<PackingListWithCount> filtered;
        if (selectedTripId == -1) {
            filtered = allLists;
        } else {
            filtered = new ArrayList<>();
            for (PackingListWithCount item : allLists) {
                if (item.tripId != null && item.tripId == selectedTripId) {
                    filtered.add(item);
                }
            }
        }
        adapter.setLists(filtered);
        boolean empty = filtered.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void showCreateListDialog() {
        EditText etName = new EditText(this);
        etName.setHint("e.g. Beach Trip, Business Travel");
        etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etName.setPadding(60, 32, 60, 16);

        new AlertDialog.Builder(this)
                .setTitle("New Packing List")
                .setView(etName)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    PackingList list = new PackingList();
                    list.listName = name;
                    list.createdAt = System.currentTimeMillis();
                    list.tripId = (selectedTripId == -1) ? null : selectedTripId;
                    viewModel.insertList(list, listId -> runOnUiThread(() -> {
                        Intent intent = new Intent(this, PackingListDetailActivity.class);
                        intent.putExtra("list_id", listId);
                        intent.putExtra("list_name", name);
                        startActivity(intent);
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
