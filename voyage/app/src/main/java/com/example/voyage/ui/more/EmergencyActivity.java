package com.example.voyage.ui.more;

import android.Manifest;
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
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.EmergencyViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmergencyActivity extends AppCompatActivity
        implements EmergencyContactAdapter.Listener {

    private EmergencyViewModel viewModel;
    private EmergencyContactAdapter adapter;
    private SessionManager session;
    private LinearLayout emptyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Call 000
        findViewById(R.id.btnCall000).setOnClickListener(v -> dialNumber("000"));

        // Nearby services — open Maps
        findViewById(R.id.rowHospital).setOnClickListener(v ->
                openMapsSearch("hospital near me"));
        findViewById(R.id.rowPolice).setOnClickListener(v ->
                openMapsSearch("police station near me"));

        // Share location
        findViewById(R.id.btnShareLocation).setOnClickListener(v -> shareLocation());

        // RecyclerView
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

        // FAB
        FloatingActionButton fab = findViewById(R.id.fabAddContact);
        fab.setOnClickListener(v -> showAddContactDialog());
    }

    @Override
    public void onCall(EmergencyContact contact) {
        dialNumber(contact.phone);
    }

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
            // Fallback to dial intent (no permission required)
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
            // Fallback to browser
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(query));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void shareLocation() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🚨 I need help! My location: https://maps.google.com/?q=current+location");
        startActivity(Intent.createChooser(shareIntent, "Share Location via"));
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText etName = dialogView.findViewById(R.id.etContactName);
        EditText etPhone = dialogView.findViewById(R.id.etContactPhone);
        EditText etRelation = dialogView.findViewById(R.id.etContactRelation);
        Switch switchPrimary = dialogView.findViewById(R.id.switchPrimary);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relation = etRelation.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            EmergencyContact contact = new EmergencyContact();
            contact.userId = session.getUserId();
            contact.name = name;
            contact.phone = phone;
            contact.relation = relation.isEmpty() ? "Contact" : relation;
            contact.isPrimary = switchPrimary.isChecked();

            viewModel.insertContact(contact);
            dialog.dismiss();
            Toast.makeText(this, name + " added to emergency contacts", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
