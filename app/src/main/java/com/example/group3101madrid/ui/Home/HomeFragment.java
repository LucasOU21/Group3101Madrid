package com.example.group3101madrid.ui.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;

import com.example.group3101madrid.DesafioDetailActivity;
import com.example.group3101madrid.Perfil.Perfil;
import com.example.group3101madrid.R;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_home, container, false);

        // Setup profile button click
        ImageButton profileButton = view.findViewById(R.id.imageBtnPerfil);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Perfil.class);
            startActivity(intent);
        });

        // Setup all containers with their respective types
        setupClickableContainer(view, R.id.container_kilometros, "kilometros");
        setupClickableContainer(view, R.id.container_canchas, "canchas");
        setupClickableContainer(view, R.id.container_fuentes, "fuentes");
        setupClickableContainer(view, R.id.container_monumentos, "monumentos");
        setupClickableContainer(view, R.id.container_playas, "playas");
        setupClickableContainer(view, R.id.container_museos, "museos");
        setupClickableContainer(view, R.id.container_poesia, "poesia");
        setupClickableContainer(view, R.id.container_jardines, "jardines");
        setupClickableContainer(view, R.id.container_mercados, "mercados");
        setupClickableContainer(view, R.id.container_festivales, "festivales");
        setupClickableContainer(view, R.id.container_teatros, "teatros");
        setupClickableContainer(view, R.id.container_galerias, "galerias");

        return view;
    }

    private void setupClickableContainer(View view, int containerId, final String desafioType) {
        LinearLayout container = view.findViewById(containerId);
        container.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DesafioDetailActivity.class);
            intent.putExtra("DESAFIO_TYPE", desafioType);
            startActivity(intent);
        });
    }
}