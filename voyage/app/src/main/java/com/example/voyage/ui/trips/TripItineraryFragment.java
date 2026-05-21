package com.example.voyage.ui.trips;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.ItineraryAdapter;
import com.example.voyage.ai.AiRepository;
import com.example.voyage.database.entities.ItineraryItem;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.ui.island.SmartIsland;
import com.example.voyage.util.AppContext;
import com.example.voyage.viewmodel.ItineraryViewModel;
import com.example.voyage.viewmodel.TripViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripItineraryFragment extends Fragment {

    private int tripId;
    private ItineraryViewModel viewModel;
    private TripViewModel tripViewModel;
    private ItineraryAdapter adapter;
    private Trip currentTrip;
    private boolean hasItems = false;
    private String selectedSlot = "morning";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        RecyclerView rv                         = view.findViewById(R.id.rvItinerary);
        View emptyState                         = view.findViewById(R.id.emptyState);
        FloatingActionButton fab                = view.findViewById(R.id.fabAddItem);
        ExtendedFloatingActionButton fabAi      = view.findViewById(R.id.fabGenerateAi);

        viewModel     = new ViewModelProvider(this).get(ItineraryViewModel.class);
        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        adapter = new ItineraryAdapter(item -> viewModel.deleteItem(item));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        tripViewModel.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> currentTrip = trip);

        viewModel.getItemsForTrip(tripId).observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            hasItems = items != null && !items.isEmpty();
            rv.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            emptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        });

        fab.setOnClickListener(v -> showAddDialog());
        fabAi.setOnClickListener(v -> confirmAndGenerate());
    }

    // ── AI generation ─────────────────────────────────────────────

    private void confirmAndGenerate() {
        if (hasItems) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Generate AI Itinerary")
                    .setMessage("This will replace your existing itinerary with an AI-generated plan. Continue?")
                    .setPositiveButton("Replace", (d, w) -> runGeneration())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            runGeneration();
        }
    }

    @SuppressWarnings("deprecation")
    private void runGeneration() {
        if (currentTrip == null) {
            Toast.makeText(requireContext(), "Trip data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("✨ Generating your itinerary…\nThis may take 15–30 seconds.");
        progress.setCancelable(false);
        progress.show();

        String prompt = buildPrompt(currentTrip);
        AiRepository.Mode mode = AiRepository.fromString(
                currentTrip.aiMode != null ? currentTrip.aiMode : "auto");

        AiRepository.sendMessage(requireContext(), prompt, mode, new AiRepository.AiCallback() {
            @Override
            public void onResponse(String text, String usedMode) {
                List<ItineraryItem> items = parseJson(text, tripId,
                        currentTrip != null ? currentTrip.days : 3);
                mainHandler.post(() -> {
                    progress.dismiss();
                    if (!isAdded()) return;
                    if (items.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Couldn't parse AI response. Try again.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    viewModel.replaceItems(tripId, items);
                    Toast.makeText(requireContext(),
                            "✨ " + items.size() + " activities generated!", Toast.LENGTH_SHORT).show();
                    // Island: itinerary ready
                    String dest = currentTrip != null ? currentTrip.destination : "your trip";
                    SmartIsland.show(requireActivity(), new SmartIsland.Config()
                            .icon("✨").title(items.size() + " activities planned!")
                            .subtitle(currentTrip != null ? currentTrip.days + " days in " + dest : dest)
                            .autoDismiss(6000));
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    progress.dismiss();
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "AI error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String buildPrompt(Trip trip) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a ").append(trip.days).append("-day travel itinerary for ")
          .append(trip.destination).append(".\n");
        if (trip.travelStyle != null && !trip.travelStyle.isEmpty()) {
            sb.append("Travel style: ").append(trip.travelStyle).append(".\n");
        }
        if (trip.budget > 0) {
            sb.append("Total budget: AUD ").append((int) trip.budget).append(".\n");
        }
        if (!AppContext.weatherDescription.isEmpty()) {
            sb.append("Current weather: ").append(AppContext.weatherDescription).append(".\n");
        }
        sb.append("\nReply with ONLY a valid JSON array. No explanation, no markdown, no code blocks.\n")
          .append("Format: [{\"day\":1,\"slot\":\"morning\",\"title\":\"Activity Name\",")
          .append("\"location\":\"Place Name\",\"cost\":10.0}, ...]\n")
          .append("Rules:\n")
          .append("- slot must be exactly: morning, afternoon, or evening\n")
          .append("- 3 activities per day (one per slot)\n")
          .append("- cost is a number in AUD\n")
          .append("- location is the specific venue or area name\n")
          .append("- title is short (max 6 words)\n")
          .append("- Total ").append(trip.days * 3).append(" objects in the array");
        return sb.toString();
    }

    private List<ItineraryItem> parseJson(String raw, int tripId, int days) {
        List<ItineraryItem> items = new ArrayList<>();
        try {
            int start = raw.indexOf('[');
            int end   = raw.lastIndexOf(']');
            if (start < 0 || end <= start) return items;

            JSONArray arr = new JSONArray(raw.substring(start, end + 1));
            String[] validSlots = {"morning", "afternoon", "evening"};
            int maxDay = Math.max(days, 1);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                ItineraryItem item = new ItineraryItem();
                item.tripId        = tripId;
                item.dayNumber     = Math.min(Math.max(obj.optInt("day", 1), 1), maxDay);
                item.estimatedCost = obj.optDouble("cost", 0.0);
                item.title         = obj.optString("title", "Activity");
                item.locationName  = obj.optString("location", "");
                item.orderIndex    = i;

                String slot = obj.optString("slot", "morning").toLowerCase().trim();
                boolean valid = false;
                for (String s : validSlots) { if (s.equals(slot)) { valid = true; break; } }
                item.timeSlot = valid ? slot : "morning";

                items.add(item);
            }
        } catch (Exception ignored) {}
        return items;
    }

    // ── Manual add dialog ─────────────────────────────────────────

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_itinerary_item, null);

        EditText etDay      = dialogView.findViewById(R.id.etDayNumber);
        EditText etTitle    = dialogView.findViewById(R.id.etActivityTitle);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etCost     = dialogView.findViewById(R.id.etEstimatedCost);
        TextView slotMorning   = dialogView.findViewById(R.id.slotMorning);
        TextView slotAfternoon = dialogView.findViewById(R.id.slotAfternoon);
        TextView slotEvening   = dialogView.findViewById(R.id.slotEvening);

        slotMorning.setOnClickListener(v   -> setSlot(slotMorning,   slotAfternoon, slotEvening,   "morning"));
        slotAfternoon.setOnClickListener(v -> setSlot(slotAfternoon, slotMorning,   slotEvening,   "afternoon"));
        slotEvening.setOnClickListener(v   -> setSlot(slotEvening,   slotMorning,   slotAfternoon, "evening"));

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
                    item.tripId        = tripId;
                    item.dayNumber     = day;
                    item.timeSlot      = selectedSlot;
                    item.title         = title;
                    item.locationName  = etLocation.getText().toString().trim();
                    item.estimatedCost = cost;
                    item.orderIndex    = 0;
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
