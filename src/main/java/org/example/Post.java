package org.example;

public class Post {
    private String id;
    private String author;
    private String content;

    public Post(String id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    // Getters and setters for id, author, and content
    // ...

    @Override
    public String toString() {
        return "Post ID: " + id + "\nAuthor: " + author + "\nContent: " + content;
    }
}
