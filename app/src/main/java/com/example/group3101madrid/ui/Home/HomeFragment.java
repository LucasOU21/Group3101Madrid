
package com.example.group3101madrid.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.DesafioDetailActivity;
import com.example.group3101madrid.Perfil.Perfil;
import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {
    private ImageButton profileButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_home, container, false);

        // Setup profile button click
        profileButton = view.findViewById(R.id.imageBtnPerfil);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Perfil.class);
            startActivity(intent);
        });

        // Load the profile picture
        loadProfilePicture();

        // Setup all containers with their respective types
        setupClickableContainer(view, R.id.container_kilometros, "kilometros");
        setupClickableContainer(view, R.id.container_canchas, "canchas");
        setupClickableContainer(view, R.id.container_fuentes, "fuentes");
        setupClickableContainer(view, R.id.container_monumentos, "monumentos");
        setupClickableContainer(view, R.id.container_playas, "playas");
        setupClickableContainer(view, R.id.container_museos, "museos");
        setupClickableContainer(view, R.id.container_poesia, "poesia");
        setupClickableContainer(view, R.id.container_jardines, "jardines");
        setupClickableContainer(view, R.id.container_mercados, "mercados");
        setupClickableContainer(view, R.id.container_festivales, "festivales");
        setupClickableContainer(view, R.id.container_teatros, "teatros");
        setupClickableContainer(view, R.id.container_galerias, "galerias");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile picture when fragment resumes
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || profileButton == null) return;

        // Get reference to the user's profile picture URL
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("profile_picture");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty() && getContext() != null) {
                    // Load image with Glide
                    Glide.with(getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.foto_perfil_ejemplo)
                            .error(R.drawable.foto_perfil_ejemplo)
                            .into(profileButton);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeFragment", "Error loading profile picture: " + databaseError.getMessage());
            }
        });
    }

    private void setupClickableContainer(View view, int containerId, final String desafioType) {
        LinearLayout container = view.findViewById(containerId);
        container.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DesafioDetailActivity.class);
            intent.putExtra("DESAFIO_TYPE", desafioType);
            startActivity(intent);
        });
    }
}