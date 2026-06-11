package com.sdms.model;

import com.sdms.utils.DatabaseService;
import java.security.MessageDigest;

public class User {
    public enum Role { ADMIN, STUDENT }

    private final String username, password, fullName, studentId;
    private final Role role;

    public User(String username, String password, Role role, String fullName, String studentId) {
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.fullName  = fullName;
        this.studentId = studentId;
    }

    public String getUsername()  { return username; }
    public String getPassword()  { return password; }
    public Role   getRole()      { return role; }
    public String getFullName()  { return fullName; }
    public String getStudentId() { return studentId; }

    /**
     * Xác thực qua SQL Server (thay thế hardcode cũ).
     * Hash mật khẩu bằng SHA-256 rồi so sánh với DB.
     */
    public static User authenticate(String username, String plainPassword) {
        String hashed = sha256(plainPassword);
        return DatabaseService.authenticate(username, hashed);
    }

    /** Hash mật khẩu thành SHA-256 hex (64 ký tự) */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}