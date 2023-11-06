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

    public User(String username, String email, boolean isAdmin, String hashedPassword) {
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.hashedPassword = hashedPassword;
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
        }
    }


    public void save() {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseConnector.connect();

            String sql = "UPDATE users SET hashedPassword = ? WHERE username = ?";

            statement = connection.prepareStatement(sql);

            statement.setString(1, this.hashedPassword);
            statement.setString(2, this.username);

            statement.executeUpdate();

            System.out.println("User password updated successfully.");

        } catch (SQLException e) {
            System.out.println("Error updating user password: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
    }
}
