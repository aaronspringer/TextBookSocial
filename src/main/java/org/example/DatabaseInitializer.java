package org.example;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    public static void initializeDatabase(Connection conn) {
        String enableFK = "PRAGMA foreign_keys = ON;";
        String sqlCreateUsers =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "email TEXT NOT NULL UNIQUE," +
                        "admin BOOLEAN NOT NULL," +
                        "hashedPassword TEXT NOT NULL," +
                        "firstlogin INTEGER NOT NULL)";


        String sqlCreatePosts =
                "CREATE TABLE IF NOT EXISTS posts (" +
                        "id TEXT PRIMARY KEY," +
                        "author TEXT NOT NULL," +
                        "text TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "FOREIGN KEY (author) REFERENCES users(username) ON DELETE CASCADE);";

        String sqlCreateComments =
                "CREATE TABLE IF NOT EXISTS comments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "postId TEXT NOT NULL," +
                        "author TEXT NOT NULL," +
                        "text TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "FOREIGN KEY (postId) REFERENCES posts(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (author) REFERENCES users(username));";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(enableFK);
            log.info("Foreign key support enabled");

            stmt.execute(sqlCreateUsers);
            log.info("Created or loaded users table");

            stmt.execute(sqlCreatePosts);
            log.info("Created or loaded posts table");

            stmt.execute(sqlCreateComments);
            log.info("Created or loaded comments table");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
