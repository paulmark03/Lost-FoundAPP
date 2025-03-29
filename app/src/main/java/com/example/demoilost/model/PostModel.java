package com.example.demoilost.model;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

public class PostModel {
    private String postId;
    private String posterId;
    private String imageUrl;
    private String title;
    private GeoPoint location;
    private String description;
    private String address;




    public PostModel() {} // Required by Firestore

    public PostModel(String postId, String posterId, String imageUrl, String title, GeoPoint location, String description) {
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

    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}
}

