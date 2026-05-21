package com.example.voyage.ui.trips;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.JournalEntryAdapter;
import com.example.voyage.database.entities.JournalEntry;
import com.example.voyage.viewmodel.JournalViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class TripJournalFragment extends Fragment {

    private int tripId;
    private JournalViewModel viewModel;
    private JournalEntryAdapter adapter;

    private String selectedMood = "happy";
    private String selectedDate = "";
    private String selectedImagePath = null;

    // Hold references into the open dialog so the launcher callback can update them
    private ImageView pendingPhotoPreview = null;
    private TextView  pendingPhotoLabel   = null;

    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        String path = copyToInternalStorage(uri);
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            selectedImagePath = path;
                            if (path == null || pendingPhotoPreview == null) return;
                            Bitmap bmp = BitmapFactory.decodeFile(path);
                            if (bmp != null) {
                                pendingPhotoPreview.setImageBitmap(bmp);
                                pendingPhotoPreview.setVisibility(View.VISIBLE);
                            }
                            if (pendingPhotoLabel != null) pendingPhotoLabel.setText("Change Photo");
                        });
                    });
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }
        if (tripId == -1) return;

        RecyclerView rv = view.findViewById(R.id.rvJournal);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddEntry);

        viewModel = new ViewModelProvider(this).get(JournalViewModel.class);

        adapter = new JournalEntryAdapter(entry ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Entry")
                        .setMessage("Delete this journal entry?")
                        .setPositiveButton("Delete", (d, w) -> viewModel.deleteEntry(entry))
                        .setNegativeButton("Cancel", null)
                        .show());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        viewModel.getEntriesForTrip(tripId).observe(getViewLifecycleOwner(), entries -> {
            adapter.setEntries(entries);
            boolean empty = entries == null || entries.isEmpty();
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    // ── Add dialog ────────────────────────────────────────────────

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_journal_entry, null);

        EditText     etTitle      = dialogView.findViewById(R.id.etEntryTitle);
        EditText     etContent    = dialogView.findViewById(R.id.etEntryContent);
        TextView     tvDate       = dialogView.findViewById(R.id.tvDate);
        LinearLayout btnPickDate  = dialogView.findViewById(R.id.btnPickDate);
        LinearLayout btnPickPhoto = dialogView.findViewById(R.id.btnPickPhoto);
        ImageView    ivDialogPhoto = dialogView.findViewById(R.id.ivDialogPhoto);
        TextView     tvPhotoLabel = dialogView.findViewById(R.id.tvPhotoLabel);

        TextView moodHappy       = dialogView.findViewById(R.id.moodHappy);
        TextView moodExcited     = dialogView.findViewById(R.id.moodExcited);
        TextView moodTired       = dialogView.findViewById(R.id.moodTired);
        TextView moodReflective  = dialogView.findViewById(R.id.moodReflective);
        TextView moodAdventurous = dialogView.findViewById(R.id.moodAdventurous);

        selectedMood = "happy";
        selectedDate = "";
        selectedImagePath = null;
        pendingPhotoPreview = ivDialogPhoto;
        pendingPhotoLabel   = tvPhotoLabel;

        moodHappy.setOnClickListener(v       -> setMood(moodHappy,       new TextView[]{moodExcited, moodTired, moodReflective, moodAdventurous}, "happy"));
        moodExcited.setOnClickListener(v     -> setMood(moodExcited,     new TextView[]{moodHappy, moodTired, moodReflective, moodAdventurous},   "excited"));
        moodTired.setOnClickListener(v       -> setMood(moodTired,       new TextView[]{moodHappy, moodExcited, moodReflective, moodAdventurous},  "tired"));
        moodReflective.setOnClickListener(v  -> setMood(moodReflective,  new TextView[]{moodHappy, moodExcited, moodTired, moodAdventurous},       "reflective"));
        moodAdventurous.setOnClickListener(v -> setMood(moodAdventurous, new TextView[]{moodHappy, moodExcited, moodTired, moodReflective},         "adventurous"));

        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dv, y, m, d) -> {
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
                tvDate.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle("New Journal Entry")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String title   = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty() && content.isEmpty()) return;

                    JournalEntry entry = new JournalEntry();
                    entry.tripId    = tripId;
                    entry.title     = title.isEmpty() ? "Untitled" : title;
                    entry.content   = content;
                    entry.mood      = selectedMood;
                    entry.imagePath = selectedImagePath;
                    entry.date      = selectedDate.isEmpty()
                            ? String.format("%04d-%02d-%02d",
                                Calendar.getInstance().get(Calendar.YEAR),
                                Calendar.getInstance().get(Calendar.MONTH) + 1,
                                Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                            : selectedDate;
                    viewModel.insertEntry(entry);
                    pendingPhotoPreview = null;
                    pendingPhotoLabel   = null;
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    pendingPhotoPreview = null;
                    pendingPhotoLabel   = null;
                })
                .show();
    }

    private void setMood(TextView selected, TextView[] others, String mood) {
        selectedMood = mood;
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        for (TextView t : others) t.setBackgroundResource(R.drawable.bg_chip);
    }

    // ── Image copy ────────────────────────────────────────────────

    private String copyToInternalStorage(Uri uri) {
        try {
            String fileName = "journal_" + System.currentTimeMillis() + ".jpg";
            File dest = new File(requireContext().getFilesDir(), fileName);
            try (InputStream in  = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(dest)) {
                if (in == null) return null;
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            return dest.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }
}
