package com.example.group3101madrid.ui.ranking;

public class RankingItem {
    private int position;
    private String name;
    private int score;
    private Integer profileImage; // Resource ID for profile image

    public RankingItem(int position, String name, int score, Integer profileImage) {
        this.position = position;
        this.name = name;
        this.score = score;
        this.profileImage = profileImage;
    }

    // Getters
    public int getPosition() { return position; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public Integer getProfileImage() { return profileImage; }
}
