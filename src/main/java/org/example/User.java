package org.example;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User {
    private int id;
    private String username;
    private String email;
    private boolean isAdmin;
    private String hashedPassword;

    private int firstLogin = 1;

    public User(String username, String email, boolean isAdmin, String hashedPassword, int firstLogin) {
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.hashedPassword = hashedPassword;
        this.firstLogin = firstLogin;
    }



    public String getHashedPassword(){
        return hashedPassword;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(int firstLogin) {
        this.firstLogin = firstLogin;
    }


    public void setPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void refresh() {
        User refreshedUser = DatabaseUtils.findUserByEmailOrUsername(this.username);
        if (refreshedUser != null) {
            this.id = refreshedUser.getId();
            this.email = refreshedUser.getEmail();
            this.isAdmin = refreshedUser.isAdmin();
            this.hashedPassword = refreshedUser.getHashedPassword();
            this.firstLogin = refreshedUser.isFirstLogin();
        }
    }


    public void save() {
        String sql = "UPDATE users SET hashedPassword = ?";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, this.hashedPassword);
            statement.setInt(2, this.firstLogin);
            statement.setString(3, this.username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error updating user password: " + e.getMessage());
        }finally{
            DatabaseConnector.encryptDatabaseFile();
        }
    }

}
