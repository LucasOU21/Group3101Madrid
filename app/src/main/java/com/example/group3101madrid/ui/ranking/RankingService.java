package com.example.group3101madrid.ui.ranking;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.group3101madrid.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple service to calculate user rankings based on completed experience points
 */
public class RankingService {
    private static final String TAG = "RankingService";

    /**
     * Get sorted ranking data for all users
     * @param callback callback to receive the ranking list
     */
    public static void getRankingData(RankingCallback callback) {
        Log.d(TAG, "Starting to load ranking data");

        // Maps to store user data and points
        Map<String, String> userNames = new HashMap<>();
        Map<String, String> userProfilePics = new HashMap<>();
        Map<String, Integer> userPoints = new HashMap<>();

        // Get Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        // Fetch all users first
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Users data snapshot received. Child count: " + dataSnapshot.getChildrenCount());

                // Process all users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId == null) continue;

                    Log.d(TAG, "Processing user: " + userId);

                    // Get username (default to "Usuario" if not found)
                    String username = userSnapshot.child("username").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = "Usuario";
                    }
                    userNames.put(userId, username);
                    Log.d(TAG, "Username: " + username);

                    // Get profile picture
                    String profilePic = userSnapshot.child("profile_picture").getValue(String.class);
                    if (profilePic != null) {
                        userProfilePics.put(userId, profilePic);
                        Log.d(TAG, "Profile pic found");
                    } else {
                        Log.d(TAG, "No profile pic found");
                    }

                    // Initialize points for this user
                    userPoints.put(userId, 0);
                }

                Log.d(TAG, "Found " + userNames.size() + " users, now loading progress data");

                // Now fetch all user progress data to calculate points
                DatabaseReference progressRef = database.getReference("user_progress");
                progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot progressSnapshot) {
                        Log.d(TAG, "Progress data snapshot received. Child count: " + progressSnapshot.getChildrenCount());

                        // Process all users' progress
                        for (DataSnapshot userSnapshot : progressSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();

                            // Even if user is not in userPoints map, create an entry for them
                            if (userId == null) continue;

                            Log.d(TAG, "Processing progress for user: " + userId);

                            // If user not in our names map, add with a default name
                            if (!userNames.containsKey(userId)) {
                                userNames.put(userId, "Usuario");
                                userPoints.put(userId, 0);
                                Log.d(TAG, "Adding missing user with default name");
                            }

                            // Track total points for this user
                            int totalPoints = 0;

                            // Iterate through each challenge for this user
                            for (DataSnapshot challengeSnapshot : userSnapshot.getChildren()) {
                                String challengeType = challengeSnapshot.getKey();
                                if (challengeType == null) continue;

                                Log.d(TAG, "Processing challenge: " + challengeType);

                                // Check if this challenge has experiences
                                if (challengeSnapshot.hasChild("experiencias")) {
                                    // Get all completed experiences
                                    for (DataSnapshot expSnapshot : challengeSnapshot.child("experiencias").getChildren()) {
                                        if (Boolean.TRUE.equals(expSnapshot.getValue(Boolean.class))) {
                                            // Add points per completed experience
                                            totalPoints += 1;
                                            Log.d(TAG, "Found completed experience, total: " + totalPoints);
                                        }
                                    }
                                }
                            }

                            // Store total points for this user
                            userPoints.put(userId, totalPoints);
                            Log.d(TAG, "Final points for " + userId + ": " + totalPoints);
                        }

                        // Create ranking list
                        List<RankingItem> rankingList = new ArrayList<>();

                        // Add all users who have points
                        for (Map.Entry<String, Integer> entry : userPoints.entrySet()) {
                            String userId = entry.getKey();
                            Integer points = entry.getValue();

                            // Only add users with at least 1 point
                            if (points > 0) {
                                String name = userNames.get(userId);
                                String profilePic = userProfilePics.get(userId);

                                Log.d(TAG, "Adding to ranking: " + name + " with " + points + " points");

                                // Create ranking item
                                RankingItem item;
                                if (profilePic != null) {
                                    item = new RankingItem(0, name, points, profilePic);
                                } else {
                                    item = new RankingItem(0, name, points, R.drawable.foto_perfil_ejemplo);
                                }

                                rankingList.add(item);
                            }
                        }

                        Log.d(TAG, "Final ranking list size: " + rankingList.size());

                        // Sort by points (highest first)
                        Collections.sort(rankingList, (a, b) -> Integer.compare(b.getScore(), a.getScore()));

                        // Assign positions
                        for (int i = 0; i < rankingList.size(); i++) {
                            rankingList.get(i).setPosition(i + 1);
                        }

                        // Return the sorted list
                        callback.onRankingDataReceived(rankingList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error loading progress: " + databaseError.getMessage());
                        callback.onRankingDataFailed(databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading users: " + databaseError.getMessage());
                callback.onRankingDataFailed(databaseError.getMessage());
            }
        });
    }

    /**
     * Callback interface for ranking data
     */
    public interface RankingCallback {
        void onRankingDataReceived(List<RankingItem> rankingList);
        void onRankingDataFailed(String errorMessage);
    }
}