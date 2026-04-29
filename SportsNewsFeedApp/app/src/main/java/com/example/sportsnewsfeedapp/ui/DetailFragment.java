package com.example.sportsnewsfeedapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.adapter.RelatedStoriesAdapter;
import com.example.sportsnewsfeedapp.data.DummyData;
import com.example.sportsnewsfeedapp.model.NewsItem;
import com.example.sportsnewsfeedapp.utils.BookmarkManager;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView ivDetailImage = view.findViewById(R.id.ivDetailImage);
        TextView tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDetailDescription = view.findViewById(R.id.tvDetailDescription);
        Button btnBookmark = view.findViewById(R.id.btnBookmark);
        RecyclerView recyclerRelated = view.findViewById(R.id.recyclerRelated);

        Bundle bundle = getArguments();
        if (bundle == null) return;

        int newsId = bundle.getInt("id");
        String title = bundle.getString("title");
        String description = bundle.getString("description");
        String category = bundle.getString("category");
        int imageResId = bundle.getInt("imageResId");

        tvDetailTitle.setText(title);
        tvDetailDescription.setText(description);
        ivDetailImage.setImageResource(imageResId);

        List<NewsItem> relatedStories = new ArrayList<>();
        for (NewsItem item : DummyData.getNewsList()) {
            if (item.getCategory().equals(category) && item.getId() != newsId) {
                relatedStories.add(item);
            }
        }

        recyclerRelated.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRelated.setAdapter(new RelatedStoriesAdapter(relatedStories));

        btnBookmark.setOnClickListener(v -> {
            BookmarkManager.toggleBookmark(requireContext(), newsId);
            Toast.makeText(requireContext(), "Bookmark updated", Toast.LENGTH_SHORT).show();
        });
    }
}