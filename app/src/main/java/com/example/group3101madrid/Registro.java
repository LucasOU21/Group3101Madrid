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
import androidx.appcompat.app.AppCompatActivity;
import com.example.group3101madrid.databinding.ActivityRegistroBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registro extends AppCompatActivity {
    private static final String TAG = "RegistroDebug";

    TextInputEditText etUsername, etEmail, etPassword, etPasswordAgain;
    ProgressBar progressBar;
    CheckBox cbTerms;
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
            etUsername = binding.etUsername;
            etEmail = binding.etEmail;
            etPassword = binding.etPassword;
            etPasswordAgain = binding.etPasswordAgain;
            cbTerms = binding.cbTerms;
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

            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordAgain = etPasswordAgain.getText().toString().trim();

            // Validate inputs
            if (username.isEmpty()) {
                Toast.makeText(Registro.this, "Introduce un nombre de usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

            // Show progress bar - Now it won't be null
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }/

            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Registro.this, task -> {
                        if (task.isSuccessful()) {
                            // Get the newly created user's ID
                            String userId = mAuth.getCurrentUser().getUid();

                            // Create User object
                            User user = new User(username, email);

                            // Save user data to Realtime Database
                            dbRef.child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        if (dbTask.isSuccessful()) {
                                            Log.d(TAG, "User data saved successfully");
                                            Toast.makeText(Registro.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(getApplicationContext(), Login.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Log.w(TAG, "Failed to save user data", dbTask.getException());
                                            Toast.makeText(Registro.this, "Error al guardar datos de usuario", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Registro.this, "Error al crear la cuenta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}