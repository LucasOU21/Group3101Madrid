package com.example.group3101madrid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
// Add any image loading library import like Glide or Picasso

public class DesafioDetailActivity extends AppCompatActivity {

    private ImageView headerImage;
    private TextView titleText;
    private TextView descriptionText;
    private TextView locationText;
    private Button startButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desafio_detail);

        // Get the desafio type from intent
        String desafioType = getIntent().getStringExtra("DESAFIO_TYPE");

        // Initialize views
        headerImage = findViewById(R.id.header_image);
        titleText = findViewById(R.id.title_text);
        descriptionText = findViewById(R.id.description_text);
        locationText = findViewById(R.id.location_text);
        startButton = findViewById(R.id.start_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Load data from Firebase
        loadDesafioData(desafioType);

        // Set up button listeners
        startButton.setOnClickListener(v -> {
            // Handle start button click
            // Add your logic to start the challenge
        });

        cancelButton.setOnClickListener(v -> {
            // Simply finish the activity to return to home
            finish();
        });
    }

    private void loadDesafioData(String desafioType) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("desafios").child(desafioType);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get data from snapshot
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Update UI
                    titleText.setText(title);
                    descriptionText.setText(description);
                    locationText.setText(location);

                    // Load image using your preferred image loading library
                    Glide.with(DesafioDetailActivity.this)
                            .load(imageUrl)
                            .into(headerImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DesafioDetailActivity.this,
                        "Error loading data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}