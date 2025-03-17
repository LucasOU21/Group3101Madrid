package com.example.group3101madrid.Grupos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group3101madrid.Amigos.Amigos;
import com.example.group3101madrid.R;

public class Grupos extends AppCompatActivity {

    Button btnAmigos;
    Button btnCrearGrupo;
    Button btnUnirseGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos);
        findViewById(R.id.ivBackButton).setOnClickListener(view -> finish());

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