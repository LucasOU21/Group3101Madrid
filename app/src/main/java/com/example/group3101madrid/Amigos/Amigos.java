// Amigos.java
package com.example.group3101madrid.Amigos;

import android.os.Bundle;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.group3101madrid.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Amigos extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amigos);

        // Initialize back button
        ImageView ivBackButton = findViewById(R.id.ivBackButton);
        ivBackButton.setOnClickListener(v -> finish());

        // Initialize ViewPager and TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Setup ViewPager with adapter
        AmigosTabAdapter tabAdapter = new AmigosTabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Mis Amigos");
                    break;
                case 1:
                    tab.setText("Solicitudes");
                    break;
                case 2:
                    tab.setText("Añadir");
                    break;
            }
        }).attach();
    }

    private static class AmigosTabAdapter extends FragmentStateAdapter {
        public AmigosTabAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AmigosListFragment();
                case 1:
                    return new AmigosRequestsFragment();
                case 2:
                    return new AmigosAddFragment();
                default:
                    return new AmigosListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}