package com.example.group3101madrid.ui.mapa;

import static androidx.core.content.ContextCompat.startActivity;

import android.os.Bundle;

import android.content.Intent;
import android.net.Uri;
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
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_la_mapa, container, false);

        // Initialize map fragment
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
        nMap = googleMap;

        nMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        LatLng sanMarinoStadium = new LatLng(43.9707725,12.4760217);
        LatLng basilicaSanMarino = new LatLng(43.9362205,12.4469272);
        LatLng acquavivaStadium = new LatLng(43.947049,12.4072099);
        LatLng fiorentinoStadium = new LatLng(43.908696,12.4477807);

        // Add markers
        nMap.addMarker(new MarkerOptions()
                        .position(basilicaSanMarino)
                        .title("Basilica San Marino")
                        .icon(BitmapDescriptorFactory.defaultMarker()))
                .showInfoWindow();

        nMap.addMarker(new MarkerOptions()
                        .position(sanMarinoStadium)
                        .title("Estadio Nacional de San Marino")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                .showInfoWindow();

        nMap.addMarker(new MarkerOptions()
                        .position(acquavivaStadium)
                        .title("Estadio de Acquaviva")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
                .showInfoWindow();

        nMap.addMarker(new MarkerOptions()
                        .position(fiorentinoStadium)
                        .title("Estadio de Fiorentino")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                .showInfoWindow();

        // Move camera
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(basilicaSanMarino, 14f);
        nMap.moveCamera(cameraUpdate);

        nMap.setOnInfoWindowClickListener(marker -> {
            String uri = "google.navigation:q="
                    + marker.getPosition().latitude
                    + ","
                    + marker.getPosition().longitude
                    + "&mode=w";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            i.setPackage("com.google.android.apps.maps");
            startActivity(i);
        });
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Implement if needed
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        // Implement if needed
    }
}