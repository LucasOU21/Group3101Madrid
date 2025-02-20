package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Grupos extends AppCompatActivity {

    Button btnAmigos;
    Button btnCrearGrupo;
    Button btnUnirseGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos);

        btnAmigos = findViewById(R.id.btnAmigos);
        btnCrearGrupo = findViewById(R.id.btnCrearGrupo);
        btnUnirseGrupo = findViewById(R.id.btnUnirseGrupo);

        btnAmigos.setOnClickListener(view -> {
            Intent intent = new Intent(Grupos.this, Amigos.class);
            startActivity(intent);
        });

        btnCrearGrupo.setOnClickListener(view -> {
            Intent intent = new Intent(Grupos.this, CrearGrupo.class);
            startActivity(intent);
        });

        btnUnirseGrupo.setOnClickListener(view -> {
            Intent intent = new Intent(Grupos.this, UnirseGrupo.class);
            startActivity(intent);
        });


    }
}