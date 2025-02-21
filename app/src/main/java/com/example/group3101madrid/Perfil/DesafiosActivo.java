package com.example.group3101madrid.Perfil;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.example.group3101madrid.databinding.ActivityActiveChallengesBinding;
import java.util.ArrayList;
import java.util.List;

public class DesafiosActivo extends AppCompatActivity {

    private ActivityActiveChallengesBinding binding;
    private ActiveChallengesAdapter adapter;
    private List<Desafio> challengeList;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActiveChallengesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Add this click listener for the back button
        binding.backButton.setOnClickListener(view -> {
            finish(); // This will close current activity and return to previous one
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        challengeList = new ArrayList<>();

        binding.recyclerViewChallenges.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveChallengesAdapter(challengeList);
        binding.recyclerViewChallenges.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        challengeList = new ArrayList<>();

        binding.recyclerViewChallenges.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveChallengesAdapter(challengeList);
        binding.recyclerViewChallenges.setAdapter(adapter);

        loadActiveChallenges();
    }

    private void loadActiveChallenges() {
        if (currentUser == null) return;

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid());

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                challengeList.clear();

                for (DataSnapshot challengeSnapshot : dataSnapshot.getChildren()) {
                    String type = challengeSnapshot.child("type").getValue(String.class);
                    String status = challengeSnapshot.child("status").getValue(String.class);
                    Long startDate = challengeSnapshot.child("startDate").getValue(Long.class);

                    if (status != null && status.equals("active")) {
                        Desafio challenge = new Desafio(type, startDate);
                        challengeList.add(challenge);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DesafiosActivo.this,
                        "Error loading challenges: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}