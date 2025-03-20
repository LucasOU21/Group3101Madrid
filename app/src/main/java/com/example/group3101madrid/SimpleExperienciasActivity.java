package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.Perfil.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleExperienciasActivity extends AppCompatActivity {

    private static final String TAG = "SimpleExperiencias";

    private RecyclerView recyclerView;
    private SimpleExperienciaAdapter adapter;
    private List<SimpleExperiencia> experienciasList;
    private Map<String, Boolean> completedExperiencias;

    private String desafioType;
    private String desafioTitle;
    private TextView tvTitle;
    private TextView tvProgress;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_experiencias);

        // Get data from intent
        desafioType = getIntent().getStringExtra("DESAFIO_TYPE");
        desafioTitle = getIntent().getStringExtra("DESAFIO_TITLE");

        if (desafioType == null) {
            Toast.makeText(this, "Error: No se especificó el tipo de desafío", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        tvTitle = findViewById(R.id.tvTitle);
        tvProgress = findViewById(R.id.tvProgress);
        ivBack = findViewById(R.id.ivBack);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize navigation buttons
        ImageButton btnHomeNav = findViewById(R.id.btnHomeNav);
        ImageButton btnProfileNav = findViewById(R.id.btnProfileNav);

        // Set navigation buttons click listeners
        btnHomeNav.setOnClickListener(v -> {
            Intent intent = new Intent(SimpleExperienciasActivity.this, Main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnProfileNav.setOnClickListener(v -> {
            Intent intent = new Intent(SimpleExperienciasActivity.this, Perfil.class);
            startActivity(intent);
        });

        // Set title
        if (desafioTitle != null && !desafioTitle.isEmpty()) {
            tvTitle.setText(desafioTitle);
        } else {
            tvTitle.setText(desafioType);
        }

        // Set back button click listener
        ivBack.setOnClickListener(v -> finish());

        // Initialize lists
        experienciasList = new ArrayList<>();
        completedExperiencias = new HashMap<>();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleExperienciaAdapter(experienciasList, experiencia -> {
            openExperienciaDetail(experiencia);
        });
        recyclerView.setAdapter(adapter);

        // Load data
        loadCompletedExperiencias();
    }

    private void loadCompletedExperiencias() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid())
                .child(desafioType)
                .child("experiencias");

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                completedExperiencias.clear();

                // Load completed experiencias
                for (DataSnapshot expSnapshot : dataSnapshot.getChildren()) {
                    String expId = expSnapshot.getKey();
                    Boolean completed = expSnapshot.getValue(Boolean.class);

                    if (expId != null && Boolean.TRUE.equals(completed)) {
                        completedExperiencias.put(expId, true);
                    }
                }

                // Now load all experiencias
                loadExperiencias();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading completed experiencias: " + databaseError.getMessage());
                Toast.makeText(SimpleExperienciasActivity.this,
                        "Error cargando progreso", Toast.LENGTH_SHORT).show();

                // Still try to load experiencias
                loadExperiencias();
            }
        });
    }

    private void loadExperiencias() {
        DatabaseReference desafioRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios")
                .child(desafioType)
                .child("experiencias");

        desafioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                experienciasList.clear();

                // Load all experiencias
                for (DataSnapshot expSnapshot : dataSnapshot.getChildren()) {
                    String id = expSnapshot.getKey();
                    String title = expSnapshot.child("title").getValue(String.class);
                    String description = expSnapshot.child("description").getValue(String.class);
                    String imageUrl = expSnapshot.child("imageUrl").getValue(String.class);
                    Double latitude = expSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = expSnapshot.child("longitude").getValue(Double.class);

                    // Get the puntos value, default to 1 if not found
                    Integer puntos = expSnapshot.child("puntos").getValue(Integer.class);
                    if (puntos == null) {
                        puntos = 1;
                    }

                    // Ensure puntos is within valid range (1-5)
                    puntos = Math.max(1, Math.min(5, puntos));

                    if (id != null && title != null) {
                        boolean completed = completedExperiencias.containsKey(id);

                        SimpleExperiencia experiencia = new SimpleExperiencia(
                                id, title, description, imageUrl,
                                latitude, longitude, completed, puntos);

                        experienciasList.add(experiencia);
                    }
                }

                // Update progress text
                int total = experienciasList.size();
                int completed = completedExperiencias.size();
                tvProgress.setText(completed + " / " + total + " completadas");

                // Update adapter
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading experiencias: " + databaseError.getMessage());
                Toast.makeText(SimpleExperienciasActivity.this,
                        "Error cargando experiencias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openExperienciaDetail(SimpleExperiencia experiencia) {
        Intent intent = new Intent(this, SimpleExperienciaDetailActivity.class);
        intent.putExtra("EXPERIENCIA_ID", experiencia.getId());
        intent.putExtra("DESAFIO_TYPE", desafioType);
        intent.putExtra("TITLE", experiencia.getTitle());
        intent.putExtra("DESCRIPTION", experiencia.getDescription());
        intent.putExtra("IMAGE_URL", experiencia.getImageUrl());
        intent.putExtra("LATITUDE", experiencia.getLatitude());
        intent.putExtra("LONGITUDE", experiencia.getLongitude());
        intent.putExtra("COMPLETED", experiencia.isCompleted());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from detail screen
        loadCompletedExperiencias();
    }

    // Simple experiencia model class
    public static class SimpleExperiencia {
        private String id;
        private String title;
        private String description;
        private String imageUrl;
        private Double latitude;
        private Double longitude;
        private boolean completed;
        private int puntos; // New field for experience points

        public SimpleExperiencia(String id, String title, String description,
                                 String imageUrl, Double latitude, Double longitude,
                                 boolean completed, int puntos) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.latitude = latitude;
            this.longitude = longitude;
            this.completed = completed;
            this.puntos = puntos;
        }

        // Getters and setters
        public String getId() {
            return id;
        }
        public String getTitle() {
            return title;
        }
        public String getDescription() {
            return description;
        }
        public String getImageUrl() {
            return imageUrl;
        }
        public Double getLatitude() {
            return latitude;
        }
        public Double getLongitude() {
            return longitude;
        }
        public boolean isCompleted() {
            return completed;
        }
        public int getPuntos() {
            return puntos;
        }
    }
}