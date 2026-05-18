package com.example.voyage.ui.trips;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.voyage.R;
import com.example.voyage.viewmodel.TripViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "trip_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        int tripId = getIntent().getIntExtra(EXTRA_TRIP_ID, -1);
        if (tripId == -1) { finish(); return; }

        TextView btnBack = findViewById(R.id.btnBack);
        TextView tvDestination = findViewById(R.id.tvDestination);
        TextView tvSubtitle = findViewById(R.id.tvTripSubtitle);
        TextView tvStyleBadge = findViewById(R.id.tvStyleBadge);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        btnBack.setOnClickListener(v -> finish());

        TripViewModel tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);
        tripViewModel.getTripById(tripId).observe(this, trip -> {
            if (trip == null) return;
            tvDestination.setText(trip.destination != null ? trip.destination : "");
            String subtitle = formatSubtitle(trip.startDate, trip.endDate, trip.days);
            tvSubtitle.setText(subtitle);
            tvStyleBadge.setText(trip.travelStyle != null ? trip.travelStyle : "");
        });

        TripDetailPagerAdapter pagerAdapter = new TripDetailPagerAdapter(this, tripId);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        String[] tabTitles = {"Overview", "Itinerary", "Budget", "Checklist", "Journal", "Map"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();
    }

    private String formatSubtitle(String start, String end, int days) {
        if (start == null || end == null || start.isEmpty()) return days + " days";
        return start + " – " + end + "  ·  " + days + " days";
    }
}
