package com.example.group3101madrid;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class Registro extends AppCompatActivity {

    // Inicializar variables
    TextInputEditText etUsername, etEmail, etPassword, etPasswordAgain;
    Button btnRegistro;
    ProgressBar progressBar;
    CheckBox cbTerms;
    FirebaseAuth mAuth;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Asignar los componentes a la vista
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        btnRegistro = findViewById(R.id.btnRegistro);
        cbTerms = findViewById(R.id.cbTerms);

        // Obtener una instancia de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus && etUsername.getText().toString().equals("Username")) {
                    etUsername.setText("");
                }
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus && etEmail.getText().toString().equals("Email")) {
                    etUsername.setText("");
                }
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus && etPassword.getText().toString().equals("Pasword")) {
                    etUsername.setText("");
                }
            }
        });

        etPasswordAgain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus && etUsername.getText().toString().equals("PasswordAgain")) {
                    etUsername.setText("");
                }
            }
        });

        // Register button click listener
        btnRegistro.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordAgain = etPasswordAgain.getText().toString().trim();

            // Validate inputs
            if (username.isEmpty()) {
                Toast.makeText(Registro.this, "Introduce un nombre de usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Registro.this, "Introduce un email válido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                Toast.makeText(Registro.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(passwordAgain)) {
                Toast.makeText(Registro.this, "¡Las contraseñas no coinciden!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!cbTerms.isChecked()) {
                Toast.makeText(Registro.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Registro.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(Registro.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), Login.class);
                            startActivity(i);
                            finish(); // Close the current activity
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Registro.this, "Error al crear la cuenta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

}