package com.example.istreamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.istreamapp.adapter.PlaylistAdapter;
import com.example.istreamapp.database.AppDatabase;
import com.example.istreamapp.database.PlaylistItem;
import com.example.istreamapp.utils.SessionManager;

import java.util.List;
import java.util.concurrent.Executors;

public class PlaylistActivity extends AppCompatActivity {

    private RecyclerView rvPlaylist;
    private TextView tvEmpty;
    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_playlist);
        db = AppDatabase.getInstance(this);

        rvPlaylist = findViewById(R.id.rvPlaylist);
        tvEmpty    = findViewById(R.id.tvEmpty);

        rvPlaylist.setLayoutManager(new LinearLayoutManager(this));

        loadPlaylist();

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void loadPlaylist() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<PlaylistItem> items = db.playlistDao()
                    .getPlaylistForUser(session.getUserId());
            runOnUiThread(() -> {
                if (items.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvPlaylist.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvPlaylist.setVisibility(View.VISIBLE);
                    PlaylistAdapter adapter = new PlaylistAdapter(items, this::openInPlayer);
                    rvPlaylist.setAdapter(adapter);
                }
            });
        });
    }

    private void openInPlayer(PlaylistItem item) {
        // Go back to MainActivity and load the selected video
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("video_url", item.videoUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}