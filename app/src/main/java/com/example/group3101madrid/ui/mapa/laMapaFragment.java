package com.example.group3101madrid.ui.mapa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.group3101madrid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class laMapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String desafioType;
    private boolean showMarker;
    private Marker userMarker;
    private Marker destinationMarker;
    private TextView tvDistance;

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

        // Default location (Madrid)
        LatLng madrid = new LatLng(40.416775, -3.703790);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15));
    }

    /**
     * Updates the user's location on the map
     * @param location The user's current location
     */
    public void updateUserLocation(LatLng location) {
        if (mMap == null) return;

        // Move camera to follow the user
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));

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

        // Update distance to destination
        updateDistanceToDestination(location);
    }

    /**
     * Sets the destination marker on the map
     * @param location The destination location
     */
    public void setDestinationMarker(LatLng location) {
        if (mMap == null) return;

        // Clear existing marker if it exists
        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        // Add the destination marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        destinationMarker = mMap.addMarker(markerOptions);

        // Initially zoom to show the destination
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    /**
     * Updates the distance display between user and destination
     */
    private void updateDistanceToDestination(LatLng userLocation) {
        if (destinationMarker != null && tvDistance != null) {
            // Calculate distance in meters
            LatLng destLocation = destinationMarker.getPosition();
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    destLocation.latitude, destLocation.longitude,
                    results);

            int distance = Math.round(results[0]);

            // Update the TextView
            tvDistance.setText("Distancia: " + distance + " m");
        }
    }
}