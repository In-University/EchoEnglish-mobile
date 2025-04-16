package com.example.echoenglish_mobile.model;

import java.io.Serializable;

public class User implements Serializable {
    // Không cần id khi gửi request đăng ký
    private String name;
    private String email;
    private String password;
    private String avatar; // Sẽ là null khi đăng ký
    private Boolean active; // Sẽ là false khi đăng ký

    // Constructors
    public User() {}

    public User(String name, String email, String password, Boolean active) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = active;
        this.avatar = null; // Mặc định avatar là null khi tạo user mới
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}