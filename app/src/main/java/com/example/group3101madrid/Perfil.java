package com.example.group3101madrid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    Button btnSignOut;
    Button btnGrupos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        btnSignOut = findViewById(R.id.btnSignOut);
        btnGrupos = findViewById(R.id.btnGrupos);

        btnSignOut.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Login.class);
            startActivity(intent);
        });

        btnGrupos.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Grupos.class);
            startActivity(intent);
        });


    }
}