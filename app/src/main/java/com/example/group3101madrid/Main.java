// Main.java
package com.example.group3101madrid;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.group3101madrid.databinding.ActivityMainBinding;

public class Main extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_home);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            //Setup the bottom navigation with NavController
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}