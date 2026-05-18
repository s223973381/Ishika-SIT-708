package com.example.lostfoundapp.database;

public class LostFoundItem {
    private long id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String location;
    private String imagePath;
    private String category;
    private String timestamp;
    private double latitude;
    private double longitude;

    public LostFoundItem() {}

    public LostFoundItem(String postType, String name, String phone, String description,
                         String date, String location, String imagePath,
                         String category, String timestamp) {
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.imagePath = imagePath;
        this.category = category;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
