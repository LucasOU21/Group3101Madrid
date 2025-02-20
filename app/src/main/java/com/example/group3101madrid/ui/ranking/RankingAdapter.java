package com.example.group3101madrid.ui.ranking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<RankingItem> rankingList;

    public RankingAdapter() {
        rankingList = new ArrayList<>();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRankPosition;
        TextView tvName;
        TextView tvScore;
        CircleImageView ivProfile;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankPosition = itemView.findViewById(R.id.tvRankPosition);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }

        public void bind(RankingItem item) {
            tvRankPosition.setText(String.valueOf(item.getPosition()));
            tvName.setText(item.getName());
            tvScore.setText(item.getScore() + " desafios");

            if (item.getProfileImage() != null) {
                ivProfile.setImageResource(item.getProfileImage());
            }
        }
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        holder.bind(rankingList.get(position));
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    public void updateList(List<RankingItem> newList) {
        rankingList = newList;
        notifyDataSetChanged();
    }
}