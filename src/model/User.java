package model;

import utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String userType;
    
    public User(String username, String password, String name, String phone, String email, String userType) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.userType = userType;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    // 验证用户
    public static User validateUser(String username, String password) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    // 检查用户名是否存在
    public static boolean isUsernameExists(String username) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    // 保存用户信息
    public boolean save() {
        try {
            List<User> users = loadUsers();
            users.add(this);
            return saveUsers(users);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 加载所有用户
    private static List<User> loadUsers() {
        try {
            List<String> lines = FileUtil.readLines("users.txt");
            List<User> users = new ArrayList<>();
            
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    users.add(new User(
                        parts[0], // username
                        parts[1], // password
                        parts[2], // name
                        parts[3], // phone
                        parts[4], // email
                        parts[5]  // userType
                    ));
                }
            }
            
            return users;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    // 保存所有用户
    private static boolean saveUsers(List<User> users) {
        try {
            List<String> lines = new ArrayList<>();
            for (User user : users) {
                lines.add(String.format("%s,%s,%s,%s,%s,%s",
                    user.getUsername(),
                    user.getPassword(),
                    user.getName(),
                    user.getPhone(),
                    user.getEmail(),
                    user.getUserType()
                ));
            }
            FileUtil.writeLines("users.txt", lines);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 