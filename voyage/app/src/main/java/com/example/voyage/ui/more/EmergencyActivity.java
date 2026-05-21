package com.example.voyage.ui.more;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.EmergencyContactAdapter;
import com.example.voyage.database.entities.EmergencyContact;
import com.example.voyage.ui.island.SmartIsland;
import com.example.voyage.ui.map.NearbyPlace;
import com.example.voyage.ui.map.OverpassClient;
import com.example.voyage.util.AppContext;
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.EmergencyViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class EmergencyActivity extends AppCompatActivity
        implements EmergencyContactAdapter.Listener {

    private static final int LOCATION_PERM_REQUEST = 301;

    private EmergencyViewModel viewModel;
    private EmergencyContactAdapter adapter;
    private SessionManager session;
    private LinearLayout emptyContacts;

    private TextView tvHospitalName, tvHospitalDist;
    private TextView tvPoliceName, tvPoliceDist;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCall000).setOnClickListener(v -> dialNumber("000"));

        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvHospitalDist = findViewById(R.id.tvHospitalDist);
        tvPoliceName   = findViewById(R.id.tvPoliceName);
        tvPoliceDist   = findViewById(R.id.tvPoliceDist);

        // Clicking rows still opens Maps for navigation
        findViewById(R.id.rowHospital).setOnClickListener(v ->
                openMapsSearch("hospital near me"));
        findViewById(R.id.rowPolice).setOnClickListener(v ->
                openMapsSearch("police station near me"));

        findViewById(R.id.btnShareLocation).setOnClickListener(v -> shareLocation());

        RecyclerView rv = findViewById(R.id.rvEmergencyContacts);
        emptyContacts = findViewById(R.id.emptyContacts);
        adapter = new EmergencyContactAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);

        viewModel.getContacts().observe(this, contacts -> {
            adapter.setContacts(contacts);
            boolean empty = contacts == null || contacts.isEmpty();
            emptyContacts.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        FloatingActionButton fab = findViewById(R.id.fabAddContact);
        fab.setOnClickListener(v -> showAddContactDialog());

        fetchNearbyEmergencyServices();
    }

    // ── GPS + Overpass nearby services ────────────────────────────

    private void fetchNearbyEmergencyServices() {
        // Use cached AppContext location if available (faster)
        if (AppContext.userLat != 0 && AppContext.userLng != 0) {
            queryNearbyServices(AppContext.userLat, AppContext.userLng);
            return;
        }

        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            doFetchLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERM_REQUEST);
        }
    }

    @SuppressLint("MissingPermission")
    private void doFetchLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        queryNearbyServices(location.getLatitude(), location.getLongitude());
                    } else {
                        tvHospitalDist.setText("Location unavailable");
                        tvPoliceDist.setText("Location unavailable");
                    }
                });
    }

    private void queryNearbyServices(double lat, double lng) {
        tvHospitalDist.setText("Searching...");
        tvPoliceDist.setText("Searching...");

        OverpassClient.fetchNearby(lat, lng, "Emergency", new OverpassClient.Callback() {
            @Override
            public void onResult(List<NearbyPlace> places) {
                NearbyPlace nearestHospital = null;
                NearbyPlace nearestPolice   = null;

                for (NearbyPlace p : places) {
                    if (nearestHospital == null
                            && (p.type.equals("hospital") || p.type.equals("clinic")
                                || p.type.equals("doctors"))) {
                        nearestHospital = p;
                    }
                    if (nearestPolice == null && p.type.equals("police")) {
                        nearestPolice = p;
                    }
                    if (nearestHospital != null && nearestPolice != null) break;
                }

                NearbyPlace finalHospital = nearestHospital;
                NearbyPlace finalPolice   = nearestPolice;

                runOnUiThread(() -> {
                    if (finalHospital != null) {
                        tvHospitalName.setText(finalHospital.name);
                        tvHospitalDist.setText(finalHospital.getFormattedDistance() + " away");
                        // Island: nearest hospital found
                        SmartIsland.show(EmergencyActivity.this, new SmartIsland.Config()
                                .icon("🏥").title(finalHospital.name)
                                .subtitle(finalHospital.getFormattedDistance() + " away · tap to navigate")
                                .action("Navigate", () -> openMapsSearch("hospital near me"))
                                .autoDismiss(12000));
                    } else {
                        tvHospitalDist.setText("None found nearby");
                    }
                    if (finalPolice != null) {
                        tvPoliceName.setText(finalPolice.name);
                        tvPoliceDist.setText(finalPolice.getFormattedDistance() + " away");
                    } else {
                        tvPoliceDist.setText("None found nearby");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvHospitalDist.setText("Tap to search");
                    tvPoliceDist.setText("Tap to search");
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERM_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doFetchLocation();
        } else {
            tvHospitalDist.setText("Location permission needed");
            tvPoliceDist.setText("Location permission needed");
        }
    }

    // ── Emergency actions ─────────────────────────────────────────

    @Override
    public void onCall(EmergencyContact contact) { dialNumber(contact.phone); }

    @Override
    public void onDelete(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Contact")
                .setMessage("Remove " + contact.name + " from emergency contacts?")
                .setPositiveButton("Remove", (d, w) -> viewModel.deleteContact(contact.contactId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dialNumber(String number) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
        } else {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
        }
    }

    private void openMapsSearch(String query) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(query));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void shareLocation() {
        String locationText = AppContext.userLat != 0
                ? "https://maps.google.com/?q=" + AppContext.userLat + "," + AppContext.userLng
                : "https://maps.google.com/?q=current+location";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "🚨 I need help! My location: " + locationText);
        startActivity(Intent.createChooser(shareIntent, "Share Location via"));
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText etName     = dialogView.findViewById(R.id.etContactName);
        EditText etPhone    = dialogView.findViewById(R.id.etContactPhone);
        EditText etRelation = dialogView.findViewById(R.id.etContactRelation);
        Switch switchPrimary = dialogView.findViewById(R.id.switchPrimary);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();
            String relation = etRelation.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            EmergencyContact contact = new EmergencyContact();
            contact.userId   = session.getUserId();
            contact.name     = name;
            contact.phone    = phone;
            contact.relation = relation.isEmpty() ? "Contact" : relation;
            contact.isPrimary = switchPrimary.isChecked();

            viewModel.insertContact(contact);
            dialog.dismiss();
            Toast.makeText(this, name + " added to emergency contacts", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
