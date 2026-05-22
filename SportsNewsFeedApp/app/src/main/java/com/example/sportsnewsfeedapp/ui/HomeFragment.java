package com.example.sportsnewsfeedapp.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.adapter.FeaturedAdapter;
import com.example.sportsnewsfeedapp.adapter.LatestNewsAdapter;
import com.example.sportsnewsfeedapp.data.DummyData;
import com.example.sportsnewsfeedapp.model.NewsItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerFeatured, recyclerLatest;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private LatestNewsAdapter latestNewsAdapter;
    private List<NewsItem> allNews;
    private boolean updatingChipFromSearch = false;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerFeatured = view.findViewById(R.id.recyclerFeatured);
        recyclerLatest = view.findViewById(R.id.recyclerLatest);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroup = view.findViewById(R.id.chipGroup);

        toolbar.inflateMenu(R.menu.top_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_bookmarks) {
                Navigation.findNavController(view)
                        .navigate(R.id.action_homeFragment_to_bookmarksFragment);
                return true;
            }
            return false;
        });

        allNews = DummyData.getNewsList();

        List<NewsItem> featuredList = new ArrayList<>();
        for (NewsItem item : allNews) {
            if (item.isFeatured()) {
                featuredList.add(item);
            }
        }

        FeaturedAdapter featuredAdapter = new FeaturedAdapter(featuredList, item -> openDetail(view, item));
        latestNewsAdapter = new LatestNewsAdapter(allNews, item -> openDetail(view, item));

        recyclerFeatured.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFeatured.setAdapter(featuredAdapter);

        recyclerLatest.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLatest.setAdapter(latestNewsAdapter);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (updatingChipFromSearch) return;
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            String category = getCategoryForChip(checkedId);

            etSearch.removeTextChangedListener(searchWatcher);
            etSearch.setText(category);
            etSearch.removeTextChangedListener(searchWatcher);
            etSearch.addTextChangedListener(searchWatcher);

            filterNews(category);
        });

        etSearch.addTextChangedListener(searchWatcher);
    }

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String query = s.toString().trim();
            filterNews(query);
            updatingChipFromSearch = true;
            selectChipForQuery(query);
            updatingChipFromSearch = false;
        }
    };

    private void selectChipForQuery(String query) {
        if (query.isEmpty()) {
            chipGroup.check(R.id.chipAll);
        } else if (query.equalsIgnoreCase("Football")) {
            chipGroup.check(R.id.chipFootball);
        } else if (query.equalsIgnoreCase("Basketball")) {
            chipGroup.check(R.id.chipBasketball);
        } else if (query.equalsIgnoreCase("Cricket")) {
            chipGroup.check(R.id.chipCricket);
        } else {
            chipGroup.check(R.id.chipAll);
        }
    }

    private String getCategoryForChip(int chipId) {
        if (chipId == R.id.chipFootball) return "Football";
        if (chipId == R.id.chipBasketball) return "Basketball";
        if (chipId == R.id.chipCricket) return "Cricket";
        return "";
    }

    private void filterNews(String query) {
        List<NewsItem> filteredList = new ArrayList<>();

        if (query.trim().isEmpty()) {
            filteredList = allNews;
        } else {
            for (NewsItem item : allNews) {
                if (item.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        latestNewsAdapter.updateList(filteredList);
    }

    private void openDetail(View view, NewsItem item) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("title", item.getTitle());
        bundle.putString("description", item.getDescription());
        bundle.putString("category", item.getCategory());
        bundle.putInt("imageResId", item.getImageResId());

        Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_detailFragment, bundle);
    }
}
