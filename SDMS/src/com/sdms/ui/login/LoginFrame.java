package com.sdms.ui.login;

import com.sdms.utils.DatabaseService;
import java.security.MessageDigest;

import com.sdms.model.User;
import com.sdms.ui.admin.AdminFrame;
import com.sdms.ui.user.UserFrame;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginFrame extends JFrame {

    private JTextField tfUser;
    private JPasswordField pfPass;
    private JCheckBox cbRemember;
    private JToggleButton btnAdmin, btnStudent;
    private JLabel lblError;
    private JButton btnRegister;

    public LoginFrame() {
        setTitle("SDMS — Student Dormitory Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ── KHẮC PHỤC CHÍNH ĐỂ THAY ĐỔI KÍCH THƯỚC THOẢI MÁI ────────────────
        setResizable(true); // 1. Cho phép phóng to toàn màn hình (bật nút ô vuông)
        setSize(920, 620); // 2. Tăng nhẹ chiều cao từ 580 lên 620 để vừa vặn khi nút Đăng ký hiện ra
        setMinimumSize(new Dimension(880, 600)); // 3. Giới hạn kích thước nhỏ nhất tránh người dùng thu nhỏ quá mức làm
                                                 // vỡ form

        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(UITheme.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        root.add(buildLeft(), BorderLayout.WEST);
        root.add(buildRight(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── LEFT PANEL ────────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E40AF),
                        getWidth(), getHeight(), new Color(0x3B82F6));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // decorative circles
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(getWidth() - 120, -60, 200, 200);
                g2.fillOval(-60, getHeight() - 120, 220, 220);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(getWidth() / 2 - 80, getHeight() / 2 - 80, 160, 160);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(380, 580));

        // Logo box
        JPanel logoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBox.setOpaque(false);
        logoBox.setLayout(new BorderLayout());
        logoBox.setBounds(154, 80, 72, 72);
        JLabel logoIcon = new JLabel(buildingIcon(), SwingConstants.CENTER);
        logoBox.add(logoIcon, BorderLayout.CENTER);

        JLabel lblTitle = new JLabel("<html><center>Student Dormitory<br>Management System</center></html>",
                SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(30, 168, 320, 56);

        JLabel lblSub = new JLabel(
                "<html><center>Quản lý ký túc xá sinh viên thông minh<br>hiện đại · nhanh chóng · tiện lợi</center></html>",
                SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(255, 255, 255, 180));
        lblSub.setBounds(30, 232, 320, 50);

        // Building illustration
        JLabel building = new JLabel(buildingIllustration());
        building.setBounds(40, 295, 300, 160);

        // Dots
        JPanel dots = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)) {
            {
                setOpaque(false);
            }
        };
        for (int i = 0; i < 3; i++) {
            final int index = i;
            JLabel d = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(index == 1 ? Color.WHITE : new Color(255, 255, 255, 120));
                    g2.fillOval(0, 0, 8, 8);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(8, 8);
                }
            };
            dots.add(d);
        }
        dots.setBounds(140, 465, 100, 16);

        panel.add(logoBox);
        panel.add(lblTitle);
        panel.add(lblSub);
        panel.add(building);
        panel.add(dots);
        return panel;
    }

    // ── RIGHT PANEL ───────────────────────────────────────────────
    // ── RIGHT PANEL (ĐÃ ĐỔI SANG GRIDBAGLAYOUT ĐỂ TỰ CO DÃN THEO MÀN HÌNH) ──
    private JPanel buildRight() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40)); // Tạo khoảng đệm cách lề xung quanh

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Riêng các tiêu đề sẽ chiếm hết 2 cột chiều ngang
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho phép dãn hết chiều ngang màn hình
        gbc.weightx = 1.0; // Tỷ lệ dãn nở theo chiều ngang là 100%
        gbc.insets = new Insets(0, 0, 8, 0); // Khoảng cách dưới

        // Welcome
        JLabel welcome = new JLabel("Chào mừng trở lại 👋");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Tăng size chữ một chút cho hợp màn hình to
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(welcome, gbc);

        // Hint
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel hint = new JLabel("Đăng nhập để tiếp tục quản lý hệ thống");
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(hint, gbc);

        // Role toggle (Chia làm 2 cột độc lập để tự dãn rộng đều nhau)
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5; // Mỗi nút chiếm 50% độ rộng vùng chứa
        gbc.insets = new Insets(0, 0, 20, 4); // Nút trái cách nút phải 4px
        btnAdmin = roleToggle("🛡  Quản trị viên");
        btnAdmin.setPreferredSize(new Dimension(150, 38));
        panel.add(btnAdmin, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 20, 0); // Nút phải cách nút trái 4px
        btnStudent = roleToggle("👤  Sinh viên");
        btnStudent.setPreferredSize(new Dimension(150, 38));
        panel.add(btnStudent, gbc);

        ButtonGroup bg = new ButtonGroup();
        btnAdmin.setSelected(true);
        bg.add(btnAdmin);
        bg.add(btnStudent);

        // Cấu hình lại gbc cho các ô nhập liệu chiếm trọn chiều ngang
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        // Username Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lUser = UITheme.formLabel("Tên đăng nhập");
        panel.add(lUser, gbc);

        // Username TextField
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        tfUser = UITheme.textField("admin hoặc sv001...");
        tfUser.setPreferredSize(new Dimension(300, 40)); // Đặt chiều cao lý tưởng là 40px, chiều rộng tự dãn
        panel.add(tfUser, gbc);

        // Password Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lPass = UITheme.formLabel("Mật khẩu");
        panel.add(lPass, gbc);

        // Password Field
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        pfPass = UITheme.passwordField();
        pfPass.setPreferredSize(new Dimension(300, 40));
        panel.add(pfPass, gbc);

        // Khối Remember + Forgot (Sử dụng Panel phụ dùng BorderLayout để tự dãn sang 2
        // đầu biên)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JPanel rowRemember = new JPanel(new BorderLayout());
        rowRemember.setOpaque(false);

        cbRemember = new JCheckBox("Hiển thị mật khẩu");
        cbRemember.setFont(UITheme.FONT_SMALL);
        cbRemember.setForeground(UITheme.TEXT_SECONDARY);
        cbRemember.setBackground(UITheme.WHITE);
        cbRemember.setFocusPainted(false); // bỏ nét đứt focus
        cbRemember.setSelected(false);
        cbRemember.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pfPass.setEchoChar((char) 0); // hiện mật khẩu
            } else {
                pfPass.setEchoChar('●'); // ẩn mật khẩu
            }
        });
        rowRemember.add(cbRemember, BorderLayout.WEST);

        JLabel lForgot = new JLabel("Quên mật khẩu?");
        lForgot.setFont(UITheme.FONT_SMALL);
        lForgot.setForeground(UITheme.PRIMARY);
        lForgot.setVisible(false);
        lForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotImageDialog();
            }
        });
        rowRemember.add(lForgot, BorderLayout.EAST);
        panel.add(rowRemember, gbc);

        // Error label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        lblError = new JLabel(" "); // dùng space thay vì "" để luôn giữ chiều cao
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setPreferredSize(new Dimension(300, 16)); // cố định chiều cao
        lblError.setMinimumSize(new Dimension(300, 16)); // không cho co lại
        panel.add(lblError, gbc);

        // Login button
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JButton btnLogin = UITheme.primaryBtn("  Đăng nhập  ");
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.addActionListener(e -> doLogin());
        panel.add(btnLogin, gbc);

        // Register button
        // Register button (dùng CardLayout để swap mà không làm layout thay đổi)
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);

        JPanel registerCard = new JPanel(new CardLayout());
        registerCard.setOpaque(false);
        registerCard.setPreferredSize(new Dimension(300, 38));
        registerCard.setMinimumSize(new Dimension(300, 38));
        registerCard.setMaximumSize(new Dimension(300, 38));

        // Card 1: nút ẩn (placeholder trong suốt)
        JPanel emptyCard = new JPanel();
        emptyCard.setOpaque(false);

        // Card 2: nút đăng ký thật
        btnRegister = UITheme.outlineBtn("  Đăng ký tài khoản  ");
        btnRegister.setPreferredSize(new Dimension(300, 38));
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRegister.addActionListener(e -> doRegister());

        registerCard.add(emptyCard, "HIDDEN");
        registerCard.add(btnRegister, "VISIBLE");
        ((CardLayout) registerCard.getLayout()).show(registerCard, "HIDDEN");
        panel.add(registerCard, gbc);

        btnStudent.addItemListener(e -> {
            boolean isStudent = (e.getStateChange() == ItemEvent.SELECTED);
            ((CardLayout) registerCard.getLayout()).show(registerCard, isStudent ? "VISIBLE" : "HIDDEN");
            lForgot.setVisible(isStudent);
        });

        // Enter key listeners
        pfPass.addActionListener(e -> doLogin());
        tfUser.addActionListener(e -> pfPass.requestFocus());

        return panel;
    }

    private JToggleButton roleToggle(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(UITheme.PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
                } else {
                    g2.setColor(UITheme.BG_SECONDARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(UITheme.BORDER);
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    private String sha256(String input) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    } catch (Exception e) {
        return "";
    }
}

    private void doLogin() {
       String u = tfUser.getText().trim();
       String p = sha256(new String(pfPass.getPassword()).trim());

        if (u.isEmpty() || p.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập đầy đủ thông tin đăng nhập.");
            return;
        }

        User user = DatabaseService.getUserByUsername(u);
        System.out.println("Username: " + u);
        System.out.println("User object: " + user);
        if (user != null) {
        System.out.println("DB Password: " + user.getPassword());
        System.out.println("Input Password: " + p);
}
        if (user == null || !user.getPassword().equals(p)) {
         lblError.setText("✗ Sai tên đăng nhập hoặc mật khẩu. Thử lại.");
         return;
}

        // Kiểm tra role đã chọn có khớp với role của user không
        boolean isAdminSelected = btnAdmin.isSelected();
        boolean isStudentSelected = btnStudent.isSelected();

        if (isAdminSelected && user.getRole() != User.Role.ADMIN) {
            lblError.setText("✗ Tài khoản này không phải là Quản trị viên. Vui lòng chọn đúng vai trò.");
            return;
        }

        if (isStudentSelected && user.getRole() != User.Role.STUDENT) {
            lblError.setText("✗ Tài khoản này không phải là Sinh viên. Vui lòng chọn đúng vai trò.");
            return;
        }

        // Nếu không chọn role nào (trường hợp hiếm), vẫn cho phép đăng nhập theo role
        // thực tế
        dispose();
        if (user.getRole() == User.Role.ADMIN) {
            new AdminFrame(user).setVisible(true);
        } else {
            new UserFrame(user).setVisible(true);
        }
    }

    private void doRegister() {
        JFrame registerWindow = new JFrame("Đăng ký tài khoản");
        registerWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerWindow.setSize(1100, 700);
        registerWindow.setLocationRelativeTo(this);
        registerWindow.setResizable(true);

        // Lưu tham chiếu đến LoginFrame hiện tại
        JFrame currentLoginFrame = this;

        com.sdms.ui.admin.AccountRegister registerPanel = new com.sdms.ui.admin.AccountRegister(() -> {
            registerWindow.dispose(); // Đóng cửa sổ đăng ký
            currentLoginFrame.setVisible(true); // Hiện lại login frame cũ
            currentLoginFrame.toFront(); // Đưa lên trước
        });

        registerWindow.add(registerPanel);
        registerWindow.setVisible(true);

        // Ẩn login frame khi đang đăng ký
        setVisible(false);
    }

    private void showForgotImageDialog() {
        JDialog dialog = new JDialog(this, "Quên mật khẩu", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(UITheme.WHITE);

        JLabel imgLabel = new JLabel("", SwingConstants.CENTER);
        imgLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int nw = 400;
        int nh = (int) (400 * 990.0 / 1320.0); // ≈ 300

        java.net.URL res = getClass().getResource("image.png");
        if (res != null) {
            ImageIcon raw = new ImageIcon(res);
            Image scaled = raw.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            MediaTracker tracker = new MediaTracker(dialog);
            tracker.addImage(scaled, 0);
            try {
                tracker.waitForAll();
            } catch (InterruptedException ignored) {
            }

            imgLabel.setIcon(new ImageIcon(scaled));
            imgLabel.setPreferredSize(new Dimension(nw, nh));
        } else {
            imgLabel.setText("⚠ Không tìm thấy ảnh hướng dẫn.");
            imgLabel.setPreferredSize(new Dimension(nw, nh));
        }

        // ── FOOTER ──────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        footer.setBackground(UITheme.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E7EB)));

        JButton btnBack = UITheme.outlineBtn("← Quay lại");
        btnBack.setPreferredSize(new Dimension(140, 36));
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.addActionListener(e -> dialog.dispose());
        footer.add(btnBack);

        main.add(imgLabel, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setSize(nw + 40, nh + 80); // 440 x 398
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── Icons ─────────────────────────────────────────────────────
    private Icon buildingIcon() {
        return new Icon() {
            public int getIconWidth() {
                return 38;
            }

            public int getIconHeight() {
                return 38;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRect(x + 4, y + 16, 30, 18);
                g2.setColor(new Color(255, 255, 255, 170));
                g2.fillRect(x + 9, y + 8, 20, 10);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillRect(x + 14, y + 2, 10, 8);
                g2.setColor(new Color(0x2563EB));
                int[] wx = { x + 6, x + 10, x + 14, x + 20, x + 25, x + 29 };
                for (int wx_ : wx)
                    g2.fillRect(wx_, y + 20, 4, 5);
                g2.setColor(new Color(0x1E3A8A));
                g2.fillRect(x + 16, y + 28, 6, 8);
                g2.dispose();
            }
        };
    }

    private Icon buildingIllustration() {
        return new Icon() {
            public int getIconWidth() {
                return 300;
            }

            public int getIconHeight() {
                return 160;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color wA = new Color(255, 255, 255, 30);
                Color wB = new Color(255, 255, 255, 50);
                Color win = new Color(255, 255, 255, 70);
                // main building
                g2.setColor(wA);
                g2.fillRoundRect(x + 30, y + 30, 230, 110, 6, 6);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(x + 30, y + 30, 230, 110, 6, 6);
                // roof
                g2.setColor(wB);
                g2.fillRoundRect(x + 60, y + 12, 170, 22, 4, 4);
                // windows row 1
                for (int i = 0; i < 4; i++) {
                    g2.setColor(win);
                    g2.fillRoundRect(x + 46 + i * 54, y + 46, 28, 20, 3, 3);
                }
                // windows row 2
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(x + 46 + i * 54, y + 76, 28, 20, 3, 3);
                }
                // doors
                g2.setColor(new Color(255, 255, 255, 90));
                g2.fillRoundRect(x + 126, y + 108, 40, 32, 4, 4);
                // ground
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(x + 0, y + 140, 300, 8, 4, 4);
                // sun
                g2.setColor(new Color(255, 220, 50, 160));
                g2.fillOval(x + 240, y + 5, 28, 28);
                g2.dispose();
            }
        };
    }
}