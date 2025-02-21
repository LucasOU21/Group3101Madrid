package com.example.group3101madrid.Grupos;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.group3101madrid.databinding.ActivityCrearGrupoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearGrupo extends AppCompatActivity {
    private ActivityCrearGrupoBinding binding;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Add back button functionality
        binding.ivBackButton.setOnClickListener(view -> finish());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("groups");

        // Initialize progress bar
        progressBar = binding.progressBar;

        binding.btnRegistro.setOnClickListener(view -> createGroup());
    }

    private void createGroup() {
        String name = binding.etName.getText().toString().trim();
        String description = binding.etDescripcion.getText().toString().trim();
        String joinCode = binding.etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Introduce un nombre de grupo");
            return;
        }
        if (description.isEmpty()) {
            binding.etDescripcion.setError("Introduce una descripción");
            return;
        }
        if (joinCode.isEmpty() || joinCode.length() < 6) {
            binding.etPassword.setError("El código debe tener al menos 6 caracteres");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String leaderId = mAuth.getCurrentUser().getUid();
        Group group = new Group(name, description, leaderId, joinCode);

        // Generate unique group ID
        String groupId = dbRef.push().getKey();
        group.setGroupId(groupId);

        // Save group to Firebase
        dbRef.child(groupId).setValue(group)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(CrearGrupo.this, "Grupo creado con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CrearGrupo.this, "Error al crear el grupo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}