// FriendRequest.java
package com.example.group3101madrid.Amigos;

public class FriendRequest {
    private String senderId;
    private String senderUsername;
    private String senderProfilePictureUrl;
    private long timestamp;
    private String status; // pending, accepted, rejected

    public FriendRequest() {
        // Required empty constructor for Firebase
    }

    public FriendRequest(String senderId, String senderUsername, String senderProfilePictureUrl, long timestamp) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderProfilePictureUrl = senderProfilePictureUrl;
        this.timestamp = timestamp;
        this.status = "pending";
    }

    // Getters and setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getSenderProfilePictureUrl() { return senderProfilePictureUrl; }
    public void setSenderProfilePictureUrl(String senderProfilePictureUrl) { this.senderProfilePictureUrl = senderProfilePictureUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}