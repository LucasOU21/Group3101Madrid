package com.example.group3101madrid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

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

public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private laMapaFragment mapFragment;
    private String desafioType;
    private boolean showMarker;

    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LatLng destinationPoint;
    private boolean locationUpdatesStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get intent extras
        desafioType = getIntent().getStringExtra("DESAFIO_TYPE");
        showMarker = getIntent().getBooleanExtra("SHOW_MARKER", false);

        if (desafioType == null) {
            Toast.makeText(this, "Error: No se especificó el tipo de desafío", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize location components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();

        // Create and add the map fragment
        mapFragment = new laMapaFragment();

        // Pass arguments to the fragment
        Bundle args = new Bundle();
        args.putString("DESAFIO_TYPE", desafioType);
        args.putBoolean("SHOW_MARKER", showMarker);
        mapFragment.setArguments(args);

        // Add the fragment to the container
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map_container, mapFragment);
        transaction.commit();

        // Load destination point from Firebase
        loadDestinationPoint();
    }

    private void loadDestinationPoint() {
        // Get reference to the desafio in Firebase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios").child(desafioType).child("locations").child("0");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        destinationPoint = new LatLng(latitude, longitude);

                        // Update the map fragment with the destination
                        if (mapFragment != null && mapFragment.isAdded()) {
                            mapFragment.setDestinationMarker(destinationPoint);
                        }

                        // Start location updates if we have permissions
                        if (ContextCompat.checkSelfPermission(
                                MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            startLocationUpdates();
                        } else {
                            requestLocationPermission();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this,
                        "Error cargando ubicación: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
                    mapFragment.updateUserLocation(currentLocation);

                    // Check if user is near the destination
                    if (destinationPoint != null) {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                currentLocation.latitude, currentLocation.longitude,
                                destinationPoint.latitude, destinationPoint.longitude,
                                results);

                        // If user is within 50 meters of the destination
                        if (results[0] < 50) {
                            // User has reached the destination
                            onDestinationReached();
                        }
                    }
                }
            }
        };
    }

    private void onDestinationReached() {
        // Stop location updates
        stopLocationUpdates();

        // Show toast to the user
        Toast.makeText(this, "¡Has llegado al destino!", Toast.LENGTH_LONG).show();

        // Update progress in Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                    .child("user_progress")
                    .child(currentUser.getUid())
                    .child(desafioType)
                    .child("destinations")
                    .child("0");

            progressRef.setValue(true);
        }

        // Here you could:
        // 1. Mark the location as visited in the database
        // 2. Unlock the content for the location
        // 3. Show a dialog with information about the place
        // 4. Navigate to a detail screen for the location
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
        fusedLocationClient.removeLocationUpdates(locationCallback);
        locationUpdatesStarted = false;
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
        if (destinationPoint != null) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}