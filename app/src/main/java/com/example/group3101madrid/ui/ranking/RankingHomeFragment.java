package com.example.group3101madrid.ui.ranking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.group3101madrid.R;
import com.example.group3101madrid.databinding.FragmentRankingHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class RankingHomeFragment extends Fragment implements RankingService.RankingCallback {

    private static final String TAG = "RankingHomeFragment";
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

        RankingService.getRankingData(this);
    }

    private void setupRecyclerView() {
        rankingAdapter = new RankingAdapter();
        binding.rvRankingList.setAdapter(rankingAdapter);
        binding.rvRankingList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRankingList.addItemDecoration(new DividerItemDecoration(requireContext(),
                LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onRankingDataReceived(List<RankingItem> rankingList) {
        Log.d(TAG, "Received ranking data, size: " + rankingList.size());

        if (rankingList.isEmpty()) {
            // Show mock data if no ranking data is available
            Log.d(TAG, "No real ranking data, using mock data");
            setupMockData();
            return;
        }

        setupTopThree(rankingList);

        List<RankingItem> listUsers = new ArrayList<>();
        for (int i = 3; i < rankingList.size(); i++) {
            listUsers.add(rankingList.get(i));
        }

        Log.d(TAG, "Updating list with " + listUsers.size() + " users");
        rankingAdapter.updateList(listUsers);
        Toast.makeText(requireContext(),
                "Ranking cargado: " + rankingList.size() + " usuarios",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRankingDataFailed(String errorMessage) {
        Log.e(TAG, "Failed to load ranking: " + errorMessage);
        Toast.makeText(requireContext(), "Error al cargar el ranking", Toast.LENGTH_SHORT).show();

        // Show mock data as fallback
        setupMockData();
    }

    private void setupTopThree(List<RankingItem> rankingList) {
        Log.d(TAG, "Setting up top three from list of size: " + rankingList.size());

        // First place
        if (rankingList.size() >= 1) {
            RankingItem first = rankingList.get(0);
            binding.tvFirstPlaceName.setText(first.getName());
            binding.tvFirstPlaceScore.setText(first.getScore() + " puntos");

            Log.d(TAG, "First place: " + first.getName() + " with " + first.getScore() + " points");
            if (first.getProfileImageUrl() != null) {
                Log.d(TAG, "Loading profile image from URL for first place");
                Glide.with(requireContext())
                        .load(first.getProfileImageUrl())
                        .placeholder(R.drawable.profileranking1)
                        .error(R.drawable.profileranking1)
                        .into(binding.ivFirstPlace);
            } else {
                Log.d(TAG, "Using default profile image for first place");
                binding.ivFirstPlace.setImageResource(R.drawable.profileranking1);
            }
        } else {

            binding.tvFirstPlaceName.setText("---");
            binding.tvFirstPlaceScore.setText("0 puntos");
            binding.ivFirstPlace.setImageResource(R.drawable.profileranking1);
        }

        // Second place
        if (rankingList.size() >= 2) {
            RankingItem second = rankingList.get(1);
            binding.tvSecondPlaceName.setText(second.getName());
            binding.tvSecondPlaceScore.setText(second.getScore() + " puntos");

            Log.d(TAG, "Second place: " + second.getName() + " with " + second.getScore() + " points");

            // Set profile image
            if (second.getProfileImageUrl() != null) {
                Log.d(TAG, "Loading profile image from URL for second place");
                Glide.with(requireContext())
                        .load(second.getProfileImageUrl())
                        .placeholder(R.drawable.profileranking3)
                        .error(R.drawable.profileranking3)
                        .into(binding.ivSecondPlace);
            } else {
                Log.d(TAG, "Using default profile image for second place");
                binding.ivSecondPlace.setImageResource(R.drawable.profileranking3);
            }
        } else {
            // Default values if no data
            binding.tvSecondPlaceName.setText("---");
            binding.tvSecondPlaceScore.setText("0 puntos");
            binding.ivSecondPlace.setImageResource(R.drawable.profileranking3);
        }

        // Third place
        if (rankingList.size() >= 3) {
            RankingItem third = rankingList.get(2);
            binding.tvThirdPlaceName.setText(third.getName());
            binding.tvThirdPlaceScore.setText(third.getScore() + " puntos");

            Log.d(TAG, "Third place: " + third.getName() + " with " + third.getScore() + " points");

            // Set profile image
            if (third.getProfileImageUrl() != null) {
                Log.d(TAG, "Loading profile image from URL for third place");
                Glide.with(requireContext())
                        .load(third.getProfileImageUrl())
                        .placeholder(R.drawable.profileranking4)
                        .error(R.drawable.profileranking4)
                        .into(binding.ivThirdPlace);
            } else {
                Log.d(TAG, "Using default profile image for third place");
                binding.ivThirdPlace.setImageResource(R.drawable.profileranking4);
            }
        } else {
            // Default values if no data
            binding.tvThirdPlaceName.setText("---");
            binding.tvThirdPlaceScore.setText("0 puntos");
            binding.ivThirdPlace.setImageResource(R.drawable.profileranking4);
        }
    }

    private void setupMockData() {
        binding.tvFirstPlaceName.setText("John");
        binding.tvFirstPlaceScore.setText("20 puntos");

        binding.tvSecondPlaceName.setText("Maria");
        binding.tvSecondPlaceScore.setText("15 puntos");

        binding.tvThirdPlaceName.setText("Carlos");
        binding.tvThirdPlaceScore.setText("12 puntos");

        // Mock data for list
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