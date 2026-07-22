package com.sdms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {


    private static final String HOST = "aws-0-ap-southeast-1.pooler.supabase.com";
    private static final String PORT = "5432";
    private static final String DATABASE = "postgres";
    private static final String USERNAME = "postgres.jwyhmlpjskijhlkwwyde";
    private static final String PASSWORD = "3UEUf9WWhEbkkJBY";

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT
            + "/" + DATABASE
            + "?sslmode=require"; 

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Khong tim thay JDBC Driver", e);
        }
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Đã đóng kết nối PostgreSQL.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean testConnection() {
        try {
            System.out.println("URL = " + URL);
            getConnection();
            System.out.println("✅ Kết nối PostgreSQL thành công!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối PostgreSQL: " + e.getMessage());
            return false;

        }
        
    }
}