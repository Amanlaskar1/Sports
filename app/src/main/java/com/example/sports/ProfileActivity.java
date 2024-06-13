package com.example.sports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private ProgressBar progressBar;

    // Declare new UI elements
    private EditText usernameEditText;
    private EditText phoneNumberEditText;
    private RadioGroup genderRadioGroup;
    private Button saveButton;
    private TextView greetingTextView;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_GENDER = "gender";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        FrameLayout container = findViewById(R.id.container);
        container.addView(contentView);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        usernameEditText = findViewById(R.id.usernameEditText); // Initialize username EditText
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        saveButton = findViewById(R.id.saveButton);
        greetingTextView = findViewById(R.id.greetingTextView); // Initialize greeting TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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

        // Set onClickListener for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        // Load existing profile data from SharedPreferences
        loadProfileData();
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

    // Method to save profile data
    private void saveProfile() {
        String username = usernameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String gender = "";
        if (selectedGenderId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedGenderId);
            gender = selectedRadioButton.getText().toString();
        }

        // Save data in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.putString(KEY_GENDER, gender);
        editor.apply();

        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();

        // Update greeting TextView
        greetingTextView.setText("Hello, " + username);
    }

    // Method to load profile data
    private void loadProfileData() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "");
        String gender = sharedPreferences.getString(KEY_GENDER, "");

        usernameEditText.setText(username);
        phoneNumberEditText.setText(phoneNumber);

        if (!username.isEmpty()) {
            greetingTextView.setText("Hello, " + username);
        }

        if (!gender.isEmpty()) {
            if (gender.equals("Male")) {
                genderRadioGroup.check(R.id.maleRadioButton);
            } else if (gender.equals("Female")) {
                genderRadioGroup.check(R.id.femaleRadioButton);
            } else if (gender.equals("Other")) {
                genderRadioGroup.check(R.id.otherRadioButton);
            }
        }
    }
}
