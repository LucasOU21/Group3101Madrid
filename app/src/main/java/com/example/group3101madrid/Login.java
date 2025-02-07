package com.example.group3101madrid;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginDebug";

    TextInputEditText etEmailOrUsername, etPassword;
    Button btnIniciarSesion;
    FirebaseAuth mAuth;
    TextView tvRegisterAccount;
    FirebaseDatabase firebase;
    DatabaseReference dbRef;
    ProgressBar progressBar;

    /*
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i = new Intent(getApplicationContext(), Home.class);
            startActivity(i);
        }
    }

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize components
        etEmailOrUsername = findViewById(R.id.etEmail); // Keep the same ID for backwards compatibility
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvRegisterAccount = findViewById(R.id.tvRegisterAccount);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firebase = FirebaseDatabase.getInstance();
        dbRef = firebase.getReference("users");

        tvRegisterAccount.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), Registro.class);
            startActivity(i);
        });

        etEmailOrUsername.setOnFocusChangeListener((view, hadFocus) -> {
            if (hadFocus) {
                etEmailOrUsername.setText("");
            }
        });

        etPassword.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus){
                etPassword.setText("");
            }
        });

        btnIniciarSesion.setOnClickListener(view -> {
            String emailOrUsername = etEmailOrUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (emailOrUsername.isEmpty()) {
                Toast.makeText(Login.this, "Introduce un email o nombre de usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(Login.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Check if input is email or username
            if (Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
                // Input is email, login directly
                signInWithEmail(emailOrUsername, password);
            } else {
                // Input is username, need to find corresponding email
                findEmailByUsername(emailOrUsername, password);
            }
        });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(Login.this, "Login realizado con éxito", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Home.class);
                        startActivity(i);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(Login.this, "No se ha podido realizar el login, revisa los datos introducidos",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findEmailByUsername(String username, String password) {
        dbRef.orderByChild("username").equalTo(username).limitToFirst(1)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot.exists()) {
                            // Get the first (and should be only) user with this username
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null && user.getEmail() != null) {
                                    // Found the email, now sign in
                                    signInWithEmail(user.getEmail(), password);
                                    return;
                                }
                            }
                        }
                        // If we get here, username wasn't found
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "findEmailByUsername:failure", task.getException());
                    }
                });
    }
}