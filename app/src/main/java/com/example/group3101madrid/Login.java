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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group3101madrid.ui.Home.HomeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginDebug";
    private static final int GOOGLE_SIGN_IN_CODE = 100; // Request code for Google Sign-In

    TextInputEditText etEmailOrUsername, etPassword;
    Button btnIniciarSesion, btnGoogleSignIn;
    FirebaseAuth mAuth;
    TextView tvRegisterAccount;
    FirebaseDatabase firebase;
    DatabaseReference dbRef;
    ProgressBar progressBar;
    GoogleSignInClient googleSignInClient;


    //planificacion del recordar usuario
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
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);

        // Add this line to initialize the Forgot Password TextView
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firebase = FirebaseDatabase.getInstance();
        dbRef = firebase.getReference("users");

        // Add the click listener for the Forgot Password TextView
        tvForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, CambiarPasswordActivity.class);
            startActivity(intent);
        });

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Uses value from strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Google Sign-In Button Click
        btnGoogleSignIn.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, GOOGLE_SIGN_IN_CODE);
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null) {
                    firebaseAuthWithGoogle(googleSignInAccount);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Error al iniciar sesión en Google: " + e.getStatusCode());
                Toast.makeText(Login.this, "Error al iniciar sesión en Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Login.this, "Inicio de sesión en Google con éxito", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, Main.class));
                finish();
            } else {
                Toast.makeText(Login.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
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
                        Intent i = new Intent(getApplicationContext(), Main.class);
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