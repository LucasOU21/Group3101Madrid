package com.example.group3101madrid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SimpleExperienciaAdapter extends RecyclerView.Adapter<SimpleExperienciaAdapter.ViewHolder> {

    private List<SimpleExperienciasActivity.SimpleExperiencia> experiencias;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SimpleExperienciasActivity.SimpleExperiencia experiencia);
    }

    public SimpleExperienciaAdapter(List<SimpleExperienciasActivity.SimpleExperiencia> experiencias, OnItemClickListener listener) {
        this.experiencias = experiencias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_experiencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SimpleExperienciasActivity.SimpleExperiencia experiencia = experiencias.get(position);

        // Set title
        holder.tvTitle.setText(experiencia.getTitle());

        // Set completed status
        if (experiencia.isCompleted()) {
            holder.ivStatus.setImageResource(android.R.drawable.ic_menu_edit);
            holder.ivStatus.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.green));
            holder.tvStatus.setText("Completado");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else {
            holder.ivStatus.setImageResource(android.R.drawable.ic_menu_view);
            holder.ivStatus.clearColorFilter();
            holder.tvStatus.setText("Pendiente");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        }

        // Load image
        if (experiencia.getImageUrl() != null && !experiencia.getImageUrl().isEmpty()) {
            try {
                // Try to load as resource
                int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                        experiencia.getImageUrl(),
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

                if (resourceId != 0) {
                    Glide.with(holder.itemView.getContext())
                            .load(resourceId)
                            .centerCrop()
                            .into(holder.ivImage);
                } else if (experiencia.getImageUrl().startsWith("http")) {
                    // Try as URL
                    Glide.with(holder.itemView.getContext())
                            .load(experiencia.getImageUrl())
                            .centerCrop()
                            .into(holder.ivImage);
                } else {
                    // Load default
                    holder.ivImage.setImageResource(R.drawable.img_101_logo);
                }
            } catch (Exception e) {
                // Load default
                holder.ivImage.setImageResource(R.drawable.img_101_logo);
            }
        } else {
            // Load default
            holder.ivImage.setImageResource(R.drawable.img_101_logo);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(experiencia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return experiencias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
        ImageView ivStatus;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}