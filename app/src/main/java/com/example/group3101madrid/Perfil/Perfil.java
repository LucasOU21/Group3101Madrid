package com.example.group3101madrid.Perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group3101madrid.Grupos.Grupos;
import com.example.group3101madrid.Login;
import com.example.group3101madrid.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Perfil extends AppCompatActivity {

    private Button btnSignOut;
    private Button btnGrupos;
    private TextView tvUsername;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        findViewById(R.id.ivBackButton).setOnClickListener(view -> {
            finish(); // This will close the current activity and return to the previous screen
        });

        // Active challenges click listener
        findViewById(R.id.llActiveDesafios).setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, DesafiosActivo.class);
            startActivity(intent);
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        btnSignOut = findViewById(R.id.btnSignOut);
        btnGrupos = findViewById(R.id.btnGrupos);
        tvUsername = findViewById(R.id.textView5);

        // Set user display name
        updateUserInterface();

        btnSignOut.setOnClickListener(view -> handleSignOut());

        btnGrupos.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Grupos.class);
            startActivity(intent);
        });
    }

    private void updateUserInterface() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's display name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUsername.setText(displayName);
            } else {
                // If no display name, use email
                String email = currentUser.getEmail();
                if (email != null) {
                    // Remove the @domain.com part
                    tvUsername.setText(email.substring(0, email.indexOf('@')));
                }
            }
        }
    }

    private void loadUserProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid());

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous progress views
                LinearLayout progressContainer = findViewById(R.id.progress_container);
                progressContainer.removeAllViews();

                for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                    String type = progressSnapshot.child("type").getValue(String.class);
                    String status = progressSnapshot.child("status").getValue(String.class);

                    if (status != null && status.equals("active")) {
                        // Create and add progress view
                        addProgressView(progressContainer, type);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Perfil.this,
                        "Error loading progress: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProgressView(LinearLayout container, String type) {
        View progressView = getLayoutInflater().inflate(R.layout.item_progress, container, false);

        ImageView iconView = progressView.findViewById(R.id.progress_icon);
        TextView titleView = progressView.findViewById(R.id.progress_title);

        // Set appropriate icon and title based on type
        switch (type) {
            case "croquetas":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Croquetas - En progreso");
                break;
            case "bares":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Bares - En progreso");
                break;
            case "paisajes":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Paisajes - En progreso");
                break;
        }

        container.addView(progressView);
    }

    private void handleSignOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Check if user signed in with Google
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                // Sign out from Google
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    mAuth.signOut();
                    returnToLogin();
                });
            } else {
                // Regular Firebase sign out
                mAuth.signOut();
                returnToLogin();
            }
        }
    }

    private void returnToLogin() {
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}