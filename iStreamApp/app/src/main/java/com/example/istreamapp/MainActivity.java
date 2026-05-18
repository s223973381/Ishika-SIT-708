package com.example.istreamapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istreamapp.database.AppDatabase;
import com.example.istreamapp.database.PlaylistItem;
import com.example.istreamapp.utils.SessionManager;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private WebView webViewPlayer;
    private EditText etVideoUrl;
    private TextView tvUrlError, tvUsername;
    private Button btnOpenInYoutube;

    private AppDatabase db;
    private SessionManager session;
    private String currentVideoUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        webViewPlayer = findViewById(R.id.webViewPlayer);
        etVideoUrl = findViewById(R.id.etVideoUrl);
        tvUrlError = findViewById(R.id.tvUrlError);
        tvUsername = findViewById(R.id.tvUsername);
        btnOpenInYoutube = findViewById(R.id.btnOpenInYoutube);

        tvUsername.setText("Hi, " + session.getUsername());

        setupWebView();

        String urlFromPlaylist = getIntent().getStringExtra("video_url");
        if (urlFromPlaylist != null) {
            etVideoUrl.setText(urlFromPlaylist);
            playVideo(urlFromPlaylist);
        }

        findViewById(R.id.btnPlay).setOnClickListener(v -> {
            String url = etVideoUrl.getText().toString().trim();

            if (!validateUrl(url)) {
                return;
            }

            playVideo(url);
        });

        findViewById(R.id.btnAddToPlaylist).setOnClickListener(v -> {
            String url = etVideoUrl.getText().toString().trim();

            if (!validateUrl(url)) {
                return;
            }

            addToPlaylist(url);
        });

        findViewById(R.id.btnMyPlaylist).setOnClickListener(v ->
                startActivity(new Intent(this, PlaylistActivity.class))
        );

        btnOpenInYoutube.setOnClickListener(v -> openInYoutubeApp());

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webViewPlayer.getSettings();

        // Spoof a real Chrome user-agent so YouTube doesn't block WebView playback (Error 152)
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36"
        );

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webViewPlayer, true);

        webViewPlayer.setWebViewClient(new WebViewClient());
        webViewPlayer.setWebChromeClient(new WebChromeClient());
    }

    private boolean validateUrl(String url) {
        tvUrlError.setText("");

        if (url.isEmpty()) {
            tvUrlError.setText("Please enter a YouTube URL.");
            return false;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            tvUrlError.setText("URL must start with http:// or https://");
            return false;
        }

        if (!url.contains("youtube.com/watch")
                && !url.contains("youtu.be/")
                && !url.contains("youtube.com/shorts/")) {
            tvUrlError.setText("Please enter a valid YouTube URL.");
            return false;
        }

        return true;
    }

    private void playVideo(String url) {
        String videoId = extractVideoId(url);

        if (videoId == null) {
            tvUrlError.setText("Could not extract video ID from URL.");
            return;
        }

        tvUrlError.setText("");
        currentVideoUrl = url;
        btnOpenInYoutube.setVisibility(View.VISIBLE);

        // Load the YouTube mobile watch page directly - bypasses all embed restrictions (Error 152/153)
        String watchUrl = "https://m.youtube.com/watch?v=" + videoId;
        webViewPlayer.loadUrl(watchUrl);
    }

    private String extractVideoId(String url) {
        try {
            if (url.contains("youtu.be/")) {
                String id = url.substring(url.lastIndexOf("youtu.be/") + 9);

                if (id.contains("?")) {
                    id = id.substring(0, id.indexOf("?"));
                }

                if (id.contains("&")) {
                    id = id.substring(0, id.indexOf("&"));
                }

                return id.isEmpty() ? null : id;
            }

            if (url.contains("youtube.com/shorts/")) {
                String id = url.substring(url.indexOf("shorts/") + 7);

                if (id.contains("?")) {
                    id = id.substring(0, id.indexOf("?"));
                }

                if (id.contains("&")) {
                    id = id.substring(0, id.indexOf("&"));
                }

                return id.isEmpty() ? null : id;
            }

            if (url.contains("v=")) {
                String id = url.substring(url.indexOf("v=") + 2);

                if (id.contains("&")) {
                    id = id.substring(0, id.indexOf("&"));
                }

                return id.isEmpty() ? null : id;
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private void openInYoutubeApp() {
        if (currentVideoUrl == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentVideoUrl));
        intent.setPackage("com.google.android.youtube");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentVideoUrl)));
        }
    }

    private void addToPlaylist(String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            PlaylistItem item = new PlaylistItem();
            item.userId = session.getUserId();
            item.videoUrl = url;

            db.playlistDao().insertItem(item);

            runOnUiThread(() ->
                    Toast.makeText(this, "Added to playlist!", Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void logout() {
        session.clearSession();
        webViewPlayer.loadUrl("about:blank");

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String url = intent.getStringExtra("video_url");

        if (url != null) {
            etVideoUrl.setText(url);
            playVideo(url);
        }
    }
}