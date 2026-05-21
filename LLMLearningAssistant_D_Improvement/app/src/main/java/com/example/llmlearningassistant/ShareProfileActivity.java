package com.example.llmlearningassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_profile);

        LinearLayout card = findViewById(R.id.shareCard);
        card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        SharedPreferences prefs = getSharedPreferences("llm_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", getIntent().getStringExtra("username"));
        if (username == null || username.isEmpty()) username = "Student";
        String email = prefs.getString("email", "—");
        String plan = prefs.getString("plan", "Free");

        DatabaseHelper db = new DatabaseHelper(this);
        UserStatsModel stats = db.getStats();

        String profileText =
                "LLM Learning Profile\n" +
                "Name: " + username + "\n" +
                "Email: " + email + "\n" +
                "Plan: " + plan + " Plan\n" +
                "Total Questions: " + stats.totalQuestions + "\n" +
                "Correct Answers: " + stats.correctAnswers + "\n" +
                "Incorrect Answers: " + stats.incorrectAnswers + "\n" +
                "App: LLM Learning Assistant";

        ((TextView) findViewById(R.id.tvProfileSummary)).setText(profileText);

        Bitmap qrBitmap = generateQrCode(profileText, 512);
        if (qrBitmap != null) {
            ((ImageView) findViewById(R.id.ivQrCode)).setImageBitmap(qrBitmap);
        }

        final String finalUsername = username;
        final String finalProfileText = profileText;
        final Bitmap finalQrBitmap = qrBitmap;

        findViewById(R.id.btnShareText).setOnClickListener(v ->
                shareAsText(finalUsername, finalProfileText));
        findViewById(R.id.btnShareQr).setOnClickListener(v ->
                shareAsQr(finalUsername, finalQrBitmap));
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void shareAsText(String username, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, username + "'s LLM Learning Profile");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share Profile via"));
    }

    private void shareAsQr(String username, Bitmap qrBitmap) {
        if (qrBitmap == null) {
            Toast.makeText(this, "QR code unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File cacheDir = new File(getCacheDir(), "qr");
            cacheDir.mkdirs();
            File qrFile = new File(cacheDir, "profile_qr.png");
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", qrFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, username + "'s Learning QR Code");
            intent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to view my LLM Learning profile!");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share QR Code via"));
        } catch (IOException e) {
            Toast.makeText(this, "Could not share QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap generateQrCode(String content, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF2D3436 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }
}
