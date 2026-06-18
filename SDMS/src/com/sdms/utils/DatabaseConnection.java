 package com.sdms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối đơn lẻ (Singleton) tới SQL Server.
 * Sửa 4 hằng số bên dưới cho khớp với máy của bạn.
 */
public class DatabaseConnection {

    // ╔══════════════════════════════════════════════════════════╗
    // ║  SỬA 4 DÒNG NÀY CHO KHỚP VỚI MÁY BẠN                     ║
    // ╚══════════════════════════════════════════════════════════╝
    private static final String SERVER   = "DESKTOP-V2852IS\\SQLEXPRESS";        // hoặc ".\SQLEXPRESS" / "127.0.0.1"
    private static final String DATABASE = "quanly-ktx";      // tên DB trong ktx.sql
    private static final String USERNAME = "sdms";            // login vừa tạo trong ktx.sql
    private static final String PASSWORD = "123456";          // mật khẩu trong ktx.sql

    // URL kết nối — trustServerCertificate=true tránh lỗi SSL với SQL Server cục bộ
    private static final String URL =
        "jdbc:sqlserver://" + SERVER
        + ";databaseName=" + DATABASE
        + ";encrypt=true"
        + ";trustServerCertificate=true"
        + ";sendStringParametersAsUnicode=true";  // quan trọng cho tiếng Việt

    private static Connection connection = null;

    // ── Lấy kết nối (tái sử dụng nếu còn sống) ─────────────────
   public static Connection getConnection() throws SQLException {
    try {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (ClassNotFoundException e) {
        throw new SQLException("Khong tim thay JDBC Driver", e);
    }

    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    return connection;
}

    // ── Đóng kết nối khi thoát app ──────────────────────────────
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Đã đóng kết nối SQL Server.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Kiểm tra kết nối — gọi khi khởi động ───────────────────

   public static boolean testConnection() {

    try {
        System.out.println("SERVER = " + SERVER);
        System.out.println("URL = " + URL);

        getConnection();

        System.out.println("✅ Kết nối SQL Server thành công!");
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}