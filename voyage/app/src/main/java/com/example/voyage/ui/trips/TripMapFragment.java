package com.example.voyage.ui.trips;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.OfflineDownloadDao;
import com.example.voyage.database.entities.ItineraryItem;
import com.example.voyage.database.entities.OfflineDownload;
import com.example.voyage.util.NominatimClient;
import com.example.voyage.viewmodel.ItineraryViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripMapFragment extends Fragment {

    private static final int ZOOM_MIN = 8;
    private static final int ZOOM_MAX = 16;
    private static final int[] DAY_COLORS = {
        Color.parseColor("#8B0000"),
        Color.parseColor("#787319"),
        Color.parseColor("#1565C0"),
        Color.parseColor("#2E7D32"),
        Color.parseColor("#E65100"),
    };

    private int tripId = -1;
    private MapView mapView;
    private TextView tvStatus;
    private TextView btnDownloadTrip;
    private RecyclerView rvStops;
    private TripStopAdapter stopAdapter;
    private final List<ItineraryItem> plottedItems = new ArrayList<>();

    private ItineraryViewModel viewModel;
    private OfflineDownloadDao offlineDownloadDao;
    private CacheManager cacheManager;
    private BoundingBox tripBoundingBox;

    private final Set<Integer> scheduledGeocode = new HashSet<>();
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private boolean isDownloading = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Point osmdroid at app-specific external storage — no special permission needed
        // on any Android version, and ensures OfflineDataActivity reads the same path.
        File extDir = requireContext().getExternalFilesDir(null);
        if (extDir == null) extDir = requireContext().getFilesDir();
        Configuration.getInstance().setOsmdroidBasePath(new File(extDir, "osmdroid"));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        if (getArguments() != null) tripId = getArguments().getInt("trip_id", -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(5.0);

        cacheManager = new CacheManager(mapView);
        offlineDownloadDao = AppDatabase.getInstance(requireContext()).offlineDownloadDao();

        view.findViewById(R.id.btnZoomIn).setOnClickListener(v ->
                mapView.getController().zoomIn());
        view.findViewById(R.id.btnZoomOut).setOnClickListener(v ->
                mapView.getController().zoomOut());

        btnDownloadTrip = view.findViewById(R.id.btnDownloadTrip);
        btnDownloadTrip.setOnClickListener(v -> showDownloadConfirmation());

        tvStatus = view.findViewById(R.id.tvTripMapStatus);
        rvStops = view.findViewById(R.id.rvTripStops);

        stopAdapter = new TripStopAdapter(plottedItems, item -> {
            mapView.getController().animateTo(new GeoPoint(item.latitude, item.longitude));
            mapView.getController().setZoom(16.0);
        });
        rvStops.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvStops.setAdapter(stopAdapter);

        if (tripId == -1) {
            showStatus("No trip loaded");
            return;
        }

        viewModel = new ViewModelProvider(this).get(ItineraryViewModel.class);
        viewModel.getItemsForTrip(tripId).observe(getViewLifecycleOwner(), this::onItemsChanged);
        showStatus("Loading itinerary...");
        checkExistingDownload();
    }

    // ── Offline download ──────────────────────────────────────────

    private void checkExistingDownload() {
        dbExecutor.execute(() -> {
            OfflineDownload dl = offlineDownloadDao.getDownloadForTripSync(tripId);
            if (dl != null && dl.mapSaved && isAdded()) {
                requireActivity().runOnUiThread(this::markButtonAsDownloaded);
            }
        });
    }

    private void showDownloadConfirmation() {
        if (isDownloading) {
            Toast.makeText(requireContext(), "Download already in progress", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tripBoundingBox == null) {
            Toast.makeText(requireContext(),
                    "Add itinerary stops first so we know what area to download",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int tileCount = cacheManager.possibleTilesInArea(tripBoundingBox, ZOOM_MIN, ZOOM_MAX);
        String sizeKB = String.valueOf(tileCount * 15);
        String msg = "Download map tiles for this trip's area for offline use?\n\n"
                + "Zoom levels: " + ZOOM_MIN + " – " + ZOOM_MAX + "\n"
                + "Estimated tiles: " + tileCount + " (~" + sizeKB + " KB)";

        new AlertDialog.Builder(requireContext())
                .setTitle("💾 Save Trip Map Offline")
                .setMessage(msg)
                .setPositiveButton("Download", (d, w) -> startTripDownload(tileCount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startTripDownload(int totalTiles) {
        if (isDownloading || tripBoundingBox == null) return;
        isDownloading = true;
        btnDownloadTrip.setText("⏳ Downloading…");
        btnDownloadTrip.setAlpha(0.6f);
        btnDownloadTrip.setClickable(false);

        int pad = (int) (20 * requireContext().getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        TextView tvMsg = new TextView(requireContext());
        tvMsg.setText("Downloading trip map tiles…");
        tvMsg.setTextSize(14);
        tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
        layout.addView(tvMsg);

        ProgressBar progressBar = new ProgressBar(
                requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(Math.max(totalTiles, 1));
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

        // Capture Activity reference now (safe to use from background callbacks).
        // requireActivity() would throw if called after the fragment is detached.
        Activity activity = requireActivity();

        cacheManager.downloadAreaAsync(requireContext(), tripBoundingBox, ZOOM_MIN, ZOOM_MAX,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void downloadStarted() {}

                    @Override
                    public void setPossibleTilesInArea(int total) {}

                    @Override
                    public void updateProgress(int progress, int currentZoomLevel,
                                               int zoomMin, int zoomMax) {
                        activity.runOnUiThread(() -> {
                            if (activity.isFinishing() || activity.isDestroyed()) return;
                            progressBar.setProgress(progress);
                            tvCount.setText(progress + " / " + totalTiles
                                    + " tiles  (zoom " + currentZoomLevel + ")");
                        });
                    }

                    @Override
                    public void onTaskComplete() {
                        // Write DB record here on CacheManager's background thread — before
                        // posting to the main thread. This guarantees OfflineDataActivity
                        // will see the record the moment the user navigates there.
                        persistDownloadRecord();
                        activity.runOnUiThread(() -> {
                            safeDismiss(progressDialog);
                            isDownloading = false;
                            if (!isAdded() || activity.isFinishing() || activity.isDestroyed()) return;
                            markButtonAsDownloaded();
                            Toast.makeText(requireContext(),
                                    "✓ Trip map saved for offline use", Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onTaskFailed(int errors) {
                        // Save record even on partial failure — tiles that did download are usable.
                        persistDownloadRecord();
                        activity.runOnUiThread(() -> {
                            safeDismiss(progressDialog);
                            isDownloading = false;
                            if (!isAdded() || activity.isFinishing() || activity.isDestroyed()) return;
                            markButtonAsDownloaded();
                            String msg = errors == 0
                                    ? "✓ Trip map saved for offline use"
                                    : "✓ Trip map saved (" + errors + " tiles unavailable in this area)";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private static void safeDismiss(AlertDialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        } catch (Exception ignored) {}
    }

    /** Called directly on CacheManager's background thread — Room allows this (non-main thread). */
    private void persistDownloadRecord() {
        try {
            OfflineDownload dl = offlineDownloadDao.getDownloadForTripSync(tripId);
            if (dl == null) {
                dl = new OfflineDownload();
                dl.tripId = tripId;
                dl.mapSaved = true;
                dl.lastDownloadedAt = System.currentTimeMillis();
                offlineDownloadDao.insert(dl);
            } else {
                dl.mapSaved = true;
                dl.lastDownloadedAt = System.currentTimeMillis();
                offlineDownloadDao.update(dl);
            }
        } catch (Exception ignored) {}
    }

    private void markButtonAsDownloaded() {
        if (btnDownloadTrip == null) return;
        btnDownloadTrip.setText("✓ Offline Ready");
        btnDownloadTrip.setAlpha(1f);
        btnDownloadTrip.setClickable(true);
        btnDownloadTrip.setBackground(
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_selected));
        btnDownloadTrip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void resetDownloadButton() {
        if (btnDownloadTrip == null) return;
        btnDownloadTrip.setText("📥 Save Offline");
        btnDownloadTrip.setAlpha(1f);
        btnDownloadTrip.setClickable(true);
        btnDownloadTrip.setBackground(
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
        btnDownloadTrip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.voyage_text_primary));
    }

    // ── Data ──────────────────────────────────────────────────────

    private void onItemsChanged(List<ItineraryItem> items) {
        if (items == null || items.isEmpty()) {
            showStatus("No itinerary items yet — add stops in the Itinerary tab");
            return;
        }

        for (ItineraryItem item : items) {
            boolean noCoords = item.latitude == 0 && item.longitude == 0;
            boolean hasName = item.locationName != null && !item.locationName.trim().isEmpty();
            if (noCoords && hasName && !scheduledGeocode.contains(item.itemId)) {
                scheduledGeocode.add(item.itemId);
                scheduleGeocode(item);
            }
        }

        renderMap(items);
    }

    private void scheduleGeocode(ItineraryItem item) {
        // Capture package name on the main thread — requireContext() is not safe in background
        String pkgName = requireContext().getPackageName();
        geocodeExecutor.execute(() -> {
            try {
                Thread.sleep(700); // respect Nominatim rate limit: 1 req/sec
                double[] coords = NominatimClient.geocode(item.locationName, pkgName);
                item.latitude = coords[0];
                item.longitude = coords[1];
                viewModel.updateItem(item);
            } catch (Exception ignored) {}
        });
    }

    // ── Rendering ─────────────────────────────────────────────────

    private void renderMap(List<ItineraryItem> items) {
        if (mapView == null) return;
        mapView.getOverlays().clear();

        List<GeoPoint> routePoints = new ArrayList<>();
        int stopNum = 0;

        for (ItineraryItem item : items) {
            if (item.latitude == 0 && item.longitude == 0) continue;
            stopNum++;
            GeoPoint pt = new GeoPoint(item.latitude, item.longitude);
            routePoints.add(pt);

            int color = DAY_COLORS[(Math.max(item.dayNumber - 1, 0)) % DAY_COLORS.length];

            Marker marker = new Marker(mapView);
            marker.setPosition(pt);
            marker.setTitle(item.title != null && !item.title.isEmpty()
                    ? item.title : item.locationName);
            marker.setSubDescription("Day " + item.dayNumber
                    + " · " + slotLabel(item.timeSlot)
                    + (item.locationName != null && !item.locationName.isEmpty()
                       ? "\n📍 " + item.locationName : ""));
            marker.setIcon(createNumberedMarker(stopNum, color));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            mapView.getOverlays().add(marker);
        }

        // Dashed route polyline
        if (routePoints.size() >= 2) {
            Polyline polyline = new Polyline();
            polyline.setPoints(routePoints);
            polyline.getOutlinePaint().setColor(Color.parseColor("#8B0000"));
            polyline.getOutlinePaint().setStrokeWidth(6f);
            polyline.getOutlinePaint().setAlpha(150);
            mapView.getOverlayManager().add(0, polyline);
        }

        // Store bounding box for offline download
        if (!routePoints.isEmpty()) {
            if (routePoints.size() == 1) {
                GeoPoint pt = routePoints.get(0);
                tripBoundingBox = new BoundingBox(
                        pt.getLatitude() + 0.05, pt.getLongitude() + 0.05,
                        pt.getLatitude() - 0.05, pt.getLongitude() - 0.05);
            } else {
                tripBoundingBox = BoundingBox.fromGeoPoints(routePoints).increaseByScale(1.35f);
            }
        }

        // Fit camera to all plotted stops
        fitCamera(routePoints);

        // Update bottom strip
        plottedItems.clear();
        for (ItineraryItem it : items) {
            if (!(it.latitude == 0 && it.longitude == 0)) plottedItems.add(it);
        }
        stopAdapter.notifyDataSetChanged();

        int total = items.size();
        int plotted = plottedItems.size();

        if (plotted == 0) {
            showStatus("Add location names in the Itinerary tab — they'll appear here");
            rvStops.setVisibility(View.GONE);
        } else {
            String status = plotted + " of " + total + " stop" + (total != 1 ? "s" : "") + " plotted";
            if (plotted < total) status += " · geocoding remaining...";
            tvStatus.setText(status);
            tvStatus.setVisibility(View.VISIBLE);
            rvStops.setVisibility(View.VISIBLE);
        }

        mapView.invalidate();
    }

    private void fitCamera(List<GeoPoint> points) {
        if (points.isEmpty()) return;
        mapView.post(() -> {
            if (!isAdded() || mapView == null) return;
            if (points.size() == 1) {
                mapView.getController().animateTo(points.get(0));
                mapView.getController().setZoom(14.0);
            } else {
                BoundingBox box = BoundingBox.fromGeoPoints(points);
                mapView.zoomToBoundingBox(box.increaseByScale(1.35f), false);
            }
        });
    }

    // ── Marker drawing ────────────────────────────────────────────

    private Drawable createNumberedMarker(int num, int color) {
        float dp = getResources().getDisplayMetrics().density;
        int size = (int) (40 * dp);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(color);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2.5f * dp);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - dp, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size * 0.38f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float textY = size / 2f - (paint.descent() + paint.ascent()) / 2f;
        canvas.drawText(String.valueOf(num), size / 2f, textY, paint);

        return new BitmapDrawable(getResources(), bmp);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static String slotLabel(String slot) {
        if (slot == null) return "";
        switch (slot.toLowerCase()) {
            case "morning":   return "🌅 Morning";
            case "afternoon": return "☀ Afternoon";
            case "evening":   return "🌙 Evening";
            default:          return slot;
        }
    }

    private void showStatus(String text) {
        if (tvStatus != null) {
            tvStatus.setText(text);
            tvStatus.setVisibility(View.VISIBLE);
        }
        if (rvStops != null) rvStops.setVisibility(View.GONE);
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
        mapView = null;
    }
}
