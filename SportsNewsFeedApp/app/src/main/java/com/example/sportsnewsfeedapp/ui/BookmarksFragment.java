package com.example.sportsnewsfeedapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.adapter.BookmarkAdapter;
import com.example.sportsnewsfeedapp.data.DummyData;
import com.example.sportsnewsfeedapp.model.NewsItem;
import com.example.sportsnewsfeedapp.utils.BookmarkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BookmarksFragment extends Fragment {

    public BookmarksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerBookmarks = view.findViewById(R.id.recyclerBookmarks);
        recyclerBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));

        Set<String> bookmarkedIds = BookmarkManager.getBookmarks(requireContext());
        List<NewsItem> bookmarkedStories = new ArrayList<>();

        for (NewsItem item : DummyData.getNewsList()) {
            if (bookmarkedIds.contains(String.valueOf(item.getId()))) {
                bookmarkedStories.add(item);
            }
        }

        recyclerBookmarks.setAdapter(new BookmarkAdapter(bookmarkedStories));
    }
}