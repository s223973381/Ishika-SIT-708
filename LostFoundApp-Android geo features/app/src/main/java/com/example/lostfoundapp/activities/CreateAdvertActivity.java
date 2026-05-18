package com.example.lostfoundapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.database.DatabaseHelper;
import com.example.lostfoundapp.database.LostFoundItem;
import com.example.lostfoundapp.utils.DateTimeUtils;
import com.example.lostfoundapp.utils.ImageUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final String[] CATEGORIES =
            {"Electronics", "Pets", "Wallets", "Clothing", "Keys", "Documents", "Other"};

    private RadioGroup rgPostType;
    private Spinner    spCategory;
    private EditText   etName, etPhone, etDescription, etDate, etLocation;
    private ImageView  ivItemImage;
    private String     currentImagePath = null;
    private double     selectedLatitude = 0;
    private double     selectedLongitude = 0;
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;

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

    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    String address = place.getAddress();
                    if (address == null || address.isEmpty()) address = place.getName();
                    etLocation.setText(address != null ? address : "");
                    if (place.getLatLng() != null) {
                        selectedLatitude = place.getLatLng().latitude;
                        selectedLongitude = place.getLatLng().longitude;
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Toast.makeText(this, "Location search error", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (granted != null && granted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        etLocation.setOnClickListener(v -> launchAutocomplete());

        MaterialButton btnGetLocation = findViewById(R.id.btn_get_location);
        btnGetLocation.setOnClickListener(v -> checkAndGetCurrentLocation());

        MaterialButton btnPickImage = findViewById(R.id.btn_pick_image);
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        MaterialButton btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void launchAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    private void checkAndGetCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLatitude = location.getLatitude();
                selectedLongitude = location.getLongitude();
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            selectedLatitude, selectedLongitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        etLocation.setText(addresses.get(0).getAddressLine(0));
                    } else {
                        etLocation.setText(selectedLatitude + ", " + selectedLongitude);
                    }
                } catch (IOException e) {
                    etLocation.setText(selectedLatitude + ", " + selectedLongitude);
                }
            } else {
                Toast.makeText(this, "Could not get location. Move to better signal area.", Toast.LENGTH_SHORT).show();
            }
        });
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
        item.setLatitude(selectedLatitude);
        item.setLongitude(selectedLongitude);

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
