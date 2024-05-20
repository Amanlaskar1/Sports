package com.example.sports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);

        Button navigateToLoginButton = findViewById(R.id.navigateToRegisterButton);

        navigateToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/login";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response!= null &&!response.isEmpty()) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        String message = jsonResponse.getString("message");
                                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                        if (message.equals("User logged in successfully.")) {
                                            // Extract and display the user ID in a toast
                                            String userId = jsonResponse.getString("userId");
                                            Toast.makeText(LoginActivity.this, "User ID: " + userId, Toast.LENGTH_SHORT).show();

                                            // Save user ID to SharedPreferences
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("userId", userId);
                                            editor.apply();

                                            // Navigate to HomeActivity and pass the user ID
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            intent.putExtra("userId", userId); // Pass the user ID as an extra
                                            startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Received empty response from server.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Error logging in. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() {
                        try {
                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("username", emailEditText.getText().toString());
                            jsonBody.put("password", passwordEditText.getText().toString());
                            return jsonBody.toString().getBytes("utf-8");
                        } catch (Exception e) {
                            return null;
                        }
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(postRequest);
            }
        });
    }
}
