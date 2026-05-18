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
import com.example.voyage.adapter.TripBudgetAdapter;
import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.database.pojo.TripSpentSummary;
import com.example.voyage.ui.trips.TripDetailActivity;
import com.example.voyage.viewmodel.TripViewModel;

import java.util.List;

public class GlobalBudgetActivity extends AppCompatActivity {

    private TripBudgetAdapter adapter;
    private TextView tvTotalSpent, tvTotalBudget;
    private LinearLayout emptyState;
    private RecyclerView rv;

    private List<Trip> currentTrips;
    private List<TripSpentSummary> currentSpent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_budget);

        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        emptyState = findViewById(R.id.emptyState);
        rv = findViewById(R.id.rvTripBudgets);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new TripBudgetAdapter(this::openTripDetail);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TripViewModel tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        tripViewModel.getAllTrips().observe(this, trips -> {
            currentTrips = trips;
            adapter.setTrips(trips);
            updateSummary();
            emptyState.setVisibility(trips == null || trips.isEmpty() ? View.VISIBLE : View.GONE);
            rv.setVisibility(trips == null || trips.isEmpty() ? View.GONE : View.VISIBLE);
        });

        AppDatabase.getInstance(this).budgetExpenseDao()
                .getAllTripSpentTotals().observe(this, summaries -> {
                    currentSpent = summaries;
                    adapter.setSpentTotals(summaries);
                    updateSummary();
                });
    }

    private void updateSummary() {
        if (currentTrips == null) return;

        double totalBudget = 0;
        for (Trip t : currentTrips) totalBudget += t.budget;

        double totalSpent = 0;
        if (currentSpent != null) {
            for (TripSpentSummary s : currentSpent) totalSpent += s.totalSpent;
        }

        tvTotalSpent.setText(String.format("$%.0f", totalSpent));
        tvTotalBudget.setText(String.format("of $%.0f total budget", totalBudget));
    }

    private void openTripDetail(Trip trip) {
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip_id", trip.tripId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
