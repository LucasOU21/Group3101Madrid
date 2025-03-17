// Update User.java
package com.example.group3101madrid;

public class User {

    private String id;
    private String username;
    private String email;
    private String profilePicture;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String username, String email) {
        this.email = email;
        this.username = username;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}