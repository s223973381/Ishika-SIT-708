package com.example.voyage.ui.trips;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TripDetailPagerAdapter extends FragmentStateAdapter {

    private final int tripId;

    public TripDetailPagerAdapter(@NonNull FragmentActivity activity, int tripId) {
        super(activity);
        this.tripId = tripId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putInt("trip_id", tripId);
        Fragment fragment;
        switch (position) {
            case 1: fragment = new TripItineraryFragment(); break;
            case 2: fragment = new TripBudgetFragment(); break;
            case 3: fragment = new TripChecklistFragment(); break;
            case 4: fragment = new TripJournalFragment(); break;
            case 5: fragment = new TripMapFragment(); break;
            default: fragment = new TripOverviewFragment(); break;
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
