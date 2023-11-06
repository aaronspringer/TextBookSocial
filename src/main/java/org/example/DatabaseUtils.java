package org.example;

import java.sql.*;

public class DatabaseUtils {

    private static final String CONNECTION_STRING = "jdbc:sqlite:db.sqlite";

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    public static boolean isUsersTableEmpty() {
        String sql = "SELECT COUNT(*) AS rowcount FROM users";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rowcount") == 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static boolean createUser(String username, String email, boolean isAdmin, String hashedPassword) {
        if (usernameExists(username)) {
            System.out.println("Username already exists. Please choose another one.");
            return false;
        }
        if (emailExists(email)) {
            System.out.println("Email already exists. Please choose another one.");
            return false;
        }

        String sqlInsertUser = "INSERT INTO users (username, email, admin, hashedPassword) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sqlInsertUser)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setBoolean(3, isAdmin);
            pstmt.setString(4, hashedPassword);
            pstmt.executeUpdate();
            System.out.println("User created successfully.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { // SQLite error code for a constraint violation.
                System.out.println("A user with that username or email already exists.");
            } else {
                System.out.println(e.getMessage());
            }
        }
        return true;
    }

    public static void resetUserPassword(String username, String newPassword) {
        User user = findUserByEmailOrUsername(username);
        if (user != null) {
            // Hash the new password before storing it
            String hashedNewPassword = SecurityUtils.hashPassword(newPassword);

            // Update the user's password
            user.setPassword(hashedNewPassword);
            // Save the updated user back to the database
            user.save();
        }
    }


    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean doesPostIdExist(String postId) {
        String sql = "SELECT id FROM posts WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static void createPost(String author, String text) {
        String postId;
        do {
            postId = randomAlphaNumeric(6);
        } while (doesPostIdExist(postId));

        String sql = "INSERT INTO posts(id, author, text, timestamp) VALUES(?,?,?,datetime('now'))";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, author);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void fetchPosts() {
        String sql = "SELECT id, author, text, timestamp FROM posts";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Retrieve ID as a string
                String id = rs.getString("id");
                String author = rs.getString("author");
                String text = rs.getString("text");
                String timestamp = rs.getString("timestamp");
                System.out.println("ID: " + id + "\tAuthor: " + author + "\tText: " + text + "\tTimestamp: " + timestamp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String hashedPassword = rs.getString("hashedPassword");
                boolean isAdmin = rs.getBoolean("admin");
                if (SecurityUtils.checkPassword(password, hashedPassword)) {
                    return new User(
                            id,
                            username,
                            rs.getString("email"),
                            isAdmin,
                            hashedPassword
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static User findUserByEmailOrUsername(String emailOrUsername) {
        String sql = "SELECT * FROM users WHERE email = ? OR username = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emailOrUsername);
            pstmt.setString(2, emailOrUsername);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                boolean admin = rs.getBoolean("admin");
                String hashedPassword = rs.getString("hashedPassword");

                // Assuming you have a constructor in your User class like this:
                // User(int id, String username, String email, boolean admin, String hashedPassword)
                return new User(id, username, email, admin, hashedPassword);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }

        return null;
    }


    public static void showUsersPosts(User user) {
        String sql = "SELECT id, text FROM posts WHERE author = ?"; // Change "username" to "author"

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String text = rs.getString("text");
                System.out.println("ID: " + id + " - " + text);
            }
        } catch (SQLException e) {
            System.out.println("Error when attempting to display posts: " + e.getMessage());
        }
    }


    public static void deletePost(String postId, User user) {
        String sql;

        // If the user is an admin, they can delete any post
        if (user.isAdmin()) {
            sql = "DELETE FROM posts WHERE id = ?";
        } else {
            // If the user is not an admin, they can only delete their own posts
            sql = "DELETE FROM posts WHERE id = ? AND author = ?";
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId); // Use setString instead of setInt
            if (!user.isAdmin()) {
                pstmt.setString(2, user.getUsername());
            }

            int affectedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error when attempting to delete post: " + e.getMessage());
        }
    }


    public static void createComment(String postId, String author, String text) {
        String sql = "INSERT INTO comments(postId, author, text, timestamp) VALUES(?,?,?,datetime('now'))";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, author);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void fetchComments(String postId) {
        String sql = "SELECT id, author, text, timestamp FROM comments WHERE postId = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" +
                        rs.getString("author") + "\t" +
                        rs.getString("text") + "\t" +
                        rs.getString("timestamp"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static Post fetchPostById(String postId) {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String author = rs.getString("author");
                String content = rs.getString("text");
                // You can also retrieve other fields if your Post object requires them
                return new Post(postId, author, content);
            }
        } catch (SQLException e) {
            System.out.println("Error when attempting to fetch post: " + e.getMessage());
        }
        return null;
    }

    public static void deleteComment(int commentId, User user) {
        Connection conn = DatabaseConnector.connect();
        PreparedStatement pstmt = null;

        try {
            // Check if the user is the comment's author, the post's author, or an admin
            String sqlCheck = "SELECT c.author AS commentAuthor, p.author AS postAuthor " +
                    "FROM comments c " +
                    "JOIN posts p ON c.postId = p.id " +
                    "WHERE c.id = ?";
            pstmt = conn.prepareStatement(sqlCheck);
            pstmt.setInt(1, commentId);
            ResultSet rs = pstmt.executeQuery();

            boolean isAllowedToDelete = false;
            if (rs.next()) {
                String commentAuthor = rs.getString("commentAuthor");
                String postAuthor = rs.getString("postAuthor");
                // If the user is the comment's author, the post's author, or an admin
                if (user.getUsername().equals(commentAuthor) || user.getUsername().equals(postAuthor) || user.isAdmin()) {
                    isAllowedToDelete = true;
                }
            }

            if (isAllowedToDelete) {
                // Delete the comment
                String sqlDelete = "DELETE FROM comments WHERE id = ?";
                pstmt = conn.prepareStatement(sqlDelete);
                pstmt.setInt(1, commentId);
                int affectedRows = pstmt.executeUpdate();
            } else {
                System.out.println("You do not have permission to delete this comment.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex.getMessage());
            }
        }
    }


    public static boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            // If the result set is not empty, the username exists
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            // If the result set is not empty, the email exists
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    //TODO:more
}
