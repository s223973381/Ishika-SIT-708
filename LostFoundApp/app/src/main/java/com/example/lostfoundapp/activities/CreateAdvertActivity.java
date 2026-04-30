package com.example.lostfoundapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.database.DatabaseHelper;
import com.example.lostfoundapp.database.LostFoundItem;
import com.example.lostfoundapp.utils.DateTimeUtils;
import com.example.lostfoundapp.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final String[] CATEGORIES =
            {"Electronics", "Pets", "Wallets", "Clothing", "Keys", "Documents", "Other"};

    private RadioGroup rgPostType;
    private Spinner    spCategory;
    private EditText   etName, etPhone, etDescription, etDate, etLocation;
    private ImageView  ivItemImage;
    private String     currentImagePath = null;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentImagePath = ImageUtils.saveImageToInternalStorage(this, uri);
                    if (currentImagePath != null) {
                        ivItemImage.setImageURI(Uri.parse("file://" + currentImagePath));
                        ivItemImage.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create a New Advert");
        }

        dbHelper = new DatabaseHelper(this);

        rgPostType    = findViewById(R.id.rg_post_type);
        spCategory    = findViewById(R.id.sp_category);
        etName        = findViewById(R.id.et_name);
        etPhone       = findViewById(R.id.et_phone);
        etDescription = findViewById(R.id.et_description);
        etDate        = findViewById(R.id.et_date);
        etLocation    = findViewById(R.id.et_location);
        ivItemImage   = findViewById(R.id.iv_item_image);

        etDate.setText(DateTimeUtils.getCurrentDate());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        MaterialButton btnPickImage = findViewById(R.id.btn_pick_image);
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        MaterialButton btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        int selectedId = rgPostType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, getString(R.string.select_lost_found), Toast.LENGTH_SHORT).show();
            return;
        }
        String postType = (selectedId == R.id.rb_lost) ? "Lost" : "Found";
        String name     = etName.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.name_required));
            return;
        }
        if (currentImagePath == null) {
            Toast.makeText(this, getString(R.string.image_required), Toast.LENGTH_SHORT).show();
            return;
        }

        LostFoundItem item = new LostFoundItem(
                postType,
                name,
                etPhone.getText().toString().trim(),
                etDescription.getText().toString().trim(),
                etDate.getText().toString().trim(),
                etLocation.getText().toString().trim(),
                currentImagePath,
                spCategory.getSelectedItem().toString(),
                DateTimeUtils.getCurrentTimestamp()
        );

        long id = dbHelper.insertItem(item);
        if (id != -1) {
            Toast.makeText(this, getString(R.string.item_saved), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
