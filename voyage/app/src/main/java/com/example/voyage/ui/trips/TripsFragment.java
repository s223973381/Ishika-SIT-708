package com.example.voyage.ui.trips;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.TripCardAdapter;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.viewmodel.TripViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class TripsFragment extends Fragment {

    private TripViewModel tripViewModel;
    private TripCardAdapter adapter;
    private RecyclerView rvTrips;
    private LinearLayout emptyState;
    private TextView tvTripCount;
    private int currentTab = 0;

    private LiveData<List<Trip>> activeLiveData;
    private final Observer<List<Trip>> listObserver = this::updateList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTrips = view.findViewById(R.id.rvTrips);
        emptyState = view.findViewById(R.id.emptyState);
        tvTripCount = view.findViewById(R.id.tvTripCount);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        FloatingActionButton fab = view.findViewById(R.id.fabCreateTrip);

        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        adapter = new TripCardAdapter(this::openTripDetail);
        rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTrips.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));

        switchTab(0);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateTripActivity.class)));
    }

    private void switchTab(int tab) {
        currentTab = tab;
        if (activeLiveData != null) {
            activeLiveData.removeObserver(listObserver);
        }
        activeLiveData = (tab == 0)
                ? tripViewModel.getUpcomingTrips()
                : tripViewModel.getCompletedTrips();
        activeLiveData.observe(getViewLifecycleOwner(), listObserver);
    }

    private void updateList(List<Trip> trips) {
        adapter.setTrips(trips);
        boolean isEmpty = trips == null || trips.isEmpty();
        rvTrips.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        int count = trips == null ? 0 : trips.size();
        tvTripCount.setText(count + (count == 1 ? " trip" : " trips"));
    }

    private void openTripDetail(Trip trip) {
        Intent intent = new Intent(requireContext(), TripDetailActivity.class);
        intent.putExtra("trip_id", trip.tripId);
        startActivity(intent);
    }
}
