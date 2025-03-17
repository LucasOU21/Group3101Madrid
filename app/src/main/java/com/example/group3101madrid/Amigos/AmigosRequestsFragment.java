// AmigosRequestsFragment.java
package com.example.group3101madrid.Amigos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.Amigos.FriendRequest;
import com.example.group3101madrid.Amigos.FriendRequestAdapter;
import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AmigosRequestsFragment extends Fragment implements FriendRequestAdapter.FriendRequestListener {

    private RecyclerView rvFriendRequests;
    private TextView tvEmptyRequests;
    private FriendRequestAdapter adapter;
    private List<FriendRequest> requestsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amigos_requests, container, false);

        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvEmptyRequests = view.findViewById(R.id.tvEmptyRequests);

        // Initialize RecyclerView
        requestsList = new ArrayList<>();
        adapter = new FriendRequestAdapter(requestsList, getContext(), this);
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendRequests.setAdapter(adapter);

        // Load friend requests
        loadFriendRequests();

        return view;
    }

    private void loadFriendRequests() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                .child("friend_requests")
                .child(currentUserId);

        requestsRef.orderByChild("status").equalTo("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestsList.clear();

                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    FriendRequest request = requestSnapshot.getValue(FriendRequest.class);
                    if (request != null) {
                        requestsList.add(request);
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateEmptyState() {
        if (requestsList.isEmpty()) {
            tvEmptyRequests.setVisibility(View.VISIBLE);
            rvFriendRequests.setVisibility(View.GONE);
        } else {
            tvEmptyRequests.setVisibility(View.GONE);
            rvFriendRequests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAcceptRequest(FriendRequest request) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderId = request.getSenderId();

        // Update request status
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("friend_requests")
                .child(currentUserId)
                .child(senderId);

        requestRef.child("status").setValue("accepted");

        // Add to friends list (both ways)
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(currentUserId)
                .child(senderId);

        DatabaseReference senderFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(senderId)
                .child(currentUserId);

        currentUserFriendsRef.setValue(true);
        senderFriendsRef.setValue(true);

        Toast.makeText(getContext(), "Solicitud aceptada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRejectRequest(FriendRequest request) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderId = request.getSenderId();

        // Update request status
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("friend_requests")
                .child(currentUserId)
                .child(senderId);

        requestRef.child("status").setValue("rejected");

        Toast.makeText(getContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show();
    }
}