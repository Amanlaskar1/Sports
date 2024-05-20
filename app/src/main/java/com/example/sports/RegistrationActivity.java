package com.example.sports;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class RegistrationActivity extends AppCompatActivity {
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize the UserRepository
        userRepository = new UserRepositoryImpl();

        // Get references to the input fields and the register button
        final EditText emailEditText = findViewById(R.id.emailEditText);
        final EditText usernameEditText = findViewById(R.id.usernameEditText);
        final EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button registerButton = findViewById(R.id.registerButton);
        Button navigateToLoginButton = findViewById(R.id.navigateToLoginButton);

        navigateToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Set a click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user data from the input fields
                String email = emailEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Register the user
                userRepository.registerUser(username, password, email, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.e("RegistrationActivity", "Request failed", e);
                        Snackbar.make(v, "Request failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                        String responseBody = response.body().string();
                        Log.d("RegistrationActivity", "Response: " + responseBody);
                        Snackbar.make(v, "Response: " + responseBody, Snackbar.LENGTH_LONG).show();

                        // Check if the registration was successful
                        if (response.isSuccessful()) {
                            // Navigate to the Login page
                            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Finish the current activity to prevent back navigation to the registration page
                        }
                    }

                });
            }
        });
    }

    interface UserRepository {
        void registerUser(String username, String password, String email, Callback callback);
    }

    class UserRepositoryImpl implements UserRepository {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        public void registerUser(String username, String password, String email, Callback callback) {
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
                json.put("username", username);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), json.toString());
            Request request = new Request.Builder()
                    .url("https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/registration")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callback);
        }
    }
}
