package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Retrieve the user ID from the intent
        userId = getIntent().getStringExtra("userId");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    Intent homeIntent = new Intent(AboutActivity.this, HomeActivity.class);
                    homeIntent.putExtra("userId", userId);
                    startActivity(homeIntent);
                } else if (id == R.id.navigation_profile) {
                    Intent profileIntent = new Intent(AboutActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("userId", userId);
                    startActivity(profileIntent);
                } else if (id == R.id.navigation_about) {
                    // Already on the About page, do nothing
                }
                return true;
            }
        });
    }
}
