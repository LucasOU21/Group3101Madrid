package com.example.group3101madrid.Grupos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class UnirseGrupo extends AppCompatActivity {
    private RecyclerView rvGroups;
    private GroupAdapter adapter;
    private List<Group> groupList;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private EditText etJoinCode;
    private Button btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_grupo);
        findViewById(R.id.ivBackButton).setOnClickListener(view -> finish());

        // Rest of your existing code...
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("groups");

        // Initialize views
        rvGroups = findViewById(R.id.rvGroups);
        etJoinCode = findViewById(R.id.etJoinCode);
        btnJoin = findViewById(R.id.btnJoin);

        // Setup RecyclerView
        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, group -> {
            // This is where we handle clicks on group items
            showGroupOptions(group);
        });
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        // Load user's groups
        loadUserGroups();

        // Join group button click
        btnJoin.setOnClickListener(v -> joinGroup());
    }

    private void showGroupOptions(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Check if user is the group leader
        boolean isLeader = group.getLeaderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Create options array based on user role
        String[] options;
        if (isLeader) {
            options = new String[]{"Ver detalles", "Ver código de grupo", "Salir del grupo"};
        } else {
            options = new String[]{"Ver detalles", "Salir del grupo"};
        }

        builder.setTitle(group.getName())
                .setItems(options, (dialog, which) -> {
                    if (isLeader) {
                        switch (which) {
                            case 0: // Ver detalles
                                showGroupDetails(group);
                                break;
                            case 1: // Ver código de grupo
                                showGroupCode(group);
                                break;
                            case 2: // Salir del grupo
                                confirmLeaveGroup(group);
                                break;
                        }
                    } else {
                        switch (which) {
                            case 0: // Ver detalles
                                showGroupDetails(group);
                                break;
                            case 1: // Salir del grupo
                                confirmLeaveGroup(group);
                                break;
                        }
                    }
                })
                .show();
    }

    private void showGroupDetails(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(group.getName())
                .setMessage("Descripción: " + group.getDescription() + "\n\n" +
                        "Miembros: " + group.getMembers().size())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void showGroupCode(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Código del Grupo")
                .setMessage("El código para unirse al grupo es:\n\n" + group.getJoinCode())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void confirmLeaveGroup(Group group) {
        new AlertDialog.Builder(this)
                .setTitle("Salir del Grupo")
                .setMessage("¿Estás seguro que deseas salir del grupo \"" + group.getName() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> leaveGroup(group))
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveGroup(Group group) {
        if (group.getLeaderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            Toast.makeText(this, "El líder no puede abandonar el grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(group.getGroupId())
                .child("members")
                .child(userId);

        groupRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UnirseGrupo.this, "Has salido del grupo", Toast.LENGTH_SHORT).show();
                    // The RecyclerView will update automatically due to the Firebase listener
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UnirseGrupo.this, "Error al salir del grupo", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserGroups() {
        String userId = mAuth.getCurrentUser().getUid();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null && group.getMembers().containsKey(userId)) {
                        groupList.add(group);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UnirseGrupo.this, "Error al cargar grupos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup() {
        String joinCode = etJoinCode.getText().toString().trim();
        if (joinCode.isEmpty()) {
            Toast.makeText(this, "Ingrese el código del grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        dbRef.orderByChild("joinCode").equalTo(joinCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                        Group group = groupSnapshot.getValue(Group.class);
                        if (group != null) {
                            // Add user to group members
                            dbRef.child(group.getGroupId()).child("members").child(userId).setValue(true)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(UnirseGrupo.this, "Te has unido al grupo exitosamente", Toast.LENGTH_SHORT).show();
                                        etJoinCode.setText("");
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(UnirseGrupo.this, "Error al unirse al grupo", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                } else {
                    Toast.makeText(UnirseGrupo.this, "Código de grupo inválido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UnirseGrupo.this, "Error al buscar grupo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}