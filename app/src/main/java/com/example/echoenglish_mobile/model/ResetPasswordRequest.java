package com.example.echoenglish_mobile.model;

public class ResetPasswordRequest {
    private String email;
    private String otpCode;
    private String newPassword;

    public ResetPasswordRequest(String email, String otpCode, String newPassword) {
        this.email = email;
        this.otpCode = otpCode;
        this.newPassword = newPassword;
    }

    // Getters and Setters (có thể bỏ qua nếu chỉ dùng để gửi đi)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}