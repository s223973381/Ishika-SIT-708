package com.example.sportsnewsfeedapp.model;

public class NewsItem {
    private int id;
    private String title;
    private String description;
    private String category;
    private int imageResId;
    private boolean featured;

    public NewsItem(int id, String title, String description, String category, int imageResId, boolean featured) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageResId = imageResId;
        this.featured = featured;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isFeatured() {
        return featured;
    }
}