package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;


public class Home extends AppCompatActivity {

    ImageButton imageBtnPerfil;
    ImageButton imgBtnDesafios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the ImageButton by ID
        imageBtnPerfil = findViewById(R.id.imageBtnPerfil);
        imgBtnDesafios = findViewById(R.id.imgBtnDesafios);

        // Set OnClickListener
        imageBtnPerfil.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Perfil.class);
            startActivity(intent);
        });


    }

    // Separate method for handling the click event
    public void onClickDesafios (View view) {
        Intent intent = new Intent(Home.this, Iniciodesafios.class);
        startActivity(intent);
    }
}