package com.example.group3101madrid.ui.ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.group3101madrid.R;
import com.example.group3101madrid.databinding.FragmentRankingHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;


public class RankingHomeFragment extends Fragment {

    private FragmentRankingHomeBinding binding;
    private RankingAdapter rankingAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRankingHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupTopThree();
        loadMockData();
    }

    private void setupRecyclerView() {
        rankingAdapter = new RankingAdapter();
        binding.rvRankingList.setAdapter(rankingAdapter);
        binding.rvRankingList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRankingList.addItemDecoration(new DividerItemDecoration(requireContext(),
                LinearLayoutManager.VERTICAL));
    }

    private void setupTopThree() {
        // Example with mock data
        binding.tvFirstPlaceName.setText("John");
        binding.tvFirstPlaceScore.setText("20 desafios");

        binding.tvSecondPlaceName.setText("Maria");
        binding.tvSecondPlaceScore.setText("15 desafios");

        binding.tvThirdPlaceName.setText("Carlos");
        binding.tvThirdPlaceScore.setText("12 desafios");
    }

    private void loadMockData() {
        // Mock data for testing
        List<RankingItem> mockList = new ArrayList<>();
        mockList.add(new RankingItem(4, "Alex", 10, R.drawable.foto_perfil_ejemplo));
        mockList.add(new RankingItem(5, "Sarah", 8, R.drawable.foto_perfil_ejemplo));
        mockList.add(new RankingItem(6, "Mike", 7, R.drawable.foto_perfil_ejemplo));
        mockList.add(new RankingItem(7, "Lisa", 6, R.drawable.foto_perfil_ejemplo));

        rankingAdapter.updateList(mockList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
