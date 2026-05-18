package com.example.lostfoundapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfoundapp.R;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnCreateAdvert = findViewById(R.id.btn_create_advert);
        MaterialButton btnShowItems    = findViewById(R.id.btn_show_items);
        MaterialButton btnShowOnMap    = findViewById(R.id.btn_show_on_map);
        MaterialButton btnRadiusSearch = findViewById(R.id.btn_radius_search);

        btnCreateAdvert.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        btnShowItems.setOnClickListener(v ->
                startActivity(new Intent(this, ItemListActivity.class)));

        btnShowOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(MapsActivity.EXTRA_MODE, MapsActivity.MODE_ALL);
            startActivity(intent);
        });

        btnRadiusSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(MapsActivity.EXTRA_MODE, MapsActivity.MODE_RADIUS);
            startActivity(intent);
        });
    }
}
