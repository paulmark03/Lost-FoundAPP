package com.example.demoilost.model;

import java.util.Date;

public class Message {
    private String senderId;
    private String text;
    private Date timestamp;

    // Empty constructor required for Firestore
    public Message() {}

    public Message(String senderId, String text, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
