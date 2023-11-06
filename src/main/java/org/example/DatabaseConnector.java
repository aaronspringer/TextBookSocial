package org.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

    public class DatabaseConnector {

        private static final String ENCRYPTED_DB_FILE = "edb.sqlite";
        private static final String DECRYPTED_DB_FILE = "db.sqlite";
        private static final String KEY = "totallygoodkeyss"; // 128-bit key, should be securely managed

        public static Connection connect() {
            Connection conn = null;
            try {
                // Connect to the decrypted database file
                String url = "jdbc:sqlite:" + DECRYPTED_DB_FILE;
                conn = DriverManager.getConnection(url);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }

        public static void decryptDatabase() {
            try {
                // Check if the encrypted database file exists
                if (Files.exists(Paths.get(ENCRYPTED_DB_FILE))) {
                    // Decrypt the database file for use
                    FileDecryptor.decrypt(KEY, ENCRYPTED_DB_FILE, DECRYPTED_DB_FILE);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


    public static String getEncryptedDbFile() {
        return ENCRYPTED_DB_FILE;
    }

    public static String getDecryptedDbFile() {
        return DECRYPTED_DB_FILE;
    }

    public static String getKey() {
        return KEY;
    }
}
