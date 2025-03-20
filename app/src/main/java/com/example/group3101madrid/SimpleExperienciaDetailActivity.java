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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.Perfil.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SimpleExperienciaDetailActivity extends AppCompatActivity {

    private static final String TAG = "ExperienciaDetail";

    private String experienciaId;
    private String desafioType;
    private String title;
    private String description;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private boolean completed;
    private String address;
    private String hours;
    private String tips;
    private String category;

    private ImageView ivBack;
    private ImageView ivImage;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvAddress;
    private TextView tvHours;
    private TextView tvTips;
    private TextView tvCategory;
    private Button btnOpenMap;
    private Button btnComplete;
    private ImageButton btnHomeNav;
    private ImageButton btnProfileNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_experiencia_detail);

        // Get data from intent
        experienciaId = getIntent().getStringExtra("EXPERIENCIA_ID");
        desafioType = getIntent().getStringExtra("DESAFIO_TYPE");
        title = getIntent().getStringExtra("TITLE");
        description = getIntent().getStringExtra("DESCRIPTION");
        imageUrl = getIntent().getStringExtra("IMAGE_URL");
        latitude = getIntent().getDoubleExtra("LATITUDE", 0);
        longitude = getIntent().getDoubleExtra("LONGITUDE", 0);
        completed = getIntent().getBooleanExtra("COMPLETED", false);

        // Get additional data
        address = getIntent().getStringExtra("ADDRESS");
        hours = getIntent().getStringExtra("HOURS");
        tips = getIntent().getStringExtra("TIPS");
        category = getIntent().getStringExtra("CATEGORY");
        int puntos = getIntent().getIntExtra("PUNTOS", 1);

        // Initialize views including a new TextView for puntos
        TextView tvPuntos = findViewById(R.id.tvPuntos);

        // Set the puntos value with a visual indicator of difficulty
        String dificultad;
        switch(puntos) {
            case 1:
                dificultad = "Muy fácil";
                break;
            case 2:
                dificultad = "Fácil";
                break;
            case 3:
                dificultad = "Media";
                break;
            case 4:
                dificultad = "Difícil";
                break;
            case 5:
                dificultad = "Muy difícil";
                break;
            default:
                dificultad = "Fácil";
        }

        tvPuntos.setText("Dificultad: " + dificultad + " (" + puntos + " puntos)");

        if (experienciaId == null || desafioType == null) {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        ivImage = findViewById(R.id.ivImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvAddress = findViewById(R.id.tvAddress);
        tvHours = findViewById(R.id.tvHours);
        tvTips = findViewById(R.id.tvTips);
        tvCategory = findViewById(R.id.tvCategory);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnComplete = findViewById(R.id.btnComplete);

        // Initialize navigation buttons
        btnHomeNav = findViewById(R.id.btnHomeNav);
        btnProfileNav = findViewById(R.id.btnProfileNav);

        // Set data
        tvTitle.setText(title);
        tvDescription.setText(description != null ? description : "");

        // Set additional data if available
        if (address != null && !address.isEmpty()) {
            tvAddress.setText("Dirección: " + address);
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }

        if (hours != null && !hours.isEmpty()) {
            tvHours.setText("Horario: " + hours);
            tvHours.setVisibility(View.VISIBLE);
        } else {
            tvHours.setVisibility(View.GONE);
        }

        if (tips != null && !tips.isEmpty()) {
            tvTips.setText("Consejos: " + tips);
            tvTips.setVisibility(View.VISIBLE);
        } else {
            tvTips.setVisibility(View.GONE);
        }

        if (category != null && !category.isEmpty()) {
            tvCategory.setText("Categoría: " + category);
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        updateCompletedStatus();

        // Load image
        loadImage();

        // Set click listeners
        ivBack.setOnClickListener(v -> finish());

        // Set navigation buttons click listeners
        btnHomeNav.setOnClickListener(v -> {
            Intent intent = new Intent(SimpleExperienciaDetailActivity.this, Main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnProfileNav.setOnClickListener(v -> {
            Intent intent = new Intent(SimpleExperienciaDetailActivity.this, Perfil.class);
            startActivity(intent);
        });

        btnOpenMap.setOnClickListener(v -> openInMaps());

        btnComplete.setOnClickListener(v -> {
            if (!completed) {
                markAsCompleted();
            }
        });
    }

    private void loadImage() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Try as resource
                int resourceId = getResources().getIdentifier(
                        imageUrl,
                        "drawable",
                        getPackageName()
                );

                if (resourceId != 0) {
                    Glide.with(this)
                            .load(resourceId)
                            .centerCrop()
                            .into(ivImage);
                } else if (imageUrl.startsWith("http")) {
                    // Try as URL
                    Glide.with(this)
                            .load(imageUrl)
                            .centerCrop()
                            .into(ivImage);
                } else {
                    // Default image
                    ivImage.setImageResource(R.drawable.img_101_logo);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                // Default image
                ivImage.setImageResource(R.drawable.img_101_logo);
            }
        } else {
            // Default image
            ivImage.setImageResource(R.drawable.img_101_logo);
        }
    }

    private void updateCompletedStatus() {
        if (completed) {
            btnComplete.setText("Completado");
            btnComplete.setEnabled(false);
            btnComplete.setAlpha(0.5f);
        } else {
            btnComplete.setText("Completar");
            btnComplete.setEnabled(true);
            btnComplete.setAlpha(1.0f);
        }
    }

    private void openInMaps() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "No hay coordenadas disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open internal map activity
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("DESAFIO_TYPE", desafioType);
        intent.putExtra("SHOW_MARKER", true);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        intent.putExtra("EXPERIENCIA_ID", experienciaId);
        intent.putExtra("EXPERIENCIA_TITLE", title); // Add this line
        startActivity(intent);
    }

    private void markAsCompleted() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid())
                .child(desafioType)
                .child("experiencias")
                .child(experienciaId);

        progressRef.setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Experiencia completada!", Toast.LENGTH_SHORT).show();
                    completed = true;
                    updateCompletedStatus();

                    // Check if we should update the challenge progress
                    checkChallengeCompletion(currentUser.getUid(), desafioType);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking as completed: " + e.getMessage());
                    Toast.makeText(this,
                            "Error al marcar como completada: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkChallengeCompletion(String userId, String desafioType) {
        // Get total experiencias in this challenge
        DatabaseReference desafioRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios")
                .child(desafioType)
                .child("totalExperiencias");

        desafioRef.get().addOnSuccessListener(dataSnapshot -> {
            Integer total = dataSnapshot.getValue(Integer.class);
            if (total == null) return;

            // Get completed experiencias
            DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                    .child("user_progress")
                    .child(userId)
                    .child(desafioType)
                    .child("experiencias");

            progressRef.get().addOnSuccessListener(snapshot -> {
                int completed = 0;
                for (com.google.firebase.database.DataSnapshot expSnapshot : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(expSnapshot.getValue(Boolean.class))) {
                        completed++;
                    }
                }

                // If all completed, update challenge status
                if (completed >= total) {
                    DatabaseReference challengeRef = FirebaseDatabase.getInstance().getReference()
                            .child("user_progress")
                            .child(userId)
                            .child(desafioType)
                            .child("status");

                    challengeRef.setValue("completed").addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "¡Desafío completado!", Toast.LENGTH_LONG).show();
                    });
                } else {
                    // Show progress
                    Toast.makeText(this,
                            String.format("Progreso: %d/%d completadas", completed, total),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}