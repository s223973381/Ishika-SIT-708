package com.example.sportsnewsfeedapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerFeatured, recyclerLatest;
    private EditText etSearch;
    private LatestNewsAdapter latestNewsAdapter;
    private List<NewsItem> allNews;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerFeatured = view.findViewById(R.id.recyclerFeatured);
        recyclerLatest = view.findViewById(R.id.recyclerLatest);
        etSearch = view.findViewById(R.id.etSearch);

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

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            filterNews(etSearch.getText().toString());
            return true;
        });
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        View view = getView();
        if (item.getItemId() == R.id.menu_bookmarks && view != null) {
            Navigation.findNavController(view)
                    .navigate(R.id.action_homeFragment_to_bookmarksFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}