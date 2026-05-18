package com.example.lostfoundapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.database.DatabaseHelper;
import com.example.lostfoundapp.database.LostFoundItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_ALL    = "all";
    public static final String MODE_RADIUS = "radius";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;
    private String mode;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    showRadiusDialog();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    loadAllMarkersOnMap();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_ALL;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    MODE_RADIUS.equals(mode) ? "Radius Search" : "Map View");
        }

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (MODE_RADIUS.equals(mode)) {
            checkLocationPermissionForRadius();
        } else {
            loadAllMarkersOnMap();
        }
    }

    private void checkLocationPermissionForRadius() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            showRadiusDialog();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void showRadiusDialog() {
        EditText input = new EditText(this);
        input.setHint("e.g. 5");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Radius Search")
                .setMessage("Show items within how many km of your location?")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        loadItemsWithinRadius(Float.parseFloat(val));
                    } else {
                        Toast.makeText(this, "Please enter a radius", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void loadItemsWithinRadius(float radiusKm) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            loadAllMarkersOnMap();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this, "Could not get your location", Toast.LENGTH_SHORT).show();
                loadAllMarkersOnMap();
                return;
            }

            googleMap.clear();
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            List<LostFoundItem> allItems = dbHelper.getAllItems();
            int count = 0;

            for (LostFoundItem item : allItems) {
                if (item.getLatitude() == 0 && item.getLongitude() == 0) continue;

                float[] results = new float[1];
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        item.getLatitude(), item.getLongitude(), results);
                float distanceKm = results[0] / 1000f;

                if (distanceKm <= radiusKm) {
                    float hue = "Lost".equals(item.getPostType())
                            ? BitmapDescriptorFactory.HUE_RED
                            : BitmapDescriptorFactory.HUE_GREEN;
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(item.getLatitude(), item.getLongitude()))
                            .title(item.getName() + " (" + item.getPostType() + ")")
                            .snippet(item.getLocation() + " — " + String.format("%.1f km away", distanceKm))
                            .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                    count++;
                }
            }

            Toast.makeText(this, count + " item(s) within " + radiusKm + " km", Toast.LENGTH_SHORT).show();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
        });
    }

    private void loadAllMarkersOnMap() {
        List<LostFoundItem> items = dbHelper.getAllItems();
        googleMap.clear();
        LatLng lastPosition = null;
        int count = 0;

        for (LostFoundItem item : items) {
            if (item.getLatitude() == 0 && item.getLongitude() == 0) continue;
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            float hue = "Lost".equals(item.getPostType())
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_GREEN;
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(item.getName() + " (" + item.getPostType() + ")")
                    .snippet(item.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            lastPosition = position;
            count++;
        }

        if (lastPosition != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 12));
        } else {
            Toast.makeText(this, "No items with location data yet", Toast.LENGTH_SHORT).show();
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
