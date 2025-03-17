package com.example.group3101madrid;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.ui.mapa.laMapaFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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

public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MapActivity";

    private laMapaFragment mapFragment;
    private String currentDesafioType;
    private TextView tvMapTitle;
    private ImageButton btnPrevDesafio;
    private ImageButton btnNextDesafio;

    // Desafios and experiencias data
    private List<String> desafioTypesList;
    private int currentDesafioIndex = 0;
    private Map<String, String> desafioTitles;
    private Map<String, List<Experiencia>> experienciasMap;

    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean locationUpdatesStarted = false;

    // Dialog for experiencia details
    private Dialog experienciaDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize UI components
        tvMapTitle = findViewById(R.id.tvMapTitle);
        btnPrevDesafio = findViewById(R.id.btnPrevDesafio);
        btnNextDesafio = findViewById(R.id.btnNextDesafio);

        // Initialize back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Get intent extras
        currentDesafioType = getIntent().getStringExtra("DESAFIO_TYPE");
        boolean showMarker = getIntent().getBooleanExtra("SHOW_MARKER", false);

// Get specific coordinates if provided
        double specificLatitude = getIntent().getDoubleExtra("LATITUDE", 0);
        double specificLongitude = getIntent().getDoubleExtra("LONGITUDE", 0);
        String specificExperienciaId = getIntent().getStringExtra("EXPERIENCIA_ID");

        if (currentDesafioType == null) {
            Toast.makeText(this, "Error: No se especificó el tipo de desafío", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize data structures
        desafioTypesList = new ArrayList<>();
        desafioTitles = new HashMap<>();
        experienciasMap = new HashMap<>();

        // Prepare experiencia dialog
        prepareExperienciaDialog();

        // Set up navigation buttons
        setupNavButtons();

        // Initialize location components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();

        // Create and add the map fragment
        mapFragment = new laMapaFragment();

        // Pass arguments to the fragment
        Bundle args = new Bundle();
        args.putString("DESAFIO_TYPE", currentDesafioType);
        args.putBoolean("SHOW_MARKER", showMarker);
        // Pass the specific coordinates if provided
        if (specificLatitude != 0 && specificLongitude != 0) {
            args.putDouble("LATITUDE", specificLatitude);
            args.putDouble("LONGITUDE", specificLongitude);
            args.putString("EXPERIENCIA_ID", specificExperienciaId);
        }
        mapFragment.setArguments(args);

        // Add the fragment to the container
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map_container, mapFragment);
        transaction.commit();

        // Load desafios list
        loadDesafiosList();
    }

    private void setupNavButtons() {
        // Make sure buttons are visible
        btnPrevDesafio.setVisibility(View.VISIBLE);
        btnNextDesafio.setVisibility(View.VISIBLE);

        btnPrevDesafio.setOnClickListener(v -> navigateDesafio(-1));
        btnNextDesafio.setOnClickListener(v -> navigateDesafio(1));
    }

    private void navigateDesafio(int direction) {
        if (desafioTypesList.isEmpty()) {
            return;
        }

        // Calculate new index with wrapping
        int newIndex = (currentDesafioIndex + direction) % desafioTypesList.size();
        if (newIndex < 0) {
            newIndex = desafioTypesList.size() - 1;
        }

        currentDesafioIndex = newIndex;
        currentDesafioType = desafioTypesList.get(currentDesafioIndex);

        // Update title
        updateDesafioTitle();

        // Load experiencias for this desafio
        loadExperiencias();
    }

    private void updateDesafioTitle() {
        String title = desafioTitles.get(currentDesafioType);
        if (title != null) {
            tvMapTitle.setText(title);
        } else {
            tvMapTitle.setText(currentDesafioType);
        }
    }

    private void loadDesafiosList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get reference to user's active desafios
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid());

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                desafioTypesList.clear();

                // Only get active desafios
                for (DataSnapshot desafioSnapshot : dataSnapshot.getChildren()) {
                    String status = desafioSnapshot.child("status").getValue(String.class);
                    if ("active".equals(status)) {
                        String type = desafioSnapshot.child("type").getValue(String.class);
                        if (type != null) {
                            desafioTypesList.add(type);
                        }
                    }
                }

                // If no desafios found, notify user
                if (desafioTypesList.isEmpty()) {
                    Toast.makeText(MapActivity.this,
                            "No tienes desafíos activos. ¡Activa alguno primero!",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Now get the titles for these desafios
                loadDesafioTitles();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this,
                        "Error cargando desafíos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDesafioTitles() {
        DatabaseReference desafiosRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios");

        desafiosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (String desafioType : desafioTypesList) {
                    DataSnapshot desafioSnapshot = dataSnapshot.child(desafioType);
                    if (desafioSnapshot.exists()) {
                        String title = desafioSnapshot.child("title").getValue(String.class);
                        if (title != null) {
                            desafioTitles.put(desafioType, title);
                        } else {
                            // If title not found, use type as title
                            desafioTitles.put(desafioType, desafioType);
                        }
                    }
                }

                // Set initial index - make it match the requested desafio or first one
                if (!desafioTypesList.isEmpty()) {
                    currentDesafioIndex = desafioTypesList.indexOf(currentDesafioType);
                    if (currentDesafioIndex < 0) {
                        currentDesafioIndex = 0;
                        currentDesafioType = desafioTypesList.get(0);
                    }

                    // Update the UI
                    updateDesafioTitle();

                    // Load experiencias for the current desafio
                    loadExperiencias();

                    // Update button visibility based on desafio count
                    updateNavButtonsVisibility();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this,
                        "Error cargando títulos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNavButtonsVisibility() {
        if (desafioTypesList.size() <= 1) {
            // Hide navigation buttons if only one desafio
            btnPrevDesafio.setVisibility(View.INVISIBLE);
            btnNextDesafio.setVisibility(View.INVISIBLE);
        } else {
            btnPrevDesafio.setVisibility(View.VISIBLE);
            btnNextDesafio.setVisibility(View.VISIBLE);
        }
    }

    private void loadExperiencias() {
        Log.d(TAG, "Loading experiencias for: " + currentDesafioType);

        // Show loading indicator
        Toast.makeText(this, "Cargando experiencias...", Toast.LENGTH_SHORT).show();

        // Clear current markers on map
        if (mapFragment != null && mapFragment.isAdded()) {
            mapFragment.clearMarkers();
        }

        // If we already have loaded this desafio's experiencias, use cached data
        if (experienciasMap.containsKey(currentDesafioType)) {
            showExperienciasOnMap(experienciasMap.get(currentDesafioType));
            return;
        }

        // Otherwise load from Firebase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios").child(currentDesafioType).child("experiencias");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Experiencia> experienciasList = new ArrayList<>();

                for (DataSnapshot expSnapshot : dataSnapshot.getChildren()) {
                    // Extract experiencia data
                    String id = expSnapshot.getKey();
                    String title = expSnapshot.child("title").getValue(String.class);
                    String description = expSnapshot.child("description").getValue(String.class);
                    String imageUrl = expSnapshot.child("imageUrl").getValue(String.class);

                    // Get location
                    Double latitude = expSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = expSnapshot.child("longitude").getValue(Double.class);

                    if (id != null && title != null && latitude != null && longitude != null) {
                        Experiencia exp = new Experiencia(id, title, description, imageUrl,
                                new LatLng(latitude, longitude));
                        experienciasList.add(exp);
                    }
                }

                // Cache the data
                experienciasMap.put(currentDesafioType, experienciasList);

                // Show on map
                showExperienciasOnMap(experienciasList);

                // Check for completed experiencias and update markers
                checkCompletedExperiencias();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this,
                        "Error cargando experiencias: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkCompletedExperiencias() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || mapFragment == null || !experienciasMap.containsKey(currentDesafioType)) {
            return;
        }

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid())
                .child(currentDesafioType)
                .child("experiencias");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot expSnapshot : dataSnapshot.getChildren()) {
                    String expId = expSnapshot.getKey();
                    Boolean completed = expSnapshot.getValue(Boolean.class);

                    if (Boolean.TRUE.equals(completed) && expId != null) {
                        // Update marker to show completed
                        mapFragment.updateMarkerCompleted(expId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking completed experiencias: " + databaseError.getMessage());
            }
        });
    }

    private void showExperienciasOnMap(List<Experiencia> experiencias) {
        if (mapFragment != null && mapFragment.isAdded() && experiencias != null) {
            for (Experiencia exp : experiencias) {
                mapFragment.addMarker(exp.getLocation(), exp.getTitle(), exp);
            }

            // If we have at least one experiencia, center the map on it
            if (!experiencias.isEmpty()) {
                mapFragment.centerMapOn(experiencias.get(0).getLocation());
            }

            // Start location updates
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                requestLocationPermission();
            }
        }
    }

    public void showExperienciaDetail(Experiencia experiencia) {
        if (experiencia == null || experienciaDialog == null) {
            return;
        }

        // Set experiencia data in dialog
        TextView tvTitle = experienciaDialog.findViewById(R.id.tvExperienciaTitle);
        TextView tvDescription = experienciaDialog.findViewById(R.id.tvExperienciaDescription);
        ImageView ivImage = experienciaDialog.findViewById(R.id.ivExperienciaImage);

        tvTitle.setText(experiencia.getTitle());

        if (experiencia.getDescription() != null) {
            tvDescription.setText(experiencia.getDescription());
        } else {
            tvDescription.setText("No hay descripción disponible.");
        }

        // Load image with Glide
        if (experiencia.getImageUrl() != null) {
            try {
                // Try to load as resource first
                int resourceId = getResources().getIdentifier(
                        experiencia.getImageUrl(),
                        "drawable",
                        getPackageName()
                );

                if (resourceId != 0) {
                    Glide.with(this)
                            .load(resourceId)
                            .centerCrop()
                            .into(ivImage);
                } else {
                    // Try as URL
                    Glide.with(this)
                            .load(experiencia.getImageUrl())
                            .centerCrop()
                            .into(ivImage);
                }
            } catch (Exception e) {
                // If loading fails, use placeholder
                ivImage.setImageResource(R.drawable.img_101_logo);
            }
        } else {
            ivImage.setImageResource(R.drawable.img_101_logo);
        }

        // Set up buttons
        Button btnStart = experienciaDialog.findViewById(R.id.btnStartExperiencia);
        Button btnComplete = experienciaDialog.findViewById(R.id.btnCompleteExperiencia);

        // Check if already completed
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                    .child("user_progress")
                    .child(currentUser.getUid())
                    .child(currentDesafioType)
                    .child("experiencias")
                    .child(experiencia.getId());

            progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean completed = dataSnapshot.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(completed)) {
                        btnStart.setEnabled(false);
                        btnComplete.setText("Completado");
                        btnComplete.setEnabled(false);
                    } else {
                        btnStart.setEnabled(true);
                        btnComplete.setText("Completar");
                        btnComplete.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Default to enabled
                    btnStart.setEnabled(true);
                    btnComplete.setEnabled(true);
                }
            });
        }

        // Set button click listeners
        final String expId = experiencia.getId();

        btnStart.setOnClickListener(v -> {
            // Center the map on this experiencia
            mapFragment.centerMapOn(experiencia.getLocation());
            experienciaDialog.dismiss();
        });

        btnComplete.setOnClickListener(v -> {
            // Mark as completed in Firebase
            if (currentUser != null) {
                DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                        .child("user_progress")
                        .child(currentUser.getUid())
                        .child(currentDesafioType)
                        .child("experiencias")
                        .child(expId);

                progressRef.setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MapActivity.this,
                                    "Experiencia completada",
                                    Toast.LENGTH_SHORT).show();
                            experienciaDialog.dismiss();

                            // Update marker to show it's completed
                            mapFragment.updateMarkerCompleted(expId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MapActivity.this,
                                    "Error al marcar como completado: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Show the dialog
        experienciaDialog.show();
    }

    private void prepareExperienciaDialog() {
        experienciaDialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_experiencia_detail, null);
        experienciaDialog.setContentView(dialogView);
        experienciaDialog.setCancelable(true);

        // Set up close button
        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseExperiencia);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> experienciaDialog.dismiss());
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Update user's location on the map
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mapFragment != null && mapFragment.isAdded()) {
                        mapFragment.updateUserLocation(currentLocation);
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (locationUpdatesStarted) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        locationUpdatesStarted = true;
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationUpdatesStarted = false;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Permiso de ubicación denegado. No se puede seguir tu progreso.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}