package org.example;

import org.slf4j.Logger;

import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnector {

    private static final String ENCRYPTED_DB_FILE = "edb.sqlite";
    private static final String DECRYPTED_DB_FILE = "db.sqlite";
    private static final String KEY = "totallygoodkeyss";

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + DECRYPTED_DB_FILE;
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void decryptDatabase() {
        try {
            if (Files.exists(Paths.get(ENCRYPTED_DB_FILE))) {
                FileDecryptor.decrypt(KEY, ENCRYPTED_DB_FILE, DECRYPTED_DB_FILE);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void encryptDatabaseFile(Console console, Logger log) {
        try {
            FileEncryptor.encrypt(DatabaseConnector.getKey(), DatabaseConnector.getDecryptedDbFile(), DatabaseConnector.getEncryptedDbFile());
            log.info("Encryption successful!");

            File decryptedFile = new File(DatabaseConnector.getDecryptedDbFile());
            if (decryptedFile.delete()) {
                log.info("Decrypted database file deleted successfully.");
            } else {
                log.warn("Failed to delete decrypted database file.");
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
        return KEY;
    }
}
