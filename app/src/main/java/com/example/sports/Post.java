package com.example.sports;

public class Post {
    private String title;
    private String content;
    private String userId;
    private String image;
    private String postId;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Constructor
    public Post(String title, String content, String userId, String image, String postId) { // Modify this line
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.image = image;
        this.postId = postId; // Add this line
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostId() { // Add this method
        return postId;
    }

    public void setPostId(String postId) { // Add this method
        this.postId = postId;
    }
}
