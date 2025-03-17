// FriendRequestAdapter.java
package com.example.group3101madrid.Amigos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requestsList;
    private Context context;
    private FriendRequestListener listener;

    public interface FriendRequestListener {
        void onAcceptRequest(FriendRequest request);
        void onRejectRequest(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestsList, Context context, FriendRequestListener listener) {
        this.requestsList = requestsList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestsList.get(position);

        holder.tvUsername.setText(request.getSenderUsername());

        // Load profile image
        if (request.getSenderProfilePictureUrl() != null && !request.getSenderProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(request.getSenderProfilePictureUrl())
                    .placeholder(R.drawable.foto_perfil_ejemplo)
                    .error(R.drawable.foto_perfil_ejemplo)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.foto_perfil_ejemplo);
        }

        // Button clicks
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptRequest(request);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectRequest(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvUsername;
        Button btnAccept;
        Button btnReject;

        RequestViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}