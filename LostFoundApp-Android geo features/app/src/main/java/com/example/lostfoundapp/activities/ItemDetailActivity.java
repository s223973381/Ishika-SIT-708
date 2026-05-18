package com.example.lostfoundapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.database.DatabaseHelper;
import com.example.lostfoundapp.database.LostFoundItem;
import com.example.lostfoundapp.utils.DateTimeUtils;
import com.example.lostfoundapp.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class ItemDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LostFoundItem  item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Item Details");
        }

        dbHelper = new DatabaseHelper(this);

        long itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId == -1) { finish(); return; }

        item = dbHelper.getItemById(itemId);
        if (item == null) { finish(); return; }

        bindData();

        MaterialButton btnRemove = findViewById(R.id.btn_remove);
        btnRemove.setOnClickListener(v -> {
            dbHelper.deleteItem(item.getId());
            Toast.makeText(this, getString(R.string.item_removed), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void bindData() {
        ImageView ivImage        = findViewById(R.id.iv_detail_image);
        Chip      chipPostType   = findViewById(R.id.chip_post_type);
        TextView  tvCategory     = findViewById(R.id.tv_detail_category);
        TextView  tvName         = findViewById(R.id.tv_detail_name);
        TextView  tvPhone        = findViewById(R.id.tv_detail_phone);
        TextView  tvDescription  = findViewById(R.id.tv_detail_description);
        TextView  tvDate         = findViewById(R.id.tv_detail_date);
        TextView  tvLocation     = findViewById(R.id.tv_detail_location);
        TextView  tvTimestamp    = findViewById(R.id.tv_detail_timestamp);

        Bitmap bmp = ImageUtils.loadBitmap(item.getImagePath());
        if (bmp != null) ivImage.setImageBitmap(bmp);

        chipPostType.setText(item.getPostType());
        if ("Lost".equals(item.getPostType())) {
            chipPostType.setChipBackgroundColorResource(R.color.lost_color);
        } else {
            chipPostType.setChipBackgroundColorResource(R.color.found_color);
        }
        chipPostType.setTextColor(getColor(R.color.chip_text_white));

        tvCategory.setText("Category: "    + item.getCategory());
        tvName.setText("Name: "            + item.getName());
        tvPhone.setText("Phone: "          + item.getPhone());
        tvDescription.setText("Description: " + item.getDescription());
        tvDate.setText("Date: "            + item.getDate());
        tvLocation.setText("Location: "    + item.getLocation());
        tvTimestamp.setText("Posted: "     + DateTimeUtils.formatTimestamp(item.getTimestamp()));
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
