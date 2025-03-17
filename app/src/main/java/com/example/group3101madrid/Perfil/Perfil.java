package com.example.group3101madrid.Perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.Grupos.Grupos;
import com.example.group3101madrid.Login;
import com.example.group3101madrid.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Perfil extends AppCompatActivity {

    private Button btnSignOut;
    private Button btnGrupos;
    private Button btnAyudaSoporte;
    private TextView tvUsername;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView ivProfilePic;
    private StorageReference storageRef;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        findViewById(R.id.ivBackButton).setOnClickListener(view -> {
            finish(); // This will close the current activity and return to the previous screen
        });

        // Active challenges click listener
        findViewById(R.id.llActiveDesafios).setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, DesafiosActivo.class);
            startActivity(intent);
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        btnSignOut = findViewById(R.id.btnSignOut);
        btnGrupos = findViewById(R.id.btnGrupos);
        btnAyudaSoporte = findViewById(R.id.btnAyudaSoporte);
        tvUsername = findViewById(R.id.textView5);

        // Set user display name
        updateUserInterface();

        btnSignOut.setOnClickListener(view -> handleSignOut());

        btnGrupos.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, Grupos.class);
            startActivity(intent);
        });

        btnAyudaSoporte.setOnClickListener(view -> {
            Intent intent = new Intent(Perfil.this, SupportActivity.class);
            startActivity(intent);
        });

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize profile image view
        ivProfilePic = findViewById(R.id.ivProfilePic);

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleImageResult
        );

        // Set click listener for profile image
        ivProfilePic.setOnClickListener(view -> {
            imagePickerLauncher.launch("image/*");
        });

        // Load existing profile picture
        loadProfilePicture();
    }

    private void enviarCorreoSoporte() {
        // Get user information to personalize support email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "No disponible";
        String userName = currentUser != null ? (currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Usuario") : "Usuario";

        // Support email address
        String email = "101MadridAdmin@Support.com";

        // Create a more detailed subject line
        String subject = "Soporte 101 Madrid - Consulta de Usuario";

        // Create a detailed template message with user info and common issues
        String body = "Hola Equipo de Soporte de 101 Madrid,\n\n" +
                "Soy " + userName + " y necesito ayuda con la aplicación.\n\n" +
                "Detalles de mi cuenta:\n" +
                "- Correo: " + userEmail + "\n\n" +
                "Descripción del problema:\n" +
                "[Por favor, describe aquí tu problema con la mayor cantidad de detalles posible]\n\n" +
                "Pasos para reproducir:\n" +
                "1. \n2. \n3. \n\n" +
                "Dispositivo: " + android.os.Build.MODEL + "\n" +
                "Versión Android: " + android.os.Build.VERSION.RELEASE + "\n\n" +
                "Gracias por tu ayuda,\n" +
                userName;

        // Create the email intent
        Uri uri = Uri.parse("mailto:" + email +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body));

        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo de soporte"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Perfil.this,
                    "No tienes ninguna app de correo instalada",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserInterface() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's display name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUsername.setText(displayName);
            } else {
                // If no display name, use email
                String email = currentUser.getEmail();
                if (email != null) {
                    // Remove the @domain.com part
                    tvUsername.setText(email.substring(0, email.indexOf('@')));
                }
            }
        }
    }

    private void loadUserProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference progressRef = FirebaseDatabase.getInstance().getReference()
                .child("user_progress")
                .child(currentUser.getUid());

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous progress views
                LinearLayout progressContainer = findViewById(R.id.progress_container);
                progressContainer.removeAllViews();

                for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                    String type = progressSnapshot.child("type").getValue(String.class);
                    String status = progressSnapshot.child("status").getValue(String.class);

                    if (status != null && status.equals("active")) {
                        // Create and add progress view
                        addProgressView(progressContainer, type);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Perfil.this,
                        "Error loading progress: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProgressView(LinearLayout container, String type) {
        View progressView = getLayoutInflater().inflate(R.layout.item_progress, container, false);

        ImageView iconView = progressView.findViewById(R.id.progress_icon);
        TextView titleView = progressView.findViewById(R.id.progress_title);

        // Set appropriate icon and title based on type
        switch (type) {
            case "croquetas":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Croquetas - En progreso");
                break;
            case "bares":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Bares - En progreso");
                break;
            case "paisajes":
                iconView.setImageResource(R.drawable.star);
                titleView.setText("101 Paisajes - En progreso");
                break;
        }

        container.addView(progressView);
    }

    private void handleSignOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Check if user signed in with Google
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                // Sign out from Google
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    mAuth.signOut();
                    returnToLogin();
                });
            } else {
                // Regular Firebase sign out
                mAuth.signOut();
                returnToLogin();
            }
        }
    }

    private void handleImageResult(Uri imageUri) {
        if (imageUri != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            StorageReference imageRef = storageRef
                    .child("profile_pictures")
                    .child(currentUser.getUid() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    saveProfilePictureUrl(downloadUri.toString());
                                    loadImageWithGlide(downloadUri.toString());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(Perfil.this,
                                                "Error al obtener la URL de la imagen",
                                                Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Perfil.this,
                                    "Error al subir la imagen",
                                    Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void saveProfilePictureUrl(String url) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("profile_picture");

        userRef.setValue(url)
                .addOnFailureListener(e ->
                        Toast.makeText(Perfil.this,
                                "Error al guardar la URL de la imagen",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void loadProfilePicture() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("profile_picture");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null) {
                    loadImageWithGlide(imageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Perfil.this,
                        "Error al cargar la imagen de perfil",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageWithGlide(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.foto_perfil_ejemplo)
                .error(R.drawable.foto_perfil_ejemplo)
                .into(ivProfilePic);
    }

    private void returnToLogin() {
        Intent intent = new Intent(Perfil.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}