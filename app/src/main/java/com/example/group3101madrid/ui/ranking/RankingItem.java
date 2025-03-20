package com.example.group3101madrid.ui.ranking;

public class RankingItem {
    private int position;
    private String name;
    private int score;
    private Integer profileImage; // Resource ID for profile image
    private String profileImageUrl; // URL for profile image

    // Constructor for resource ID-based profile image
    public RankingItem(int position, String name, int score, Integer profileImage) {
        this.position = position;
        this.name = name;
        this.score = score;
        this.profileImage = profileImage;
        this.profileImageUrl = null;
    }

    // Constructor for URL-based profile image
    public RankingItem(int position, String name, int score, String profileImageUrl) {
        this.position = position;
        this.name = name;
        this.score = score;
        this.profileImage = null;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters
    public int getPosition() { return position; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public Integer getProfileImage() { return profileImage; }
    public String getProfileImageUrl() { return profileImageUrl; }

    // Setters
    public void setPosition(int position) { this.position = position; }
}