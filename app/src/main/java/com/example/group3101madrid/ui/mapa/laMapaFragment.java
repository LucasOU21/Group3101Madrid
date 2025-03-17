package com.example.group3101madrid.ui.mapa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.group3101madrid.Experiencia;
import com.example.group3101madrid.MapActivity;
import com.example.group3101madrid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class laMapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String desafioType;
    private boolean showMarker;
    private Marker userMarker;
    private TextView tvDistance;

    // Map of markers for experiencias
    private Map<String, Marker> experienciaMarkers = new HashMap<>();
    private Map<String, Experiencia> experienciaMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_la_mapa, container, false);

        // Get arguments if passed
        Bundle args = getArguments();
        if (args != null) {
            desafioType = args.getString("DESAFIO_TYPE");
            showMarker = args.getBoolean("SHOW_MARKER", false);
        }

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fgMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Try to find the distance TextView from parent activity if exists
        View parentView = getActivity().findViewById(android.R.id.content);
        tvDistance = parentView.findViewById(R.id.tvDistance);

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable my location button (requires permission checks in containing activity)
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // Permission handling should be done in the containing activity
            e.printStackTrace();
        }

        // Get arguments for specific coordinates
        Bundle args = getArguments();
        if (args != null) {
            double specificLatitude = args.getDouble("LATITUDE", 0);
            double specificLongitude = args.getDouble("LONGITUDE", 0);
            String specificExperienciaId = args.getString("EXPERIENCIA_ID", "");

            // If specific coordinates were provided, center the map on them
            if (specificLatitude != 0 && specificLongitude != 0) {
                LatLng specificLocation = new LatLng(specificLatitude, specificLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(specificLocation, 15));

                // You might want to add a special marker for this location
                mMap.addMarker(new MarkerOptions()
                        .position(specificLocation)
                        .title("Experiencia")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            } else {
                // Default location (Madrid)
                LatLng madrid = new LatLng(40.416775, -3.703790);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15));
            }
        } else {
            // Default location (Madrid)
            LatLng madrid = new LatLng(40.416775, -3.703790);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15));
        }

        // Set up marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Experiencia) {
                // Show experiencia detail dialog
                if (getActivity() instanceof MapActivity) {
                    ((MapActivity) getActivity()).showExperienciaDetail((Experiencia) tag);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Updates the user's location on the map
     * @param location The user's current location
     */
    public void updateUserLocation(LatLng location) {
        if (mMap == null) return;

        // Update or add the user marker
        if (userMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            userMarker = mMap.addMarker(markerOptions);
        } else {
            userMarker.setPosition(location);
        }

        // We only move the camera if we're not using experiencia markers
        if (experienciaMarkers.isEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        }

        // Update distance display for the closest experiencia
        updateDistanceToClosestExperiencia(location);
    }

    /**
     * Adds a marker for an experiencia on the map
     * @param location The location for the marker
     * @param title The title for the marker
     * @param experiencia The experiencia data to attach to the marker
     */
    public void addMarker(LatLng location, String title, Experiencia experiencia) {
        if (mMap == null || experiencia == null) return;

        // Create the marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        Marker marker = mMap.addMarker(markerOptions);

        // Store the experiencia with the marker
        marker.setTag(experiencia);

        // Add to our maps
        experienciaMarkers.put(experiencia.getId(), marker);
        experienciaMap.put(experiencia.getId(), experiencia);
    }

    /**
     * Updates a marker to show it's been completed
     * @param experienciaId The ID of the completed experiencia
     */
    public void updateMarkerCompleted(String experienciaId) {
        Marker marker = experienciaMarkers.get(experienciaId);
        if (marker != null) {
            // Change marker color to green to indicate completion
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
    }

    /**
     * Clears all markers from the map
     */
    public void clearMarkers() {
        if (mMap != null) {
            mMap.clear();
            experienciaMarkers.clear();
            experienciaMap.clear();
            userMarker = null;
        }
    }

    /**
     * Centers the map on the specified location
     * @param location The location to center on
     */
    public void centerMapOn(LatLng location) {
        if (mMap != null && location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    /**
     * Updates the distance display to show the closest experiencia
     */
    private void updateDistanceToClosestExperiencia(LatLng userLocation) {
        if (mMap == null || tvDistance == null || userLocation == null || experienciaMarkers.isEmpty()) {
            return;
        }

        // Find the closest experiencia
        float closestDistance = Float.MAX_VALUE;
        String closestTitle = "";

        for (Map.Entry<String, Marker> entry : experienciaMarkers.entrySet()) {
            Marker marker = entry.getValue();
            LatLng markerPos = marker.getPosition();

            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    markerPos.latitude, markerPos.longitude,
                    results);

            if (results[0] < closestDistance) {
                closestDistance = results[0];
                closestTitle = marker.getTitle();
            }
        }

        // Update the distance text
        if (closestDistance < Float.MAX_VALUE) {
            int distance = Math.round(closestDistance);
            tvDistance.setText(closestTitle + ": " + distance + " m");
        }
    }
}