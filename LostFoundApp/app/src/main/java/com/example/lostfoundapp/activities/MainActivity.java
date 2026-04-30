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

        btnCreateAdvert.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        btnShowItems.setOnClickListener(v ->
                startActivity(new Intent(this, ItemListActivity.class)));
    }
}
