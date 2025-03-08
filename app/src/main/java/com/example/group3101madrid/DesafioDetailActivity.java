package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.ui.mapa.laMapaFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DesafioDetailActivity extends AppCompatActivity {

    private ImageView headerImage;
    private TextView titleText;
    private TextView descriptionText;
    private TextView locationText;
    private Button startButton;
    private Button cancelButton;
    private String desafioType;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desafio_detail);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get the desafio type from intent
        desafioType = getIntent().getStringExtra("DESAFIO_TYPE");

        // Initialize views
        headerImage = findViewById(R.id.header_image);
        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        locationText = findViewById(R.id.location_text);
        startButton = findViewById(R.id.start_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Load data from Firebase
        loadDesafioData(desafioType);

        // Check if desafio is already active when loading data
        if (currentUser != null) {
            checkDesafioStatus();
        }

        // Set up button listeners
        startButton.setOnClickListener(v -> {
            if (desafioType != null) {
                startDesafio(desafioType);
            } else {
                Toast.makeText(this, "Error: No se especificó el tipo de desafío", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void checkDesafioStatus() {
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid())
                .child(desafioType);

        userProgressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null && status.equals("active")) {
                        // Disable button and change its appearance
                        startButton.setEnabled(false);
                        startButton.setAlpha(0.5f);
                    } else {
                        startButton.setEnabled(true);
                        startButton.setAlpha(1.0f);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DesafioDetailActivity.this,
                        "Error comprobando estado del desafío: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startDesafio(String desafioType) {
        if (currentUser == null) {
            Toast.makeText(this, "Por favor, inicia sesión primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if desafio is already active before starting
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid())
                .child(desafioType);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null && status.equals("active")) {
                        Toast.makeText(DesafioDetailActivity.this,
                                "¡Este desafío ya está activado!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // If not active, proceed with starting the desafio
                Map<String, Object> progressData = new HashMap<>();
                progressData.put("startDate", ServerValue.TIMESTAMP);
                progressData.put("status", "active");
                progressData.put("type", desafioType);

                userProgressRef.setValue(progressData)
                        .addOnSuccessListener(aVoid -> {
                            // Add logging
                            Log.d("DesafioDebug", "Starting MapActivity with desafioType: " + desafioType);

                            Intent mapIntent = new Intent(DesafioDetailActivity.this, com.example.group3101madrid.MapActivity.class);
                            mapIntent.putExtra("DESAFIO_TYPE", desafioType);
                            mapIntent.putExtra("SHOW_MARKER", true);
                            startActivity(mapIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DesafioDebug", "Failed to start desafio: " + e.getMessage());
                            Toast.makeText(DesafioDetailActivity.this,
                                    "Error al iniciar el desafío: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DesafioDetailActivity.this,
                        "Error comprobando estado del desafío: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDesafioData(String desafioType) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios").child(desafioType);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get data from snapshot
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String imageResource = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Update UI
                    titleText.setText(title);
                    descriptionText.setText(description);
                    locationText.setText(location);

                    try {
                        // Try to get the resource ID
                        int resourceId = getResources().getIdentifier(
                                imageResource,  // drawable name from Firebase
                                "drawable",     // resource type
                                getPackageName()
                        );

                        if (resourceId != 0) {  // Resource found
                            Glide.with(DesafioDetailActivity.this)
                                    .load(resourceId)
                                    .into(headerImage);
                        } else {  // Resource not found, load default image
                            headerImage.setImageResource(R.drawable.kilomarathon);  // Your default image
                        }
                    } catch (Exception e) {
                        // If there's any error, load default image
                        headerImage.setImageResource(R.drawable.kilomarathon);
                        Toast.makeText(DesafioDetailActivity.this,
                                "Error cargando la imagen: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DesafioDetailActivity.this,
                        "Error cargando datos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}