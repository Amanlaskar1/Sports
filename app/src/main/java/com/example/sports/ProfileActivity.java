package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ProgressBar; // Import ProgressBar

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ProfileAdapter postsAdapter;
    private List<Post> profileList = new ArrayList<>();
    private ProgressBar progressBar; // Declare ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        FrameLayout container = findViewById(R.id.container);
        container.addView(contentView);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve the user ID passed from the previous activity
        String userId = getIntent().getStringExtra("userId");
        postsAdapter = new ProfileAdapter(this, profileList, userId);
        recyclerView.setAdapter(postsAdapter);
        // Fetch posts from the fetchProfile endpoint and filter by the logged-in user's ID
        fetchProfilePosts(userId);

        // Find and set onClickListener for the logout button
        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void fetchProfilePosts(String userId) {
        progressBar.setVisibility(View.VISIBLE); // Show ProgressBar before fetching data

        OkHttpClient client = new OkHttpClient();
        String url = "https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/fetchProfile?userId=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide ProgressBar on failure
                    // Handle failure
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE); // Hide ProgressBar on success or failure
                });

                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        // Check if the response is a JSON array
                        if (jsonData.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(jsonData);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String title = jsonObject.getString("title");
                                String content = jsonObject.getString("description");
                                String userId = jsonObject.getString("userId");
                                String image = jsonObject.getString("image");
                                String postId = jsonObject.getString("postId"); // Extract postId

                                // Pass postId to the Post constructor
                                Post post = new Post(title, content, userId, image, postId);
                                profileList.add(post);
                            }

                            runOnUiThread(() -> {
                                postsAdapter.notifyDataSetChanged();
                            });
                        } else {
                            // Handle the case where no posts are found
                            runOnUiThread(() -> {
                                // You can show a message or handle this case as needed
                                Toast.makeText(ProfileActivity.this, "No posts found for this user.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Method to handle logout
    private void logout() {
        // Implement logout logic here
        // For example, you can start the LoginActivity and finish this activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
