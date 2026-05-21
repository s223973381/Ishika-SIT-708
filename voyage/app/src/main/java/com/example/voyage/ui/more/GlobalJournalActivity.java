package com.example.voyage.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.GlobalJournalAdapter;
import com.example.voyage.database.entities.JournalEntry;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.ui.trips.TripDetailActivity;
import com.example.voyage.viewmodel.JournalViewModel;
import com.example.voyage.viewmodel.TripViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalJournalActivity extends AppCompatActivity {

    private GlobalJournalAdapter adapter;
    private LinearLayout emptyState;
    private LinearLayout llTripChips;
    private RecyclerView rv;
    private TextView tvEntryCount;

    private List<JournalEntry> allEntries = new ArrayList<>();
    private List<Trip> allTrips = new ArrayList<>();
    private int selectedTripId = -1; // -1 = show All

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_journal);

        emptyState = findViewById(R.id.emptyState);
        rv = findViewById(R.id.rvJournalEntries);
        tvEntryCount = findViewById(R.id.tvEntryCount);
        llTripChips = findViewById(R.id.llTripChips);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new GlobalJournalAdapter(this::onEntryClick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        JournalViewModel journalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);
        TripViewModel tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        journalViewModel.getAllEntries().observe(this, entries -> {
            allEntries = entries != null ? entries : new ArrayList<>();
            buildTripChips();
            filterEntries();
        });

        tripViewModel.getAllTrips().observe(this, trips -> {
            allTrips = trips != null ? trips : new ArrayList<>();
            adapter.setTrips(trips);
            buildTripChips();
        });
    }

    private void buildTripChips() {
        Set<Integer> tripIdsWithEntries = new HashSet<>();
        for (JournalEntry e : allEntries) tripIdsWithEntries.add(e.tripId);

        llTripChips.removeAllViews();

        llTripChips.addView(makeChip("All", selectedTripId == -1, v -> {
            selectedTripId = -1;
            buildTripChips();
            filterEntries();
        }));

        for (Trip trip : allTrips) {
            if (!tripIdsWithEntries.contains(trip.tripId)) continue;
            final int tid = trip.tripId;
            boolean selected = selectedTripId == tid;
            llTripChips.addView(makeChip(trip.destination, selected, v -> {
                selectedTripId = tid;
                buildTripChips();
                filterEntries();
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

    private void filterEntries() {
        List<JournalEntry> filtered;
        if (selectedTripId == -1) {
            filtered = allEntries;
        } else {
            filtered = new ArrayList<>();
            for (JournalEntry e : allEntries) {
                if (e.tripId == selectedTripId) filtered.add(e);
            }
        }
        adapter.setEntries(filtered);
        int count = filtered.size();
        tvEntryCount.setText(count + (count == 1 ? " entry" : " entries"));
        boolean empty = filtered.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void onEntryClick(JournalEntry entry, Trip trip) {
        if (trip == null) return;
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip_id", trip.tripId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
