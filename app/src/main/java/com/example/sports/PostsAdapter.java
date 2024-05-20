package com.example.sports;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> implements Filterable {
    private Context context;
    private List<Post> posts;
    private List<Post> postsFiltered; // List for filtered posts

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.postsFiltered = posts; // Initialize the filtered list with the original list
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postsFiltered.get(position); // Use the filtered list
        holder.titleTextView.setText(post.getTitle());

        // Split the content into words
        String[] words = post.getContent().split(" ");

        // Check if the content has more than 10 words
        if (words.length > 10) {
            String trimmedContent = String.join(" ", Arrays.copyOfRange(words, 0, 10)) + "... Read More";
            holder.contentTextView.setText(trimmedContent);

            // Set a click listener to show the full content when "Read More" is clicked
            holder.contentTextView.setOnClickListener(v -> holder.contentTextView.setText(post.getContent()));
        } else {
            holder.contentTextView.setText(post.getContent());
        }

        // Convert base64 string back to image
        byte[] imageBytes = Base64.decode(post.getImage(), Base64.DEFAULT);
        holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
    }

    @Override
    public int getItemCount() {
        return postsFiltered.size(); // Use the filtered list
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    postsFiltered = posts;
                } else {
                    List<Post> filteredList = new ArrayList<>();
                    for (Post post : posts) {
                        if (post.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(post);
                        }
                    }
                    postsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = postsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                postsFiltered = (List<Post>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, userIdTextView;
        ImageView imageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            userIdTextView = itemView.findViewById(R.id.userIdTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
