package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.Perfil.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DesafioDetailActivity extends AppCompatActivity {

    private static final String TAG = "DesafioDetail";

    private ImageView headerImage;
    private TextView titleText;
    private TextView descriptionText;
    private TextView locationText;
    private Button startButton;
    private Button cancelButton;
    private ImageButton btnHomeNav;
    private ImageButton btnProfileNav;
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

        if (desafioType == null) {
            Log.e(TAG, "No desafio type specified in intent");
            Toast.makeText(this, "Error: No se especificó el tipo de desafío", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading desafio: " + desafioType);

        // Initialize views
        headerImage = findViewById(R.id.header_image);
        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        locationText = findViewById(R.id.location_text);
        startButton = findViewById(R.id.start_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Initialize navigation buttons
        btnHomeNav = findViewById(R.id.btnHomeNav);
        btnProfileNav = findViewById(R.id.btnProfileNav);

        // Set navigation buttons click listeners
        btnHomeNav.setOnClickListener(v -> {
            Intent intent = new Intent(DesafioDetailActivity.this, Main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnProfileNav.setOnClickListener(v -> {
            Intent intent = new Intent(DesafioDetailActivity.this, Perfil.class);
            startActivity(intent);
        });

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
                        // Update button text for active desafios
                        startButton.setText("Ver Experiencias");
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
                        // If already active, just open the experiencias list
                        openExperienciasList();
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
                            Toast.makeText(DesafioDetailActivity.this,
                                    "¡Desafío activado con éxito!",
                                    Toast.LENGTH_SHORT).show();
                            openExperienciasList();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(DesafioDetailActivity.this,
                                        "Error al iniciar el desafío: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DesafioDetailActivity.this,
                        "Error comprobando estado del desafío: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openExperienciasList() {
        Intent intent = new Intent(DesafioDetailActivity.this, SimpleExperienciasActivity.class);
        intent.putExtra("DESAFIO_TYPE", desafioType);
        intent.putExtra("DESAFIO_TITLE", titleText.getText().toString());
        startActivity(intent);
    }

    private void loadDesafioData(String desafioType) {
        Log.d(TAG, "Loading desafio data for: " + desafioType);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios").child(desafioType);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get existing data
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Get additional info
                    String category = dataSnapshot.child("category").getValue(String.class);
                    Integer totalExperiencias = dataSnapshot.child("totalExperiencias").getValue(Integer.class);

                    // Count completed experiences
                    if (currentUser != null) {
                        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                                .child("user_progress")
                                .child(currentUser.getUid())
                                .child(desafioType)
                                .child("experiencias");

                        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int count = 0;
                                for (DataSnapshot expSnapshot : snapshot.getChildren()) {
                                    if (Boolean.TRUE.equals(expSnapshot.getValue(Boolean.class))) {
                                        count++;
                                    }
                                }

                                // Update UI with completion info
                                updateCompletionStatus(count, totalExperiencias != null ? totalExperiencias : 0);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error loading completion data: " + error.getMessage());
                            }
                        });
                    }

                    // Update UI
                    titleText.setText(title != null ? title : "");

                    // Format the description with additional info
                    StringBuilder fullDescription = new StringBuilder();
                    fullDescription.append(description != null ? description : "");

                    if (category != null) {
                        fullDescription.append("\n\nCategoría: ").append(category);
                    }

                    if (totalExperiencias != null) {
                        fullDescription.append("\nTotal de experiencias: ").append(totalExperiencias);
                    }

                    descriptionText.setText(fullDescription.toString());
                    locationText.setText(location != null ? "Ubicación: " + location : "");

                    // Load image
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            // Try to get the resource ID
                            int resourceId = getResources().getIdentifier(
                                    imageUrl,  // drawable name from Firebase
                                    "drawable",     // resource type
                                    getPackageName()
                            );

                            if (resourceId != 0) {  // Resource found
                                Glide.with(DesafioDetailActivity.this)
                                        .load(resourceId)
                                        .into(headerImage);
                            } else if (imageUrl.startsWith("http")) {  // URL
                                Glide.with(DesafioDetailActivity.this)
                                        .load(imageUrl)
                                        .into(headerImage);
                            } else {  // Resource not found, load default image
                                headerImage.setImageResource(R.drawable.kilomarathon);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                            // If there's any error, load default image
                            headerImage.setImageResource(R.drawable.kilomarathon);
                        }
                    } else {
                        // No image URL provided, load default
                        headerImage.setImageResource(R.drawable.kilomarathon);
                    }
                } else {
                    Log.e(TAG, "No data found for desafio: " + desafioType);
                    Toast.makeText(DesafioDetailActivity.this,
                            "No se encontraron datos para este desafío",
                            Toast.LENGTH_SHORT).show();
                }
            }

            // Add this method to your DesafioDetailActivity class
            private void updateCompletionStatus(int completed, int total) {
                // Find TextView that we'll use to display completion status
                TextView tvCompletionStatus = findViewById(R.id.tvCompletionStatus);

                // If we have a progress bar, update it too
                androidx.core.widget.ContentLoadingProgressBar progressBar = findViewById(R.id.progressCompletion);

                if (tvCompletionStatus != null) {
                    String completionText = "Completado: " + completed + " / " + total;
                    tvCompletionStatus.setText(completionText);

                    // Make sure the TextView is visible
                    tvCompletionStatus.setVisibility(View.VISIBLE);
                }

                if (progressBar != null) {
                    progressBar.setMax(total);
                    progressBar.setProgress(completed);
                }

                // You can also update the button text if all experiences are completed
                if (completed == total && total > 0) {
                    startButton.setText("Desafío Completado");
                    // Optionally disable the button or change its appearance
                    // startButton.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading desafio data: " + databaseError.getMessage());
                Toast.makeText(DesafioDetailActivity.this,
                        "Error cargando datos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}