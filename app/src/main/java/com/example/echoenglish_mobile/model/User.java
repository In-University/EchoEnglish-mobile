package com.example.echoenglish_mobile.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    // Không cần id khi gửi request đăng ký
    private Long id;
    private String name;
    private String email;
    private String password;
    private String avatar;
    private Boolean active; // Sẽ là false khi đăng ký

    public User() {}

    public User(String name, String email, String password, Boolean active) { // Đky k cần id
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = active;
        this.avatar = "https://cdn.kona-blue.com/upload/kona-blue_com/post/images/2024/09/18/457/avatar-mac-dinh-11.jpg"; // Mặc định avatar khi tạo user mới
    }

    public User(Long id, String name, String email, String password, String avatar, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}