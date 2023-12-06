package org.example;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DatabaseUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    private static final String CONNECTION_STRING = "jdbc:sqlite:db.sqlite";

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";


    public static boolean isUsersTableEmpty() {
        log.info("Checking for empty users table");
        String sql = "SELECT COUNT(*) AS rowcount FROM users";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                log.info("Users table is empty");
                return rs.getInt("rowcount") == 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        log.info("Users table not empty");
        return false;
    }

    public static boolean isPostsTableEmpty() {
        log.info("Checking for empty comments table");
        String sql = "SELECT COUNT(*) AS rowcount FROM posts";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                log.info("Posts table is empty");
                return rs.getInt("rowcount") == 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        log.info("Posts table not empty");
        return false;
    }

    public static boolean isCommentsTableEmpty() {
        log.info("Checking for empty comments table");
        String sql = "SELECT COUNT(*) AS rowcount FROM comments";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                log.info("Comments table is empty");
                return rs.getInt("rowcount") == 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        log.info("Comments table not empty");
        return false;
    }

    public static boolean createUser(String username, String email, boolean isAdmin, String hashedPassword, int firstlogin) {
        log.info("Creating new user");
        if (usernameExists(username)) {
            log.info("Attempted to create a user with an existing username");
            System.out.println("Username already exists. Please choose another one.");
            return false;
        }
        if (emailExists(email)) {
            log.info("Attempted to create a user with an existing email");
            System.out.println("Email already exists. Please choose another one.");
            return false;
        }

        String sqlInsertUser = "INSERT INTO users (username, email, admin, hashedPassword, firstlogin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sqlInsertUser)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setBoolean(3, isAdmin);
            pstmt.setString(4, hashedPassword);
            pstmt.setInt(5, firstlogin);
            pstmt.executeUpdate();
            System.out.println("User created successfully.");
            log.info("User created with username: " + username);
            return true;
        } catch (SQLException e) {
            log.error("Error creating user: " + e.getMessage());
            if (e.getErrorCode() == 19) {
                System.out.println("A user with that username or email already exists.");
            } else {
                System.out.println(e.getMessage());
            }
            return false;
        } finally {
            DatabaseConnector.encryptDatabaseFile();
        }
    }


    public static void swapRole(User user, String username){
        log.info("Swapping "+username+" role");
        String sql = "UPDATE users SET admin = ? WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, !user.isAdmin());
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("User role changed successfully.");
            log.info("Swapped user role");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }


    public static void resetUserPassword(String username, String newPassword) {
        User user = findUserByEmailOrUsername(username);
        if (user != null) {
            log.info("Changing users password to hashed password");
            user.setPassword(newPassword);
            user.setFirstLogin(0);
            user.save();
        }
    }


    public static String randomAlphaNumeric(int count) {
        log.info("Generating user ID");
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean doesPostIdExist(String postId) {
        log.info("Checking for post ID");
        String sql = "SELECT id FROM posts WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            log.info("Found post with ID");
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        return false;
    }

    public static void createPost(String author, String text) {
        log.info("Creating post");
        String postId;
        do {
            postId = randomAlphaNumeric(6);
        } while (doesPostIdExist(postId));

        String sql = "INSERT INTO posts(id, author, text, timestamp) VALUES(?,?,?,datetime('now'))";

        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, author);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
            log.info("Created post");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }

    public static void fetchPosts() {
        log.info("Looking for posts");
        String sql = "SELECT id, author, text, timestamp FROM posts";
        int count = 0;
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                String id = rs.getString("id");
                String author = rs.getString("author");
                String text = rs.getString("text");
                String timestamp = rs.getString("timestamp");
                System.out.println("ID: " + id + "\tAuthor: " + author + "\tText: " + text + "\tTimestamp: " + timestamp);
                count++;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        log.info("Found " + count + " posts");
        if (count == 0) {
            System.out.println("No posts found.");
        }
    }


    public static User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                log.info("Attempting to log in user ID: " + findUserByEmailOrUsername(username).getId());
                String hashedPassword = rs.getString("hashedPassword");
                boolean isAdmin = rs.getBoolean("admin");
                if (SecurityUtils.checkPassword(password, hashedPassword)) {
                    log.info("Logged in user ID: " + findUserByEmailOrUsername(username).getId());
                    return new User(
                            username,
                            rs.getString("email"),
                            isAdmin,
                            hashedPassword,
                            rs.getInt("firstlogin")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
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
                String username = rs.getString("username");
                String email = rs.getString("email");
                boolean admin = rs.getBoolean("admin");
                String hashedPassword = rs.getString("hashedPassword");
                int firstlogin = rs.getInt("firstlogin");

                return new User(username, email, admin, hashedPassword, firstlogin);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }

        return null;
    }


    public static void showUsersPosts(User user) {
        String sql = "SELECT id, text FROM posts WHERE author = ?";
        log.info("Attempting to show posts from user: " + user.getId());

        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                String text = rs.getString("text");
                System.out.println("ID: " + id + " - " + text);
                count++;
            }
            log.info("Found " + count + " posts from user " + user.getId());
        } catch (SQLException e) {
            System.out.println("Error when attempting to display posts: " + e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }


    public static void deletePost(String postId, User user) {
        if (!doesPostIdExist(postId)) {
            System.out.println("Post with ID " + postId + " does not exist.");
            return;
        }
        String sql;

        if (user.isAdmin()) {
            log.info("Admin attempting to delete a post");
            sql = "DELETE FROM posts WHERE id = ?";
        } else {
            log.info("User attempting to delete one of their posts");
            sql = "DELETE FROM posts WHERE id = ? AND author = ?";
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId);
            if (!user.isAdmin()) {
                pstmt.setString(2, user.getUsername());
            }

            log.info("Deleted post " + postId);
            System.out.println("Deleted post " + postId);
        } catch (SQLException e) {
            System.out.println("Error when attempting to delete post: " + e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }


    public static void createComment(String postId, String author, String text) {
        String sql = "INSERT INTO comments(postId, author, text, timestamp) VALUES(?,?,?,datetime('now'))";
        log.info("Creating a comment");
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, author);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
            log.info("Created comment on post " + postId);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }



    public static void fetchComments(String postId) {
        String sql = "SELECT id, author, text, timestamp FROM comments WHERE postId = ?";

        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" +
                        rs.getString("author") + "\t" +
                        rs.getString("text") + "\t" +
                        rs.getString("timestamp"));
                count++;
            }
            log.info("Fetched "+count+" comments from post "+postId);
            if (count == 0) {
                System.out.println("No comments found for post with ID " + postId);
            }
        }catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }


    }


    public static Post fetchPostById(String postId) {
        String sql = "SELECT * FROM posts WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String author = rs.getString("author");
                String content = rs.getString("text");
                log.info("Found post "+postId);
                return new Post(postId, author, content);
            }
        } catch (SQLException e) {
            System.out.println("Error when attempting to fetch post: " + e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        log.info("Could not find post "+postId);
        return null;
    }

    public static void deleteComment(int commentId, User user) {
        Connection conn = DatabaseConnector.connect();
        if (!doesCommentIdExist(commentId)) {
            System.out.println("Comment with ID " + commentId + " does not exist.");
            return;
        }
        PreparedStatement pstmt = null;

        try {
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
                if (user.getUsername().equals(commentAuthor) || user.getUsername().equals(postAuthor) || user.isAdmin()) {
                    isAllowedToDelete = true;
                }
            }

            if (isAllowedToDelete) {
                String sqlDelete = "DELETE FROM comments WHERE id = ?";
                pstmt = conn.prepareStatement(sqlDelete);
                pstmt.setInt(1, commentId);
                pstmt.executeUpdate();
                log.info(user.getUsername()+" deleted comment "+ commentId);
            } else {
                System.out.println("You do not have permission to delete this comment.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }


    public static boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        return false;
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
        return false;
    }

    public static boolean doesCommentIdExist(int commentId) {
        String sql = "SELECT id FROM comments WHERE id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
            return false;
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }

    public static void deleteUser(String usernameToDelete) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usernameToDelete);
            pstmt.executeUpdate();
            System.out.println("User " + usernameToDelete + " deleted successfully.");
            log.info("Deleted user "+usernameToDelete);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }

    public static void fetchUsers() {
        String sql = "SELECT username, email, admin FROM users";
        try (Connection conn = DatabaseConnector.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                boolean admin = rs.getBoolean("admin");
                System.out.println("Username: " + username + "\tEmail: " + email + "\tAdmin: " + admin);
                count++;
            }
            log.info("Found "+count+" users");
            if (count == 0) {
                System.out.println("No users found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }

    //TODO:more
}
