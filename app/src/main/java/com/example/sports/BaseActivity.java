package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Retrieve the user ID from the intent
        userId = getIntent().getStringExtra("userId");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    // Intent for Home Activity
                    Intent homeIntent = new Intent(BaseActivity.this, HomeActivity.class);
                    // Pass the user ID to the HomeActivity
                    homeIntent.putExtra("userId", userId);
                    startActivity(homeIntent);
                } else if (id == R.id.navigation_profile) {
                    // Intent for Profile Activity
                    Intent profileIntent = new Intent(BaseActivity.this, ProfileActivity.class);
                    // Pass the user ID to the ProfileActivity
                    profileIntent.putExtra("userId", userId);
                    startActivity(profileIntent);
                }
                return true;
            }
        });
    }
}
