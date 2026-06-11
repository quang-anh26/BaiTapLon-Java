package com.sdms;

import com.sdms.ui.login.LoginFrame;
import com.sdms.utils.DatabaseConnection;
import com.sdms.utils.UITheme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // ── Kiểm tra kết nối DB trước khi mở UI ──────────────────
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                "❌ Không thể kết nối SQL Server!\n\n"
              + "Vui lòng kiểm tra:\n"
              + "  • SQL Server đang chạy\n"
              + "  • Tên server / database đúng trong DatabaseConnection.java\n"
              + "  • Username / password đúng (sdms / 123456)\n"
              + "  • SQL Server Authentication đã được bật\n"
              + "  • TCP/IP đã bật trong SQL Server Configuration Manager",
                "Lỗi kết nối Database",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── Đóng kết nối khi thoát ───────────────────────────────
        Runtime.getRuntime().addShutdownHook(
            new Thread(DatabaseConnection::closeConnection)
        );

        // ── Khởi động giao diện ───────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalTheme();
            new LoginFrame().setVisible(true);
        });
    }
}