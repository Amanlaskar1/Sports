package com.example.sports;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PostActivity extends BaseActivity {
    private UserRepository userRepository;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_add_post, null, false);
        FrameLayout container = findViewById(R.id.container);
        container.addView(contentView);

        userRepository = new UserRepositoryImpl();

        final EditText titleEditText = findViewById(R.id.titleEditText);
        final EditText contentEditText = findViewById(R.id.contentEditText);
        Button postButton = findViewById(R.id.postButton);
        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        ImageView imageView = findViewById(R.id.imageView);

        String userId = getIntent().getStringExtra("userId");
        Toast.makeText(this, "User ID: " + userId, Toast.LENGTH_SHORT).show();

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();
                String imageBase64 = imageToBase64(imageUri);

                JSONObject json = new JSONObject();
                try {
                    json.put("title", title);
                    json.put("content", content);
                    json.put("userId", userId);
                    json.put("image", imageBase64);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                userRepository.createPost(json.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.e("PostActivity", "Request failed", e);
                        Snackbar.make(v, "Request failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                        String responseBody = response.body().string();
                        Log.d("PostActivity", "Response: " + responseBody);
                        Snackbar.make(v, "Response: " + responseBody, Snackbar.LENGTH_LONG).show();
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!= null && data.getData()!= null) {
            imageUri = data.getData();
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
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

    interface UserRepository {
        void createPost(String postData, Callback callback);
    }

    class UserRepositoryImpl implements UserRepository {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        public void createPost(String postData, Callback callback) {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), postData);
            Request request = new Request.Builder()
                    .url("https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/makePost")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callback);
        }
    }
}
