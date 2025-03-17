// UserSearchAdapter.java
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
import com.example.group3101madrid.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.SearchViewHolder> {

    private List<User> usersList;
    private Context context;
    private UserSearchListener listener;

    public interface UserSearchListener {
        void onSendFriendRequest(User user);
    }

    public UserSearchAdapter(List<User> usersList, Context context, UserSearchListener listener) {
        this.usersList = usersList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        User user = usersList.get(position);

        holder.tvUsername.setText(user.getUsername());

        // Load profile image if available
        String profilePic = user.getProfilePicture();
        if (profilePic != null && !profilePic.isEmpty()) {
            Glide.with(context)
                    .load(profilePic)
                    .placeholder(R.drawable.foto_perfil_ejemplo)
                    .error(R.drawable.foto_perfil_ejemplo)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.foto_perfil_ejemplo);
        }

        // Add friend button click
        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSendFriendRequest(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvUsername;
        Button btnAddFriend;

        SearchViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}