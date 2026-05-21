package com.example.voyage.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapFragment extends Fragment {

    private static final int ZOOM_MIN = 10;
    private static final int ZOOM_MAX = 16;
    private static final int TILES_WARN_THRESHOLD = 6000;

    private MapView mapView;
    private EditText etSearch;
    private TextView tvMapSuggestion, tvMapStatus;
    private RecyclerView rvNearbyPlaces;

    private NearbyPlaceAdapter adapter;
    private final List<NearbyPlace> nearbyPlaces = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private MyLocationNewOverlay locationOverlay;
    private ActivityResultLauncher<String> locationPermLauncher;

    private CacheManager cacheManager;
    private boolean isDownloading = false;

    private double userLat = 0, userLng = 0;
    private boolean locationReady = false;
    private String pendingFilter = null;
    private String currentFilter = null;

    private TextView chipFood, chipHotels, chipAttractions, chipTransport, chipEmergency;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        locationPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) fetchLocationAndCenter();
                    else showStatus("Location permission needed to explore nearby places");
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(5.0);

        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        cacheManager = new CacheManager(mapView);

        etSearch = view.findViewById(R.id.etSearch);
        tvMapSuggestion = view.findViewById(R.id.tvMapSuggestion);
        tvMapStatus = view.findViewById(R.id.tvMapStatus);
        rvNearbyPlaces = view.findViewById(R.id.rvNearbyPlaces);

        chipFood = view.findViewById(R.id.chipFood);
        chipHotels = view.findViewById(R.id.chipHotels);
        chipAttractions = view.findViewById(R.id.chipAttractions);
        chipTransport = view.findViewById(R.id.chipTransport);
        chipEmergency = view.findViewById(R.id.chipEmergency);

        adapter = new NearbyPlaceAdapter(nearbyPlaces, place -> {
            mapView.getController().animateTo(new GeoPoint(place.lat, place.lng));
            mapView.getController().setZoom(17.5);
        });
        rvNearbyPlaces.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNearbyPlaces.setAdapter(adapter);

        view.findViewById(R.id.btnMapBack).setOnClickListener(v ->
                requireActivity().onBackPressed());

        view.findViewById(R.id.btnZoomIn).setOnClickListener(v ->
                mapView.getController().zoomIn());

        view.findViewById(R.id.btnZoomOut).setOnClickListener(v ->
                mapView.getController().zoomOut());

        view.findViewById(R.id.btnDownloadArea).setOnClickListener(v ->
                showDownloadConfirmation());

        chipFood.setOnClickListener(v -> selectFilter("Food"));
        chipHotels.setOnClickListener(v -> selectFilter("Hotels"));
        chipAttractions.setOnClickListener(v -> selectFilter("Attractions"));
        chipTransport.setOnClickListener(v -> selectFilter("Transport"));
        chipEmergency.setOnClickListener(v -> selectFilter("Emergency"));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                dismissKeyboard();
                filterLoadedPlaces(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        applyContextSuggestion();
        checkAndFetchLocation();
    }

    // ── Context suggestion ────────────────────────────────────────

    private void applyContextSuggestion() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String suggestion;
        String filter;

        if (hour >= 6 && hour < 11) {
            suggestion = "Good morning! ☕ Find breakfast spots";
            filter = "Food";
        } else if (hour >= 11 && hour < 14) {
            suggestion = "Lunchtime! 🍽 Find restaurants nearby";
            filter = "Food";
        } else if (hour >= 14 && hour < 18) {
            suggestion = "Afternoon ⭐ Explore local attractions";
            filter = "Attractions";
        } else if (hour >= 18 && hour < 21) {
            suggestion = "Evening 🍽 Find a dinner spot";
            filter = "Food";
        } else {
            suggestion = "🚨 Find emergency services or transit";
            filter = "Emergency";
        }

        tvMapSuggestion.setText(suggestion);
        pendingFilter = filter;
    }

    // ── Location ──────────────────────────────────────────────────

    private void checkAndFetchLocation() {
        boolean fine = ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            fetchLocationAndCenter();
        } else {
            locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndCenter() {
        showStatus("Finding your location...");
        locationOverlay.enableMyLocation();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), location -> {
                    if (!isAdded()) return;
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                        locationReady = true;

                        GeoPoint geoPoint = new GeoPoint(userLat, userLng);
                        mapView.getController().animateTo(geoPoint);
                        mapView.getController().setZoom(16.0);

                        if (pendingFilter != null) {
                            selectFilter(pendingFilter);
                            pendingFilter = null;
                        } else {
                            showStatus("Tap a filter to explore places nearby");
                        }
                    } else {
                        showStatus("Location unavailable — tap a filter after moving");
                    }
                });
    }

    // ── Filter chips ──────────────────────────────────────────────

    private void selectFilter(String filter) {
        if (!locationReady) {
            pendingFilter = filter;
            showStatus("Waiting for location...");
            return;
        }

        currentFilter = filter;
        updateChipUI(filter);
        showStatus("Searching for " + filter.toLowerCase() + "...");
        rvNearbyPlaces.setVisibility(View.GONE);

        mapView.getOverlays().clear();
        mapView.getOverlays().add(locationOverlay);

        OverpassClient.fetchNearby(userLat, userLng, filter, new OverpassClient.Callback() {
            @Override
            public void onResult(List<NearbyPlace> places) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    nearbyPlaces.clear();
                    nearbyPlaces.addAll(places);
                    adapter.notifyDataSetChanged();
                    addMarkersToMap(places);

                    if (places.isEmpty()) {
                        showStatus("No " + filter.toLowerCase() + " places found nearby");
                    } else {
                        tvMapStatus.setVisibility(View.GONE);
                        rvNearbyPlaces.setVisibility(View.VISIBLE);
                        tvMapSuggestion.setText(places.size() + " "
                                + filter.toLowerCase() + " spots found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        showStatus("Could not load places — check your connection"));
            }
        });
    }

    private void addMarkersToMap(List<NearbyPlace> places) {
        for (NearbyPlace place : places) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(place.lat, place.lng));
            marker.setTitle(place.emoji + " " + place.name);
            marker.setSubDescription(place.getFormattedDistance() + " away");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    // ── Search ────────────────────────────────────────────────────

    private void filterLoadedPlaces(String query) {
        if (query.isEmpty()) return;

        if (nearbyPlaces.isEmpty()) {
            showStatus("Select a filter category first, then search");
            return;
        }

        List<NearbyPlace> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (NearbyPlace p : nearbyPlaces) {
            if (p.name.toLowerCase().contains(lower)) filtered.add(p);
        }

        if (!filtered.isEmpty()) {
            nearbyPlaces.clear();
            nearbyPlaces.addAll(filtered);
            adapter.notifyDataSetChanged();
            mapView.getController().animateTo(
                    new GeoPoint(filtered.get(0).lat, filtered.get(0).lng));
            tvMapSuggestion.setText(filtered.size() + " result(s) for \"" + query + "\"");
        } else {
            showStatus("No results for \"" + query + "\" — try a different term");
        }
    }

    // ── Offline download ──────────────────────────────────────────

    private void showDownloadConfirmation() {
        if (isDownloading) {
            Toast.makeText(requireContext(), "Download already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        BoundingBox box = mapView.getBoundingBox();
        int tileCount = cacheManager.possibleTilesInArea(box, ZOOM_MIN, ZOOM_MAX);

        String sizeEstimateKB = String.valueOf(tileCount * 15);
        String msg = "Save the current map view for offline use?\n\n"
                + "Zoom levels: " + ZOOM_MIN + " – " + ZOOM_MAX + "\n"
                + "Estimated tiles: " + tileCount + " (~" + sizeEstimateKB + " KB)";

        if (tileCount > TILES_WARN_THRESHOLD) {
            msg += "\n\n⚠ Large area — this may take a while and use significant storage.";
        }

        if (tileCount == 0) {
            Toast.makeText(requireContext(), "Zoom in to an area first, then download", Toast.LENGTH_SHORT).show();
            return;
        }

        final BoundingBox finalBox = box;
        final int finalCount = tileCount;
        new AlertDialog.Builder(requireContext())
                .setTitle("💾 Save for Offline")
                .setMessage(msg)
                .setPositiveButton("Download", (d, w) -> startAreaDownload(finalBox, finalCount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startAreaDownload(BoundingBox box, int totalTiles) {
        if (isDownloading) return;
        isDownloading = true;

        // Build inline progress dialog
        int pad = (int) (20 * requireContext().getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        TextView tvMsg = new TextView(requireContext());
        tvMsg.setText("Downloading map tiles…");
        tvMsg.setTextSize(14);
        tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
        layout.addView(tvMsg);

        ProgressBar progressBar = new ProgressBar(
                requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(totalTiles);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pbParams.topMargin = pad / 2;
        progressBar.setLayoutParams(pbParams);
        layout.addView(progressBar);

        TextView tvCount = new TextView(requireContext());
        tvCount.setText("0 / " + totalTiles + " tiles");
        tvCount.setTextSize(11);
        tvCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_hint));
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.topMargin = pad / 4;
        tvCount.setLayoutParams(countParams);
        layout.addView(tvCount);

        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
                .setTitle("💾 Saving Offline Map")
                .setView(layout)
                .setCancelable(false)
                .create();
        progressDialog.show();

        cacheManager.downloadAreaAsync(requireContext(), box, ZOOM_MIN, ZOOM_MAX,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void downloadStarted() {}

                    @Override
                    public void setPossibleTilesInArea(int total) {}

                    @Override
                    public void updateProgress(int progress, int currentZoomLevel,
                                               int zoomMin, int zoomMax) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setProgress(progress);
                            tvCount.setText(progress + " / " + totalTiles
                                    + " tiles  (zoom " + currentZoomLevel + ")");
                        });
                    }

                    @Override
                    public void onTaskComplete() {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            isDownloading = false;
                            saveGlobalDownloadTimestamp();
                            Toast.makeText(requireContext(),
                                    "✓ Map saved for offline use", Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onTaskFailed(int errors) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            isDownloading = false;
                            saveGlobalDownloadTimestamp();
                            String msg = errors == 0
                                    ? "✓ Map saved for offline use"
                                    : "✓ Map saved (" + errors + " tiles unavailable in this area)";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void saveGlobalDownloadTimestamp() {
        requireContext().getSharedPreferences("voyage_offline", android.content.Context.MODE_PRIVATE)
                .edit()
                .putLong("global_map_downloaded_at", System.currentTimeMillis())
                .apply();
    }

    // ── Chip UI ───────────────────────────────────────────────────

    private void updateChipUI(String selectedFilter) {
        resetChip(chipFood);
        resetChip(chipHotels);
        resetChip(chipAttractions);
        resetChip(chipTransport);
        resetChip(chipEmergency);

        TextView selected;
        switch (selectedFilter) {
            case "Food":        selected = chipFood;        break;
            case "Hotels":      selected = chipHotels;      break;
            case "Attractions": selected = chipAttractions; break;
            case "Transport":   selected = chipTransport;   break;
            case "Emergency":   selected = chipEmergency;   break;
            default:            return;
        }
        selected.setBackground(ContextCompat.getDrawable(requireContext(),
                R.drawable.bg_chip_selected));
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetChip(TextView chip) {
        chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.voyage_text_primary));
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void showStatus(String text) {
        if (tvMapStatus != null) {
            tvMapStatus.setText(text);
            tvMapStatus.setVisibility(View.VISIBLE);
        }
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && etSearch != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationOverlay != null) locationOverlay.disableMyLocation();
        mapView = null;
    }
}
