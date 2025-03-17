
package com.example.group3101madrid.Amigos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AmigosListFragment extends Fragment {

    private RecyclerView rvAmigos;
    private TextView tvEmptyState;
    private AmigosAdapter adapter;
    private List<Friend> friendsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amigos_list, container, false);

        rvAmigos = view.findViewById(R.id.rvAmigos);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Initialize RecyclerView
        friendsList = new ArrayList<>();
        adapter = new AmigosAdapter(friendsList, getContext());
        rvAmigos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAmigos.setAdapter(adapter);

        // Load friends
        loadFriends();

        return view;
    }

    private void loadFriends() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(currentUserId);

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    Boolean isAccepted = friendSnapshot.getValue(Boolean.class);

                    if (Boolean.TRUE.equals(isAccepted)) {
                        // Load user details
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(friendId);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    String username = userSnapshot.child("username").getValue(String.class);
                                    String profilePic = userSnapshot.child("profile_picture").getValue(String.class);
                                    String status = "offline"; // Default status

                                    Friend friend = new Friend(friendId, username, profilePic, status);
                                    friendsList.add(friend);
                                    adapter.notifyDataSetChanged();

                                    // Update empty state visibility
                                    updateEmptyState();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }

                // Update empty state visibility
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateEmptyState() {
        if (friendsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvAmigos.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvAmigos.setVisibility(View.VISIBLE);
        }
    }
}