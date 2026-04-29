package com.example.sportsnewsfeedapp.data;

import com.example.sportsnewsfeedapp.R;
import com.example.sportsnewsfeedapp.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    public static List<NewsItem> getNewsList() {
        List<NewsItem> newsList = new ArrayList<>();

        newsList.add(new NewsItem(
                1,
                "Football Final Ends in Dramatic Penalty Shootout",
                "The football final ended with a thrilling penalty shootout after a 2-2 draw.",
                "Football",
                R.drawable.sample_football,
                true
        ));

        newsList.add(new NewsItem(
                2,
                "Basketball Team Claims Fourth Straight Win",
                "The basketball team dominated the second half to secure another victory.",
                "Basketball",
                R.drawable.sample_basketball,
                true
        ));

        newsList.add(new NewsItem(
                3,
                "Cricket Captain Scores Century",
                "A brilliant century led the team to a comfortable win in the final overs.",
                "Cricket",
                R.drawable.sample_cricket,
                false
        ));

        newsList.add(new NewsItem(
                4,
                "Football Club Signs New Striker",
                "The new striker is expected to strengthen the team’s attack this season.",
                "Football",
                R.drawable.sample_football,
                false
        ));

        newsList.add(new NewsItem(
                5,
                "Basketball Playoffs Begin This Weekend",
                "Fans are excited as the playoffs begin with top teams facing off.",
                "Basketball",
                R.drawable.sample_basketball,
                false
        ));

        newsList.add(new NewsItem(
                6,
                "Cricket Series Decider Scheduled for Sunday",
                "The deciding match promises high tension between the rival teams.",
                "Cricket",
                R.drawable.sample_cricket,
                true
        ));

        return newsList;
    }
}