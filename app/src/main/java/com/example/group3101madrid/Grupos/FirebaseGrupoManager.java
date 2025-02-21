package com.example.group3101madrid.Grupos;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseGrupoManager {
    private static final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static void getUserGroups(GroupsCallback callback) {
        String userId = mAuth.getCurrentUser().getUid();
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> userGroups = new ArrayList<>();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null && group.getMembers().containsKey(userId)) {
                        userGroups.add(group);
                    }
                }
                callback.onGroupsLoaded(userGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void createGroup(Group group, DatabaseCallback callback) {
        String groupId = groupsRef.push().getKey();
        group.setGroupId(groupId);

        groupsRef.child(groupId).setValue(group)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void joinGroup(String joinCode, DatabaseCallback callback) {
        String userId = mAuth.getCurrentUser().getUid();

        groupsRef.orderByChild("joinCode").equalTo(joinCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("Código de grupo inválido");
                            return;
                        }

                        DataSnapshot groupSnapshot = snapshot.getChildren().iterator().next();
                        Group group = groupSnapshot.getValue(Group.class);

                        if (group == null) {
                            callback.onError("Error al obtener datos del grupo");
                            return;
                        }

                        if (group.getMembers().containsKey(userId)) {
                            callback.onError("Ya eres miembro de este grupo");
                            return;
                        }

                        groupsRef.child(group.getGroupId())
                                .child("members")
                                .child(userId)
                                .setValue(true)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public static void leaveGroup(String groupId, DatabaseCallback callback) {
        String userId = mAuth.getCurrentUser().getUid();
        groupsRef.child(groupId)
                .child("members")
                .child(userId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Callback interfaces
    public interface GroupsCallback {
        void onGroupsLoaded(List<Group> groups);
        void onError(String error);
    }

    public interface DatabaseCallback {
        void onSuccess();
        void onError(String error);
    }
}