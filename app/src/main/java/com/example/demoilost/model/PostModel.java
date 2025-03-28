package com.example.demoilost.model;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.PropertyName;

public class PostModel {
    private String postId;
    private String posterId;
    private String imageUrl;
    private String title;
    private String location;
    private String description;

    public PostModel() {} // Required by Firestore

    public PostModel(String postId, String posterId, String imageUrl, String title, String location, String description) {
        this.postId = postId;
        this.posterId = posterId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.location = location;
        this.description = description;
    }

    // Getters and setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getPosterId() { return posterId; }
    public void setPosterId(String posterId) { this.posterId = posterId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

