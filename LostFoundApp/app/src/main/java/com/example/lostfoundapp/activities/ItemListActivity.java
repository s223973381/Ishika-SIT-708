package com.example.lostfoundapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.adapter.ItemAdapter;
import com.example.lostfoundapp.database.DatabaseHelper;
import com.example.lostfoundapp.database.LostFoundItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ItemListActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

    private static final String[] FILTER_CATEGORIES =
            {"All", "Electronics", "Pets", "Wallets", "Clothing", "Keys", "Documents", "Other"};

    private RecyclerView   recyclerView;
    private ItemAdapter    adapter;
    private DatabaseHelper dbHelper;
    private EditText       etSearch;
    private Spinner        spFilter;
    private TextView       tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lost & Found Items");
        }

        dbHelper     = new DatabaseHelper(this);
        etSearch     = findViewById(R.id.et_search);
        spFilter     = findViewById(R.id.sp_filter);
        tvEmpty      = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FILTER_CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilter.setAdapter(catAdapter);

        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                filterItems();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterItems(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterItems();
    }

    private void filterItems() {
        String keyword  = etSearch.getText().toString().trim();
        String category = spFilter.getSelectedItem() != null
                ? spFilter.getSelectedItem().toString() : "All";

        List<LostFoundItem> items = dbHelper.searchItems(keyword, category);

        if (adapter == null) {
            adapter = new ItemAdapter(items, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateItems(items);
        }

        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(LostFoundItem item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        startActivity(intent);
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
