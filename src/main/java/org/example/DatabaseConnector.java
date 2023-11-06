package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:sqlite:db.sqlite";

    public static Connection connect() {
        Connection conn = null;
        try {
            // Create a connection to the database
            conn = DriverManager.getConnection(URL);
            // System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
