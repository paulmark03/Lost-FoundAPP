package com.example.demoilost.model;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.PropertyName;

public class PostModel {
    private String name;
    private String location;
    private String description;

    // Map the Firestore field "image" to this property
    @PropertyName("image")
    private Blob imageBlob;

    // Empty constructor required by Firestore
    public PostModel() {}

    // Getters and setters for name, location, description...
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // Use the @PropertyName annotation on the getter and setter
    @PropertyName("image")
    public Blob getImageBlob() {
        return imageBlob;
    }
    @PropertyName("image")
    public void setImageBlob(Blob imageBlob) {
        this.imageBlob = imageBlob;
    }
}
