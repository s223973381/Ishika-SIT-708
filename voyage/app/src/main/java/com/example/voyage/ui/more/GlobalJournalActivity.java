package com.example.voyage.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.List;

public class GlobalJournalActivity extends AppCompatActivity {

    private GlobalJournalAdapter adapter;
    private LinearLayout emptyState;
    private RecyclerView rv;
    private TextView tvEntryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_journal);

        emptyState = findViewById(R.id.emptyState);
        rv = findViewById(R.id.rvJournalEntries);
        tvEntryCount = findViewById(R.id.tvEntryCount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new GlobalJournalAdapter(this::onEntryClick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        JournalViewModel journalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);
        TripViewModel tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        journalViewModel.getAllEntries().observe(this, entries -> {
            adapter.setEntries(entries);
            boolean empty = entries == null || entries.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);

            int count = entries != null ? entries.size() : 0;
            tvEntryCount.setText(count + (count == 1 ? " entry" : " entries"));
        });

        tripViewModel.getAllTrips().observe(this, trips -> adapter.setTrips(trips));
    }

    private void onEntryClick(JournalEntry entry, Trip trip) {
        if (trip == null) return;
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip_id", trip.tripId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
