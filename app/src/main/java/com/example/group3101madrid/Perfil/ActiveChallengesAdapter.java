package com.example.group3101madrid.Perfil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group3101madrid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActiveChallengesAdapter extends RecyclerView.Adapter<ActiveChallengesAdapter.ChallengeViewHolder> {
    private List<Desafio> challenges;

    public ActiveChallengesAdapter(List<Desafio> challenges) {
        this.challenges = challenges;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Desafio challenge = challenges.get(position);
        holder.titleText.setText(formatChallengeTitle(challenge.getType()));
        holder.dateText.setText(formatDate(challenge.getStartDate()));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    private String formatChallengeTitle(String type) {
        switch (type) {
            case "croquetas": return "101 Croquetas";
            case "bares": return "101 Bares";
            case "paisajes": return "101 Paisajes";
            default: return type;
        }
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return "Started: " + sdf.format(new Date(timestamp));
    }

    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView dateText;

        ChallengeViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.challengeTitle);
            dateText = itemView.findViewById(R.id.challengeDate);
        }
    }
}
