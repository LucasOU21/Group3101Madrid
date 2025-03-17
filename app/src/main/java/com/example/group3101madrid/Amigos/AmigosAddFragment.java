// AmigosAddFragment.java (continued)
package com.example.group3101madrid.Amigos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.R;
import com.example.group3101madrid.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmigosAddFragment extends Fragment implements UserSearchAdapter.UserSearchListener {

    private EditText etSearchUsername;
    private Button btnSearch;
    private ProgressBar progressSearch;
    private RecyclerView rvSearchResults;
    private Button btnInviteContacts;

    private UserSearchAdapter searchAdapter;
    private List<User> searchResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amigos_add, container, false);

        etSearchUsername = view.findViewById(R.id.etSearchUsername);
        btnSearch = view.findViewById(R.id.btnSearch);
        progressSearch = view.findViewById(R.id.progressSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        btnInviteContacts = view.findViewById(R.id.btnInviteContacts);

        // Initialize RecyclerView
        searchResults = new ArrayList<>();
        searchAdapter = new UserSearchAdapter(searchResults, getContext(), this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(searchAdapter);

        // Set click listeners
        btnSearch.setOnClickListener(v -> searchUser());
        btnInviteContacts.setOnClickListener(v -> inviteFromContacts());

        return view;
    }

    private void searchUser() {
        String username = etSearchUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(getContext(), "Introduce un nombre de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        progressSearch.setVisibility(View.VISIBLE);
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();

        // Get current user ID to exclude from results
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        // Search users by username
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = usersRef.orderByChild("username").startAt(username).endAt(username + "\uf8ff").limitToFirst(10);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    // Skip current user
                    if (userId.equals(currentUserId)) {
                        continue;
                    }

                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // Set user ID
                        user.setId(userId);
                        searchResults.add(user);
                    }
                }

                searchAdapter.notifyDataSetChanged();
                progressSearch.setVisibility(View.GONE);

                if (searchResults.isEmpty()) {
                    Toast.makeText(getContext(), "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressSearch.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error al buscar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inviteFromContacts() {
        // Generate app invitation message
        String inviteMessage = "¡Únete a '101 Experiencias en Madrid' y descubre la ciudad con amigos! Descarga la app aquí: [LINK DE DESCARGA]";

        // Create invite intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, inviteMessage);

        // Show app chooser
        startActivity(Intent.createChooser(intent, "Invitar amigos"));
    }

    @Override
    public void onSendFriendRequest(User user) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        // Check if there's already a friend request
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                .child("friend_requests")
                .child(user.getId())
                .child(currentUserId);

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "Ya has enviado una solicitud a este usuario", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get current user info
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId);

                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String profilePic = dataSnapshot.child("profile_picture").getValue(String.class);

                        // Create friend request object
                        Map<String, Object> requestData = new HashMap<>();
                        requestData.put("senderId", currentUserId);
                        requestData.put("senderUsername", username);
                        requestData.put("senderProfilePictureUrl", profilePic);
                        requestData.put("timestamp", ServerValue.TIMESTAMP);
                        requestData.put("status", "pending");

                        // Save to recipient's friend_requests
                        requestsRef.setValue(requestData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error al obtener información del usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error al comprobar solicitudes existentes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}