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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class LoginFrame extends JFrame {

    private JTextField tfUser;
    private JPasswordField pfPass;
    private JCheckBox cbRemember;
    private JToggleButton btnAdmin, btnStudent;
    private JLabel lblError;
    private JButton btnRegister;

    // ── layer nền xanh + ảnh cover ─────────────────────────────────
    private BackgroundPanel bgPanel;
    // ── card trắng nổi lên ───────────────────────────────────────────
    private RoundedCardPanel cardPanel;
    private JLayeredPane layeredPane;

    public LoginFrame() {
        setTitle("SDMS — Student Dormitory Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setResizable(true);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));

        setLocationRelativeTo(null);
        initUI();

        setBackgroundImageResource("image2.jpg");
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        // ── Layer 0: nền xanh full khung (chứa ảnh cover + logo + text) ──
        bgPanel = new BackgroundPanel();
        layeredPane.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

        // ── Layer 1: card trắng nổi bên phải ─────────────────────────────
        cardPanel = new RoundedCardPanel();
        cardPanel.setLayout(new GridBagLayout());
        buildCardContent(cardPanel);
        layeredPane.add(cardPanel, JLayeredPane.PALETTE_LAYER);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutLayers();
            }
        });
    }

    /** Đặt lại vị trí + kích thước cho 2 layer mỗi khi cửa sổ resize. */
    private void layoutLayers() {
        int w = layeredPane.getWidth();
        int h = layeredPane.getHeight();

        bgPanel.setBounds(0, 0, w, h);

        // Card chiếm ~58% chiều rộng (tối thiểu 420, tối đa 620), cách lề các phía
        int margin = Math.max(30, (int) (h * 0.06));
        int cardW = Math.max(420, Math.min(620, (int) (w * 0.50)));
        int cardH = h - margin * 2;
        int cardX = w - cardW - margin;
        int cardY = margin;

        cardPanel.setBounds(cardX, cardY, cardW, cardH);

        bgPanel.revalidate();
        cardPanel.revalidate();
        layeredPane.repaint();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            layoutLayers();
    }

    // ════════════════════════════════════════════════════════════════
    // BACKGROUND PANEL — nền gradient xanh + ảnh cover (không bị vỡ)
    // ════════════════════════════════════════════════════════════════
    private class BackgroundPanel extends JPanel {

        private BufferedImage bgImage; // ảnh do người dùng chèn (có thể null)

        BackgroundPanel() {
            setLayout(null);
            setOpaque(true);
            buildDecor();
        }

        void setImage(BufferedImage img) {
            this.bgImage = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();

            // 1) Gradient nền xanh luôn vẽ trước (làm lớp lót)
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E40AF), w, h, new Color(0x3B82F6));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // 2) Nếu có ảnh chèn -> vẽ kiểu "cover": giữ tỉ lệ, fill kín, crop phần dư
            if (bgImage != null) {
                drawImageCover(g2, bgImage, 0, 0, w, h);
                // overlay xanh mờ để chữ/icon vẫn đọc được trên mọi ảnh
                g2.setColor(new Color(0x1E40AF));
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.fillRect(0, 0, w, h);
                g2.setComposite(old);
            }

            // 3) Decor: vòng tròn mờ
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fillOval(w - 160, -80, 260, 260);
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillOval(-80, h - 160, 280, 280);

            g2.dispose();
            // KHÔNG gọi super.paintComponent(g) ở đây — panel này tự vẽ kín toàn bộ nền,
            // gọi super sẽ làm Swing phủ màu nền mặc định (xám) đè lên hết.
        }

        /** Vẽ ảnh theo kiểu CSS background-size:cover — không méo, không vỡ. */
        private void drawImageCover(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
            double imgRatio = (double) img.getWidth() / img.getHeight();
            double boxRatio = (double) w / h;

            int sw, sh, sx, sy;
            if (imgRatio > boxRatio) {
                // ảnh "rộng" hơn khung -> fit theo chiều cao, crop 2 bên
                sh = img.getHeight();
                sw = (int) (sh * boxRatio);
                sx = (img.getWidth() - sw) / 2;
                sy = 0;
            } else {
                // ảnh "cao" hơn khung -> fit theo chiều rộng, crop trên/dưới
                sw = img.getWidth();
                sh = (int) (sw / boxRatio);
                sx = 0;
                sy = (img.getHeight() - sh) / 2;
            }
            g2.drawImage(img, x, y, x + w, y + h, sx, sy, sx + sw, sy + sh, null);
        }

        private void buildDecor() {
            JPanel logoBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            logoBox.setOpaque(false);
            logoBox.setLayout(new BorderLayout());
            logoBox.setBounds(80, 90, 72, 72);
            logoBox.add(new JLabel(buildingIcon(), SwingConstants.CENTER), BorderLayout.CENTER);

            // ── Tiêu đề + phụ đề dùng ShadowLabel để luôn nổi rõ trên mọi nền ảnh ──
            JLabel lblTitle = new ShadowLabel("<html><b>Student Dormitory<br>Management System</b></html>");
            lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setBounds(75, 180, 420, 70);

            JLabel lblSub = new ShadowLabel(
                    "<html>Quản lý ký túc xá sinh viên thông minh<br>hiện đại · nhanh chóng · tiện lợi</html>");
            lblSub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            lblSub.setForeground(new Color(255, 255, 255, 230));
            lblSub.setBounds(75, 255, 420, 50);


            JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            dots.setOpaque(false);
            for (int i = 0; i < 3; i++) {
                final int idx = i;
                JLabel d = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(idx == 0 ? Color.WHITE : new Color(255, 255, 255, 120));
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
            dots.setBounds(75, 555, 100, 16);

            add(logoBox);
            add(lblTitle);
            add(lblSub);
            add(dots);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // CARD TRẮNG (bo góc, đổ bóng) chứa form đăng nhập
    // ════════════════════════════════════════════════════════════════
    private static class RoundedCardPanel extends JPanel {
        RoundedCardPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(40, 44, 40, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // đổ bóng nhẹ
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 8, getHeight() - 8, 22, 22));
            g2.setColor(UITheme.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 8, getHeight() - 12, 22, 22));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Nội dung form (giữ nguyên logic cũ, chỉ đổi panel chứa) ─────────
    private void buildCardContent(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 8, 0);

        JLabel welcome = new JLabel("Chào mừng trở lại 👋");
        welcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(welcome, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        JLabel hint = new JLabel("Đăng nhập để tiếp tục quản lý hệ thống");
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(hint, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 20, 4);
        btnAdmin = roleToggle("🛡  Quản trị viên");
        btnAdmin.setPreferredSize(new Dimension(150, 40));
        panel.add(btnAdmin, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 20, 0);
        btnStudent = roleToggle("🎓  Sinh viên");
        btnStudent.setPreferredSize(new Dimension(150, 40));
        panel.add(btnStudent, gbc);

        ButtonGroup bg = new ButtonGroup();
        btnAdmin.setSelected(true);
        bg.add(btnAdmin);
        bg.add(btnStudent);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(UITheme.formLabel("Tên đăng nhập"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        tfUser = UITheme.textField("admin hoặc sv001...");
        tfUser.setPreferredSize(new Dimension(300, 42));
        panel.add(tfUser, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(UITheme.formLabel("Mật khẩu"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        pfPass = UITheme.passwordField();
        pfPass.setPreferredSize(new Dimension(300, 42));
        panel.add(pfPass, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JPanel rowRemember = new JPanel(new BorderLayout());
        rowRemember.setOpaque(false);

        cbRemember = new JCheckBox("Hiển thị mật khẩu");
        cbRemember.setFont(UITheme.FONT_SMALL);
        cbRemember.setForeground(UITheme.TEXT_SECONDARY);
        cbRemember.setOpaque(false);
        cbRemember.setFocusPainted(false);
        cbRemember.setSelected(false);
        cbRemember.addItemListener(e -> pfPass.setEchoChar(
                e.getStateChange() == ItemEvent.SELECTED ? (char) 0 : '●'));
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

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setPreferredSize(new Dimension(300, 16));
        panel.add(lblError, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JButton btnLogin = UITheme.primaryBtn("  Đăng nhập  ");
        btnLogin.setPreferredSize(new Dimension(300, 44));
        btnLogin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnLogin.addActionListener(e -> doLogin());
        panel.add(btnLogin, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel registerCard = new JPanel(new CardLayout());
        registerCard.setOpaque(false);
        registerCard.setPreferredSize(new Dimension(300, 38));

        JPanel emptyCard = new JPanel();
        emptyCard.setOpaque(false);

        btnRegister = UITheme.outlineBtn("  Đăng ký tài khoản  ");
        btnRegister.setPreferredSize(new Dimension(300, 38));
        btnRegister.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btnRegister.addActionListener(e -> doRegister());

        registerCard.add(emptyCard, "HIDDEN");
        registerCard.add(btnRegister, "VISIBLE");
        ((CardLayout) registerCard.getLayout()).show(registerCard, "HIDDEN");
        panel.add(registerCard, gbc);

        btnStudent.addItemListener(e -> {
            boolean isStudent = (e.getStateChange() == ItemEvent.SELECTED);
            ((CardLayout) registerCard.getLayout()).show(registerCard, isStudent ? "VISIBLE" : "HIDDEN");
            lForgot.setVisible(isStudent);

            if (isStudent) {
                // Mới chuyển sang Sinh viên -> xóa value cũ của Admin
                tfUser.setText("");
                pfPass.setText("");
                lblError.setText(" ");
                cbRemember.setSelected(false);
            }
        });

        btnAdmin.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // Mới chuyển sang Quản trị viên -> xóa value cũ của Sinh viên
                tfUser.setText("");
                pfPass.setText("");
                lblError.setText(" ");
                cbRemember.setSelected(false);
            }
        });

        pfPass.addActionListener(e -> doLogin());
        tfUser.addActionListener(e -> pfPass.requestFocus());
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

    // ── Hàm public để chèn ảnh nền (gọi từ ngoài, ví dụ qua JFileChooser) ──
    public void setBackgroundImageFile(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            bgPanel.setImage(img);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearBackgroundImage() {
        bgPanel.setImage(null);
    }

    public void setBackgroundImageResource(String resourceName) {
        try {
            java.net.URL url = getClass().getResource(resourceName);
            if (url == null) {
                System.err.println("Không tìm thấy ảnh trong resource: " + resourceName);
                return;
            }
            BufferedImage img = ImageIO.read(url);
            if (img == null) {
                System.err.println("ImageIO không đọc được file (file hỏng hoặc sai định dạng thật)");
                return;
            }
            bgPanel.setImage(img);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
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
        if (user == null || !user.getPassword().equals(p)) {
            lblError.setText("✗ Sai tên đăng nhập hoặc mật khẩu. Thử lại.");
            return;
        }

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

        JFrame currentLoginFrame = this;

        com.sdms.ui.admin.AccountRegister registerPanel = new com.sdms.ui.admin.AccountRegister(() -> {
            registerWindow.dispose();
            currentLoginFrame.setVisible(true);
            currentLoginFrame.toFront();
        });

        registerWindow.add(registerPanel);
        registerWindow.setVisible(true);
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
        int nh = (int) (400 * 990.0 / 1320.0);

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

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        footer.setBackground(UITheme.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E7EB)));

        JButton btnBack = UITheme.outlineBtn("← Quay lại");
        btnBack.setPreferredSize(new Dimension(140, 36));
        btnBack.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btnBack.addActionListener(e -> dialog.dispose());
        footer.add(btnBack);

        main.add(imgLabel, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setSize(nw + 40, nh + 80);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

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
                g2.setColor(wA);
                g2.fillRoundRect(x + 30, y + 30, 230, 110, 6, 6);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(x + 30, y + 30, 230, 110, 6, 6);
                g2.setColor(wB);
                g2.fillRoundRect(x + 60, y + 12, 170, 22, 4, 4);
                for (int i = 0; i < 4; i++) {
                    g2.setColor(win);
                    g2.fillRoundRect(x + 46 + i * 54, y + 46, 28, 20, 3, 3);
                }
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(x + 46 + i * 54, y + 76, 28, 20, 3, 3);
                }
                g2.setColor(new Color(255, 255, 255, 90));
                g2.fillRoundRect(x + 126, y + 108, 40, 32, 4, 4);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(x + 0, y + 140, 300, 8, 4, 4);
                g2.setColor(new Color(255, 220, 50, 160));
                g2.fillOval(x + 240, y + 5, 28, 28);
                g2.dispose();
            }
        };
    }

    // ── Label có viền/bóng đen để luôn nổi rõ trên mọi nền ảnh ──────────
    private static class ShadowLabel extends JLabel {
        ShadowLabel(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fg = getForeground();

            setForeground(new Color(0, 0, 0, 170));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    g2.translate(dx, dy);
                    super.paintComponent(g2);
                    g2.translate(-dx, -dy);
                }
            }

            setForeground(fg);
            super.paintComponent(g2);

            g2.dispose();
        }
    }
}