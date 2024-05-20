package com.example.sports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private static final String TAG = "ProfileAdapter";

    private Context context;
    private List<Post> posts;
    private String userId;

    public ProfileAdapter(Context context, List<Post> posts, String userId) {
        this.context = context;
        this.posts = posts;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.titleTextView.setText(post.getTitle());
        holder.contentTextView.setText(post.getContent());
        holder.userIdTextView.setText(post.getUserId());

        byte[] imageBytes = Base64.decode(post.getImage(), Base64.DEFAULT);
        holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));

        holder.deleteButton.setOnClickListener(v -> {
            deletePost(post.getPostId(), userId, position, holder.itemView);
        });

        holder.updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdatePostActivity.class);
            intent.putExtra("title", post.getTitle());
            intent.putExtra("description", post.getContent());

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] compressedImageBytes = stream.toByteArray();
            intent.putExtra("image", compressedImageBytes);

            intent.putExtra("postId", post.getPostId());
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, userIdTextView;
        ImageView imageView;
        Button deleteButton, updateButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            userIdTextView = itemView.findViewById(R.id.userIdTextView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            updateButton = itemView.findViewById(R.id.updateButton);
        }
    }

    private void deletePost(String postId, String userId, int position, View view) {
        String url = "https://ap-south-1.aws.data.mongodb-api.com/app/application-1-ilvcvhh/endpoint/deletePost";

        JSONObject json = new JSONObject();
        try {
            json.put("postId", postId);
            json.put("userId", userId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object", e);
            e.printStackTrace();
        }

        Log.d(TAG, "Deleting post with postId: " + postId + ", userId: " + userId);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed", e);
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Snackbar.make(view, "Failed to delete post", Snackbar.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Post deleted successfully");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Snackbar.make(view, "Post deleted successfully", Snackbar.LENGTH_LONG).show();
                        posts.remove(position);
                        notifyItemRemoved(position);
                    });
                } else {
                    Log.e(TAG, "Failed to delete post. Response code: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Snackbar.make(view, "Failed to delete post", Snackbar.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
