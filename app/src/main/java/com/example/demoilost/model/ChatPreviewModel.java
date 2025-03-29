package com.example.demoilost.model;

public class ChatPreviewModel {

    private String chatId;
    private String postId;
    private String founderId;
    private String userId;
    private String lastMessage; // ✅ ADD THIS

    // Empty constructor for Firestore
    public ChatPreviewModel() {}

    // Getters and Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getFounderId() {
        return founderId;
    }

    public void setFounderId(String founderId) {
        this.founderId = founderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMessage() {   // ✅ GETTER
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) { // ✅ SETTER
        this.lastMessage = lastMessage;
    }
}
