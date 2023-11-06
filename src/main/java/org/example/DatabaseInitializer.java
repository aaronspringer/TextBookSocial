package org.example;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    public static void initializeDatabase(Connection conn) {
        String sqlCreateUsers =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "email TEXT NOT NULL UNIQUE," +
                        "admin BOOLEAN NOT NULL," +
                        "hashedPassword TEXT NOT NULL)";


        String sqlCreatePosts =
                "CREATE TABLE IF NOT EXISTS posts (" +
                        "id TEXT NOT NULL," +
                        "author TEXT NOT NULL," +
                        "text TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL);";


        String sqlCreateComments =
                "CREATE TABLE IF NOT EXISTS comments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "postId INTEGER NOT NULL," +
                        "userId INTEGER NOT NULL," +
                        "author TEXT NOT NULL," +
                        "text TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "FOREIGN KEY (postId) REFERENCES posts(id)," +
                        "FOREIGN KEY (userId) REFERENCES users(id));";


        try (Statement stmt = conn.createStatement()) {
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
