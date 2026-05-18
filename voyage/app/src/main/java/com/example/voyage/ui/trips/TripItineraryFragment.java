package com.example.voyage.ui.trips;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.ItineraryAdapter;
import com.example.voyage.database.entities.ItineraryItem;
import com.example.voyage.viewmodel.ItineraryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TripItineraryFragment extends Fragment {

    private int tripId;
    private ItineraryViewModel viewModel;
    private ItineraryAdapter adapter;
    private String selectedSlot = "morning";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_itinerary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }
        if (tripId == -1) return;

        RecyclerView rv = view.findViewById(R.id.rvItinerary);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);

        viewModel = new ViewModelProvider(this).get(ItineraryViewModel.class);

        adapter = new ItineraryAdapter(item -> viewModel.deleteItem(item));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        viewModel.getItemsForTrip(tripId).observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            boolean empty = items == null || items.isEmpty();
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_itinerary_item, null);

        EditText etDay = dialogView.findViewById(R.id.etDayNumber);
        EditText etTitle = dialogView.findViewById(R.id.etActivityTitle);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etCost = dialogView.findViewById(R.id.etEstimatedCost);
        TextView slotMorning = dialogView.findViewById(R.id.slotMorning);
        TextView slotAfternoon = dialogView.findViewById(R.id.slotAfternoon);
        TextView slotEvening = dialogView.findViewById(R.id.slotEvening);

        slotMorning.setOnClickListener(v -> setSlot(slotMorning, slotAfternoon, slotEvening, "morning"));
        slotAfternoon.setOnClickListener(v -> setSlot(slotAfternoon, slotMorning, slotEvening, "afternoon"));
        slotEvening.setOnClickListener(v -> setSlot(slotEvening, slotMorning, slotAfternoon, "evening"));

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Activity")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) return;

                    int day = 1;
                    try { day = Integer.parseInt(etDay.getText().toString().trim()); } catch (Exception ignored) {}
                    double cost = 0;
                    try { cost = Double.parseDouble(etCost.getText().toString().trim()); } catch (Exception ignored) {}

                    ItineraryItem item = new ItineraryItem();
                    item.tripId = tripId;
                    item.dayNumber = day;
                    item.timeSlot = selectedSlot;
                    item.title = title;
                    item.locationName = etLocation.getText().toString().trim();
                    item.estimatedCost = cost;
                    item.orderIndex = 0;
                    viewModel.insertItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setSlot(TextView selected, TextView a, TextView b, String slot) {
        selectedSlot = slot;
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(0xFFFFFFFF);
        a.setBackgroundResource(R.drawable.bg_chip);
        a.setTextColor(requireContext().getColor(R.color.voyage_text_primary));
        b.setBackgroundResource(R.drawable.bg_chip);
        b.setTextColor(requireContext().getColor(R.color.voyage_text_primary));
    }
}
