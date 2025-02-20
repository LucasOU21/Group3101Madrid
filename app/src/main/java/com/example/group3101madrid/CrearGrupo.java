package com.example.group3101madrid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group3101madrid.databinding.ActivityRegistroBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearGrupo extends AppCompatActivity {

    private static final String TAG = "RegistroDebug";

    TextInputEditText etName, etDescripcion, etPassword, etPasswordAgain;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    ActivityRegistroBinding binding;
    FirebaseDatabase firebase;
    DatabaseReference dbRef;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix: Remove the first setContentView call and only use binding
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            // Initialize views with proper binding
            etName = binding.etName;
            etDescripcion = binding.etDescripcion;
            etPassword = binding.etPassword;
            etPasswordAgain = binding.etPasswordAgain;
            progressBar = binding.progressBar;

            Log.d(TAG, "onCreate: Views initialized successfully");

            // Initialize Firebase instances
            mAuth = FirebaseAuth.getInstance();
            firebase = FirebaseDatabase.getInstance();
            dbRef = firebase.getReference("users");
            Log.d(TAG, "onCreate: Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error during initialization", e);
        }

        binding.btnRegistro.setOnClickListener(view -> {
            Log.d(TAG, "onRegisterClick: Register button clicked");

            String name = etName.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordAgain = etPasswordAgain.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(CrearGrupo.this, "Introduce un nombre de usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (descripcion.isEmpty()) {
                Toast.makeText(CrearGrupo.this, "Introduce un email válido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                Toast.makeText(CrearGrupo.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(passwordAgain)) {
                Toast.makeText(CrearGrupo.this, "¡Las contraseñas no coinciden!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress bar - Now it won't be null
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(name, password)
                    .addOnCompleteListener(CrearGrupo.this, task -> {
                        if (task.isSuccessful()) {
                            // Get the newly created user's ID
                            String groupId = mAuth.getCurrentUser().getUid();

                            // Create User object
                            User user = new User(name, descripcion);

                            // Save user data to Realtime Database
                            dbRef.child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        if (dbTask.isSuccessful()) {
                                            Log.d(TAG, "Group data saved successfully");
                                            Toast.makeText(CrearGrupo.this, "Grupo creado con éxito", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(getApplicationContext(), Login.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Log.w(TAG, "Failed to save group data", dbTask.getException());
                                            Toast.makeText(CrearGrupo.this, "Error al guardar datos del grupo", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(CrearGrupo.this, "Error al crear el grupo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}