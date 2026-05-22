package com.example.voyage.ui.more;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.AppDatabase;
import com.example.voyage.database.dao.OfflineDownloadDao;
import com.example.voyage.database.dao.TripDao;
import com.example.voyage.database.entities.OfflineDownload;
import com.example.voyage.database.entities.Trip;

import org.osmdroid.config.Configuration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OfflineDataActivity extends AppCompatActivity {

    private static final String PREFS_OFFLINE = "voyage_offline";
    private static final String KEY_GLOBAL_TS = "global_map_downloaded_at";

    private TextView tvCacheSize, btnClearAll;
    private View cardGlobalMap;
    private TextView tvGlobalDownloadedAt;
    private RecyclerView rvOfflineTrips;
    private View emptyState;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private TripAdapter adapter;

    private OfflineDownloadDao downloadDao;
    private TripDao tripDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_data);

        // Use the same app-specific path as TripMapFragment so we read the correct cache
        File extDir = getExternalFilesDir(null);
        if (extDir == null) extDir = getFilesDir();
        Configuration.getInstance().setOsmdroidBasePath(new File(extDir, "osmdroid"));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        downloadDao = AppDatabase.getInstance(this).offlineDownloadDao();
        tripDao = AppDatabase.getInstance(this).tripDao();

        tvCacheSize = findViewById(R.id.tvCacheSize);
        btnClearAll = findViewById(R.id.btnClearAll);
        cardGlobalMap = findViewById(R.id.cardGlobalMap);
        tvGlobalDownloadedAt = findViewById(R.id.tvGlobalDownloadedAt);
        rvOfflineTrips = findViewById(R.id.rvOfflineTrips);
        emptyState = findViewById(R.id.emptyState);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new TripAdapter(new ArrayList<>());
        rvOfflineTrips.setLayoutManager(new LinearLayoutManager(this));
        rvOfflineTrips.setAdapter(adapter);

        btnClearAll.setOnClickListener(v -> confirmClearAll());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // ── Data ──────────────────────────────────────────────────────

    private void loadData() {
        executor.execute(() -> {
            // Load saved trip maps
            List<OfflineDownload> downloads = downloadDao.getAllMapDownloadsSync();
            List<TripDownloadInfo> infos = new ArrayList<>();
            for (OfflineDownload dl : downloads) {
                Trip trip = tripDao.getTripByIdSync(dl.tripId);
                if (trip != null) {
                    infos.add(new TripDownloadInfo(trip, dl));
                }
            }

            // Calculate osmdroid tile cache size
            long cacheBytes = getOsmCacheSize();

            // Check global explore map download
            SharedPreferences prefs = getSharedPreferences(PREFS_OFFLINE, MODE_PRIVATE);
            long globalTs = prefs.getLong(KEY_GLOBAL_TS, 0);

            mainHandler.post(() -> {
                updateCacheSizeLabel(cacheBytes, infos.size());
                adapter.setItems(infos);

                // Global explore map card
                if (globalTs > 0 && cacheBytes > 0) {
                    cardGlobalMap.setVisibility(View.VISIBLE);
                    tvGlobalDownloadedAt.setText("Downloaded "
                            + DATE_FMT.format(new Date(globalTs)));
                } else {
                    cardGlobalMap.setVisibility(View.GONE);
                }

                boolean hasAny = !infos.isEmpty() || (globalTs > 0 && cacheBytes > 0);
                rvOfflineTrips.setVisibility(infos.isEmpty() ? View.GONE : View.VISIBLE);
                emptyState.setVisibility(hasAny ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void updateCacheSizeLabel(long bytes, int tripCount) {
        String size = formatBytes(bytes);
        String count = tripCount + " trip map" + (tripCount != 1 ? "s" : "") + " saved";
        tvCacheSize.setText(count + "  ·  " + size + " used");
    }

    // ── Clear all ─────────────────────────────────────────────────

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Offline Maps")
                .setMessage("This will delete all downloaded map tiles and free up storage. "
                        + "You can re-download them from each trip's Map tab.\n\nContinue?")
                .setPositiveButton("Clear All", (d, w) -> clearAll())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAll() {
        executor.execute(() -> {
            deleteOsmCache();
            downloadDao.clearAllMapSavedFlags();
            getSharedPreferences(PREFS_OFFLINE, MODE_PRIVATE)
                    .edit().remove(KEY_GLOBAL_TS).apply();

            mainHandler.post(() -> {
                Toast.makeText(this, "All offline map data cleared", Toast.LENGTH_SHORT).show();
                loadData();
            });
        });
    }

    // ── osmdroid cache helpers ────────────────────────────────────

    private long getOsmCacheSize() {
        try {
            File basePath = Configuration.getInstance().getOsmdroidBasePath();
            if (basePath == null || !basePath.exists()) return 0;
            long size = 0;
            // osmdroid 6.x stores tiles in osmdroid.db (SqlTileWriter)
            File dbFile = new File(basePath, "osmdroid.db");
            if (dbFile.exists()) size += dbFile.length();
            // Older/fallback file-based tile cache
            File tilesDir = new File(basePath, "tiles");
            if (tilesDir.exists()) size += folderSize(tilesDir);
            return size;
        } catch (Exception e) {
            return 0;
        }
    }

    private void deleteOsmCache() {
        try {
            File basePath = Configuration.getInstance().getOsmdroidBasePath();
            if (basePath == null) return;
            File dbFile = new File(basePath, "osmdroid.db");
            if (dbFile.exists()) dbFile.delete();
            File tilesDir = new File(basePath, "tiles");
            if (tilesDir.exists()) deleteDir(tilesDir);
        } catch (Exception ignored) {}
    }

    private long folderSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            size += f.isDirectory() ? folderSize(f) : f.length();
        }
        return size;
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ── Data model ────────────────────────────────────────────────

    static class TripDownloadInfo {
        final Trip trip;
        final OfflineDownload download;

        TripDownloadInfo(Trip trip, OfflineDownload download) {
            this.trip = trip;
            this.download = download;
        }
    }

    // ── Adapter ───────────────────────────────────────────────────

    static class TripAdapter extends RecyclerView.Adapter<TripAdapter.VH> {

        private final List<TripDownloadInfo> items;
        private static final SimpleDateFormat ITEM_DATE_FMT =
                new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        TripAdapter(List<TripDownloadInfo> items) {
            this.items = items;
        }

        void setItems(List<TripDownloadInfo> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_offline_trip, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            TripDownloadInfo info = items.get(position);
            h.tvTitle.setText(info.trip.title != null ? info.trip.title : "Untitled Trip");
            h.tvDestination.setText(info.trip.destination != null
                    ? "📍 " + info.trip.destination : "");
            h.tvDate.setText("Downloaded "
                    + ITEM_DATE_FMT.format(new Date(info.download.lastDownloadedAt)));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDestination, tvDate;

            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTripTitle);
                tvDestination = v.findViewById(R.id.tvTripDestination);
                tvDate = v.findViewById(R.id.tvDownloadedAt);
            }
        }
    }
}
