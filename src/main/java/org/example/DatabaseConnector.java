package org.example;

import org.slf4j.Logger;

import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.slf4j.LoggerFactory;
public class DatabaseConnector {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnector.class);
    private static final String ENCRYPTED_DB_FILE = "edb.sqlite";
    private static final String DECRYPTED_DB_FILE = "db.sqlite";
    // Removed the hardcoded key

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + DECRYPTED_DB_FILE;
            conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            log.info("Connected to " + DECRYPTED_DB_FILE + " with foreign key support enabled.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public static void decryptDatabase() {
        try {
            if (Files.exists(Paths.get(ENCRYPTED_DB_FILE))) {
                String key = getKey();
                FileDecryptor.decrypt(key, ENCRYPTED_DB_FILE, DECRYPTED_DB_FILE);
                log.info("Decrypted " + ENCRYPTED_DB_FILE);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void encryptDatabaseFile(Console console, Logger log) {
        try {
            String key = getKey();
            FileEncryptor.encrypt(key, getDecryptedDbFile(), getEncryptedDbFile());
            log.info("Encrypted " + getEncryptedDbFile());

            File decryptedFile = new File(getDecryptedDbFile());
            if (decryptedFile.delete()) {
                log.info("Deleted unencrypted file " + getDecryptedDbFile());
            } else {
                log.warn("Failed to delete decrypted file " + getDecryptedDbFile());
            }
        } catch (Exception e) {
            console.printf("Error encrypting the database file: %s\n", e.getMessage());
        }
    }

    public static String getEncryptedDbFile() {
        return ENCRYPTED_DB_FILE;
    }

    public static String getDecryptedDbFile() {
        return DECRYPTED_DB_FILE;
    }

    public static String getKey() {
        return System.getenv("DB_ENCRYPTION_KEY");
    }
}

