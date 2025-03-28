package com.example.demoilost.model;

import java.util.Date;

public class Message {
    private String senderId;
    private String text;           // Optional (for text messages)
    private String imageUrl;       // Optional (for image messages)
    private String messageType;    // "text", "image"
    private Date timestamp;

    // Required for Firestore
    public Message() {}

    public Message(String senderId, String text, String imageUrl, String messageType, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
