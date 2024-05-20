    package com.example.sports;

    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.util.Base64;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;

    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.snackbar.Snackbar;

    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.ByteArrayOutputStream;
    import java.io.IOException;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.MediaType;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    public class UpdatePostActivity extends AppCompatActivity {
        private UserRepository userRepository;
        private static final int PICK_IMAGE_REQUEST = 1;
        private Uri imageUri;
        private String postId;
        private String userId;
        private byte[] compressedImageBytes;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_post);

            userRepository = new UserRepositoryImpl();

            final EditText titleEditText = findViewById(R.id.titleEditText);
            final EditText contentEditText = findViewById(R.id.contentEditText);
            Button updateButton = findViewById(R.id.postButton);
            Button uploadImageButton = findViewById(R.id.uploadImageButton);
            ImageView imageView = findViewById(R.id.imageView);

            // Change the text of the button to "Update"
            updateButton.setText("Update");

            // Get the data passed from the adapter
            // Get the data passed from the adapter
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            byte[] compressedImageBytes = intent.getByteArrayExtra("image");
            postId = intent.getStringExtra("postId");
            userId = intent.getStringExtra("userId");

            // Set the data to the views
            titleEditText.setText(title);
            contentEditText.setText(description);
            if (compressedImageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(compressedImageBytes, 0, compressedImageBytes.length);
                imageView.setImageBitmap(bitmap);
            }

            uploadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFileChooser();
                }
            });

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String updatedTitle = titleEditText.getText().toString();
                    String updatedContent = contentEditText.getText().toString();

                    JSONObject json = new JSONObject();
                    try {
                        json.put("postId", postId);
                        json.put("title", updatedTitle);
                        json.put("content", updatedContent);
                        json.put("userId", userId);

                        if (imageUri != null) {
                            // New image selected, convert it to base64
                            String updatedImageBase64 = imageToBase64(imageUri);
                            json.put("image", updatedImageBase64);
                        } else {
                            // No new image selected, use the existing compressed image byte array
                            String existingImageBase64 = Base64.encodeToString(compressedImageBytes, Base64.DEFAULT);
                            json.put("image", existingImageBase64);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("UpdatePostActivity", "Sending update request with data: " + json.toString());

                    userRepository.updatePost(json.toString(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            Log.e("UpdatePostActivity", "Request failed", e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(v, "Request failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseBody = response.body().string();
                            Log.d("UpdatePostActivity", "Response: " + responseBody);

                            if (response.isSuccessful()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(v, "Post updated successfully", Snackbar.LENGTH_LONG).show();
                                        finish(); // Finish the activity and go back to the previous screen
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(v, "Failed to update post: " + responseBody, Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }

        private void openFileChooser() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                imageUri = data.getData();
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageURI(imageUri);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    compressedImageBytes = stream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String imageToBase64(Uri imageUri) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                return Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Bitmap base64ToBitmap(String base64String) {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }

        interface UserRepository {
            void updatePost(String postData, Callback callback);
        }

        class UserRepositoryImpl implements UserRepository {
            private final OkHttpClient client = new OkHttpClient();

            @Override
            public void updatePost(String postData, Callback callback) {
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"), postData);
                Request request = new Request.Builder()
                        .url("https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/updatePosts")
                        .put(body)
                        .build();
                client.newCall(request).enqueue(callback);
            }
        }
    }
