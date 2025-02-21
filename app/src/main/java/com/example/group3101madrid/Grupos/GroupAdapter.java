package com.example.group3101madrid.Grupos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<Group> groups;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupName, tvGroupDescription, tvMemberCount, tvRole;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvRole = itemView.findViewById(R.id.tvRole);
        }

        public void bind(Group group, OnGroupClickListener listener) {
            tvGroupName.setText(group.getName());
            tvGroupDescription.setText(group.getDescription());
            tvMemberCount.setText(group.getMembers().size() + " miembros");

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (group.getLeaderId().equals(userId)) {
                tvRole.setText("Líder");
                tvRole.setVisibility(View.VISIBLE);
            } else {
                tvRole.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}