package com.example.group3101madrid.ui.mapa;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.group3101madrid.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class laMapaFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap nMap;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_la_mapa, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fgMap);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fgMap, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d("MapDebug", "Map is ready");
        nMap = googleMap;


        // Enable UI settings
        nMap.getUiSettings().setZoomControlsEnabled(true);
        nMap.getUiSettings().setCompassEnabled(true);
        nMap.getUiSettings().setMapToolbarEnabled(true);

        // Set map type (you can change to MAP_TYPE_SATELLITE if preferred)
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Set click listeners
        nMap.setOnMapClickListener(this);
        nMap.setOnMapLongClickListener(this);

        // Add permanent markers
        addPermanentMarkers();

        // Set center position and zoom
        LatLng basilicaSanMarino = new LatLng(43.9362205, 12.4469272);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(basilicaSanMarino, 12f);
        nMap.moveCamera(cameraUpdate);

        // Set info window click listener
        nMap.setOnInfoWindowClickListener(marker -> {
            String uri = "google.navigation:q="
                    + marker.getPosition().latitude
                    + ","
                    + marker.getPosition().longitude
                    + "&mode=w";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            i.setPackage("com.google.android.apps.maps");

            if (getActivity() != null) {
                startActivity(i);
            }
        });
    }

    private void addPermanentMarkers() {
        LatLng sanMarinoStadium = new LatLng(43.9707725, 12.4760217);
        LatLng basilicaSanMarino = new LatLng(43.9362205, 12.4469272);
        LatLng acquavivaStadium = new LatLng(43.947049, 12.4072099);
        LatLng fiorentinoStadium = new LatLng(43.908696, 12.4477807);

        nMap.addMarker(new MarkerOptions()
                .position(basilicaSanMarino)
                .title("Basilica San Marino")
                .snippet("Click for directions")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        nMap.addMarker(new MarkerOptions()
                .position(sanMarinoStadium)
                .title("Estadio Nacional de San Marino")
                .snippet("Click for directions")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        nMap.addMarker(new MarkerOptions()
                .position(acquavivaStadium)
                .title("Estadio de Acquaviva")
                .snippet("Click for directions")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        nMap.addMarker(new MarkerOptions()
                .position(fiorentinoStadium)
                .title("Estadio de Fiorentino")
                .snippet("Click for directions")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Clear previous temporary markers
        nMap.clear();

        // Re-add permanent markers
        addPermanentMarkers();

        // Add clicked location marker
        nMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected Location")
                .snippet("Lat: " + latLng.latitude + " Long: " + latLng.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true));
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        String uri = "google.navigation:q=" + latLng.latitude + "," + latLng.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (getActivity() != null) {
            startActivity(intent);
        }
    }
}