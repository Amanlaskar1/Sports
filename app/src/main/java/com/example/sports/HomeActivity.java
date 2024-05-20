package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private List<Post> postsList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView noPostsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_home and add it to the container in BaseActivity
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_home, null, false);
        FrameLayout container = findViewById(R.id.container);
        container.addView(contentView);

        fab = contentView.findViewById(R.id.fab);
        recyclerView = contentView.findViewById(R.id.recyclerView);
        progressBar = contentView.findViewById(R.id.progressBar);
        noPostsTextView = contentView.findViewById(R.id.noPostsTextView);
        SearchView searchView = contentView.findViewById(R.id.searchView);


        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(this, postsList);
        recyclerView.setAdapter(postsAdapter);

        String userId = getIntent().getStringExtra("userId");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postsAdapter.getFilter().filter(newText);
                return false;
            }
        });
        // Fetch posts from the endpoint
        fetchPosts();
    }

    private void fetchPosts() {
        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient();
        String url = "https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/fetchPosts";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Hide the progress bar
                    progressBar.setVisibility(View.GONE);
                    // Handle failure
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
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
                            postsList.add(post);
                        }

                        runOnUiThread(() -> {
                            // Hide the progress bar
                            progressBar.setVisibility(View.GONE);

                            if (postsList.isEmpty()) {
                                // Show the "No posts" message
                                noPostsTextView.setVisibility(View.VISIBLE);
                            } else {
                                // Hide the "No posts" message
                                noPostsTextView.setVisibility(View.GONE);
                            }

                            postsAdapter.notifyDataSetChanged();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            // Hide the progress bar
                            progressBar.setVisibility(View.GONE);
                            // Handle failure
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        // Hide the progress bar
                        progressBar.setVisibility(View.GONE);
                        // Handle failure
                    });
                }
            }
        });
    }
}
