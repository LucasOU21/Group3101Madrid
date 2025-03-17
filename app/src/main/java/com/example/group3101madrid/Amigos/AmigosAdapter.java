// AmigosAdapter.java
package com.example.group3101madrid.Amigos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AmigosAdapter extends RecyclerView.Adapter<AmigosAdapter.AmigoViewHolder> {

    private List<Friend> friendsList;
    private Context context;

    public AmigosAdapter(List<Friend> friendsList, Context context) {
        this.friendsList = friendsList;
        this.context = context;
    }

    @NonNull
    @Override
    public AmigoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_amigo, parent, false);
        return new AmigoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmigoViewHolder holder, int position) {
        Friend friend = friendsList.get(position);

        holder.tvUsername.setText(friend.getUsername());
        holder.tvStatus.setText(friend.getStatus());

        // Load profile image
        if (friend.getProfilePictureUrl() != null && !friend.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(friend.getProfilePictureUrl())
                    .placeholder(R.drawable.foto_perfil_ejemplo)
                    .error(R.drawable.foto_perfil_ejemplo)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.foto_perfil_ejemplo);
        }

        // Menu button click
        holder.btnMenu.setOnClickListener(v -> showOptions(friend));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    private void showOptions(Friend friend) {
        String[] options = {"Ver perfil", "Eliminar amigo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(friend.getUsername())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // View profile - You can implement this functionality
                            Toast.makeText(context, "Ver perfil de " + friend.getUsername(), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // Remove friend
                            confirmRemoveFriend(friend);
                            break;
                    }
                })
                .show();
    }

    private void confirmRemoveFriend(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar amigo")
                .setMessage("¿Estás seguro de que quieres eliminar a " + friend.getUsername() + " de tu lista de amigos?")
                .setPositiveButton("Sí", (dialog, which) -> removeFriend(friend))
                .setNegativeButton("No", null)
                .show();
    }

    private void removeFriend(Friend friend) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Remove from both users' friend lists
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(currentUserId)
                .child(friend.getUserId());

        DatabaseReference friendUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("friends")
                .child(friend.getUserId())
                .child(currentUserId);

        currentUserFriendsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Also remove from friend's list
                    friendUserFriendsRef.removeValue();

                    Toast.makeText(context, "Amigo eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al eliminar amigo", Toast.LENGTH_SHORT).show();
                });
    }

    static class AmigoViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvUsername;
        TextView tvStatus;
        ImageButton btnMenu;

        AmigoViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}