package com.example.demoilost.model;

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

    // Required by Firestore
    public PostModel() {}

    public PostModel(String postId, String posterId, String imageUrl, String title,
                     GeoPoint location, String description, String address) {
        this.postId = postId;
        this.posterId = posterId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.location = location;
        this.description = description;
        this.address = address;
    }

    @PropertyName("postId")
    public String getPostId() {
        return postId;
    }

    @PropertyName("postId")
    public void setPostId(String postId) {
        this.postId = postId;
    }

    @PropertyName("posterId")
    public String getPosterId() {
        return posterId;
    }

    @PropertyName("posterId")
    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    @PropertyName("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("location")
    public GeoPoint getLocation() {
        return location;
    }

    @PropertyName("location")
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("address")
    public String getAddress() {
        return address;
    }

    @PropertyName("address")
    public void setAddress(String address) {
        this.address = address;
    }
}
