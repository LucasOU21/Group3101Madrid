package com.example.group3101madrid;

import com.google.android.gms.maps.model.LatLng;

public class Experiencia {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private LatLng location;
    private int puntos; // New field for experience points (1-5)

    public Experiencia(String id, String title, String description, String imageUrl, LatLng location, int puntos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.puntos = puntos;
    }

    // Default constructor
    public Experiencia() {
        // Default to 1 point if not specified
        this.puntos = 1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        // Ensure points are within the valid range (1-5)
        if (puntos < 1) {
            this.puntos = 1;
        } else if (puntos > 5) {
            this.puntos = 5;
        } else {
            this.puntos = puntos;
        }
    }
}