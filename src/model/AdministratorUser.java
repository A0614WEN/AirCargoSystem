package model;

public class AdministratorUser {
    private String username;
    private String password;
    private String email;

    // Default constructor
    public AdministratorUser() {
    }

    // Constructor for creating a user object
    public AdministratorUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Override toString for easy file writing
    @Override
    public String toString() {
        // Format: username,password,email
        return username + "," + password + "," + email;
    }
}
