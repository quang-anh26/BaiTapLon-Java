package com.sdms.ui.user;

import com.sdms.model.User;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;

public class ChangePasswordPanel extends JPanel {

    private final User currentUser;

    private JPasswordField pfCurrent, pfNew, pfConfirm;
    private JLabel lblStrength;
    private JPanel strengthBarFill;
    private JCheckBox chkShow;
    private JLabel[] tipIcons;

    // Màu chủ đạo
    private static final Color PRIMARY = new Color(0x4F46E5);
    private static final Color PRIMARY_DARK = new Color(0x3730A3);
    private static final Color BG_PAGE = new Color(0xF3F4F8);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BG_FIELD = new Color(0xF9FAFB);
    private static final Color BG_TIPS = new Color(0xF8F9FF);
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color BORDER_TIPS = new Color(0xE0E7FF);
    private static final Color TEXT_PRIMARY = new Color(0x111827);
    private static final Color TEXT_MUTED = new Color(0x6B7280);
    private static final Color TEXT_LABEL = new Color(0x6B7280);
    private static final Color TIP_INACTIVE = new Color(0x9CA3AF);
    private static final Color TIP_ACTIVE = new Color(0x16A34A);

    private static final Color[] STRENGTH_COLORS = {
            new Color(0xEF4444), new Color(0xF97316),
            new Color(0xEAB308), new Color(0x22C55E), new Color(0x16A34A)
    };
    private static final String[] STRENGTH_LABELS = {
            "Quá yếu", "Yếu", "Trung bình", "Mạnh", "Rất mạnh"
    };
    private static final float[] STRENGTH_WIDTHS = { 0.2f, 0.4f, 0.6f, 0.8f, 1.0f };

    public ChangePasswordPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_PAGE);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)));

        JLabel title = new JLabel("🔒  Đổi mật khẩu");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Đổi mật khẩu");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub, BorderLayout.SOUTH);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Content ───────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_PAGE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(32, 0, 32, 0);
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel card = buildCard();
        card.setPreferredSize(new Dimension(440, card.getPreferredSize().height));
        outer.add(card, gbc);
        return outer;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(32, 32, 28, 32));

        // ── Icon vòng tròn gradient ───────────────────────────────
        JLabel lockIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x3B82F6), getWidth(), getHeight(),
                        new Color(0x6366F1));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lockIcon.setText("🔐");
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lockIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lockIcon.setPreferredSize(new Dimension(64, 64));
        lockIcon.setMaximumSize(new Dimension(64, 64));
        lockIcon.setMinimumSize(new Dimension(64, 64));
        lockIcon.setOpaque(false);

        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconWrapper.setOpaque(false);
        iconWrapper.add(lockIcon);
        iconWrapper.setAlignmentX(CENTER_ALIGNMENT);

        JLabel cardTitle = new JLabel("Thay đổi mật khẩu", SwingConstants.CENTER);
        cardTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        cardTitle.setForeground(TEXT_PRIMARY);
        cardTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel cardSub = new JLabel("Tài khoản: " + currentUser.getUsername(), SwingConstants.CENTER);
        cardSub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        cardSub.setForeground(TEXT_MUTED);
        cardSub.setAlignmentX(CENTER_ALIGNMENT);

        // ── Fields ────────────────────────────────────────────────
        pfCurrent = styledPasswordField();
        pfNew = styledPasswordField();
        pfConfirm = styledPasswordField();

        pfNew.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }
        });

        JPanel rowCurrent = fieldRow("MẬT KHẨU HIỆN TẠI *", pfCurrent);
        JPanel rowNew = fieldRow("MẬT KHẨU MỚI *", pfNew);
        JPanel strengthRow = buildStrengthRow();
        JPanel rowConfirm = fieldRow("XÁC NHẬN MẬT KHẨU MỚI *", pfConfirm);

        // ── Checkbox ──────────────────────────────────────────────
        chkShow = new JCheckBox("Hiện mật khẩu");
        chkShow.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        chkShow.setForeground(TEXT_MUTED);
        chkShow.setOpaque(false);
        chkShow.setFocusPainted(false);
        chkShow.setAlignmentX(LEFT_ALIGNMENT);
        chkShow.addActionListener(e -> toggleShow(chkShow.isSelected()));

        // ── Nút chính (gradient) ──────────────────────────────────
        JButton btnChange = gradientBtn("🔒  Xác nhận đổi mật khẩu");
        btnChange.setAlignmentX(CENTER_ALIGNMENT);
        btnChange.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnChange.addActionListener(e -> changePassword());

        // ── Nút phụ ───────────────────────────────────────────────
        JButton btnReset = outlineBtn("↺  Nhập lại");
        btnReset.setAlignmentX(CENTER_ALIGNMENT);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnReset.addActionListener(e -> resetFields());

        // ── Tips ─────────────────────────────────────────────────
        JPanel tips = buildTipsPanel();

        // ── Assemble ──────────────────────────────────────────────
        card.add(iconWrapper);
        card.add(Box.createVerticalStrut(14));
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(cardSub);
        card.add(Box.createVerticalStrut(24));
        card.add(rowCurrent);
        card.add(Box.createVerticalStrut(14));
        card.add(rowNew);
        card.add(Box.createVerticalStrut(6));
        card.add(strengthRow);
        card.add(Box.createVerticalStrut(14));
        card.add(rowConfirm);
        card.add(Box.createVerticalStrut(10));
        card.add(chkShow);
        card.add(Box.createVerticalStrut(18));
        card.add(btnChange);
        card.add(Box.createVerticalStrut(10));
        card.add(btnReset);
        card.add(Box.createVerticalStrut(18));
        card.add(tips);
        return card;
    }

    // ── Strength bar ──────────────────────────────────────────────
    private JPanel buildStrengthRow() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        lblStrength = new JLabel("Độ mạnh: —");
        lblStrength.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblStrength.setForeground(TEXT_MUTED);

        // Track bar
        JPanel track = new JPanel(new BorderLayout());
        track.setBackground(new Color(0xF3F4F6));
        track.setPreferredSize(new Dimension(180, 6));
        track.setBorder(null);
        track.setOpaque(true);

        strengthBarFill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        strengthBarFill.setOpaque(false);
        strengthBarFill.setBackground(new Color(0xE5E7EB));
        track.add(strengthBarFill, BorderLayout.WEST);

        p.add(lblStrength, BorderLayout.WEST);
        p.add(track, BorderLayout.EAST);
        return p;
    }

    private void updateStrength() {
        String pwd = new String(pfNew.getPassword());
        if (pwd.isEmpty()) {
            lblStrength.setText("Độ mạnh: —");
            lblStrength.setForeground(TEXT_MUTED);
            strengthBarFill.setPreferredSize(new Dimension(0, 6));
            strengthBarFill.getParent().revalidate();
            updateTips(pwd);
            return;
        }
        int score = 0;
        if (pwd.length() >= 8)
            score++;
        if (pwd.matches(".*[A-Z].*"))
            score++;
        if (pwd.matches(".*[0-9].*"))
            score++;
        if (pwd.matches(".*[!@#$%^&*()_+=\\[\\]{}|;':\",./<>?].*"))
            score++;

        lblStrength.setText("Độ mạnh: " + STRENGTH_LABELS[score]);
        lblStrength.setForeground(STRENGTH_COLORS[score]);
        int trackW = 180;
        strengthBarFill.setPreferredSize(new Dimension((int) (trackW * STRENGTH_WIDTHS[score]), 6));
        strengthBarFill.setBackground(STRENGTH_COLORS[score]);
        strengthBarFill.getParent().revalidate();
        strengthBarFill.getParent().repaint();
        updateTips(pwd);
    }

    // ── Tips panel ────────────────────────────────────────────────
    private String[] tipTexts = {
            "Ít nhất 8 ký tự",
            "Có chữ hoa (A-Z)",
            "Có chữ số (0-9)",
            "Ký tự đặc biệt (!@#$...)"
    };

    private JPanel buildTipsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(true);
        p.setBackground(BG_TIPS);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_TIPS, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("💡  Gợi ý tạo mật khẩu mạnh");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        title.setForeground(PRIMARY);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 6));
        grid.setOpaque(false);
        tipIcons = new JLabel[tipTexts.length];
        for (int i = 0; i < tipTexts.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            row.setOpaque(false);
            tipIcons[i] = new JLabel("✓");
            tipIcons[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            tipIcons[i].setForeground(TIP_INACTIVE);
            JLabel txt = new JLabel(tipTexts[i]);
            txt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            txt.setForeground(TIP_INACTIVE);
            // store ref so updateTips can reach it
            tipIcons[i].putClientProperty("txt", txt);
            row.add(tipIcons[i]);
            row.add(txt);
            grid.add(row);
        }

        p.add(title, BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private void updateTips(String pwd) {
        if (tipIcons == null)
            return;
        boolean[] met = {
                pwd.length() >= 8,
                pwd.matches(".*[A-Z].*"),
                pwd.matches(".*[0-9].*"),
                pwd.matches(".*[!@#$%^&*()_+=\\[\\]{}|;':\",./<>?].*")
        };
        for (int i = 0; i < tipIcons.length; i++) {
            Color c = met[i] ? TIP_ACTIVE : TIP_INACTIVE;
            tipIcons[i].setForeground(c);
            JLabel txt = (JLabel) tipIcons[i].getClientProperty("txt");
            if (txt != null)
                txt.setForeground(c);
        }
    }

    // ── Logic đổi mật khẩu ───────────────────────────────────────
    private void changePassword() {
        char[] current = pfCurrent.getPassword();
        char[] newPwd = pfNew.getPassword();
        char[] confirm = pfConfirm.getPassword();

        if (current.length == 0) {
            showWarn("Vui lòng nhập mật khẩu hiện tại!");
            return;
        }
        if (!sha256(new String(current)).equals(currentUser.getPassword())) {
            showWarn("Mật khẩu hiện tại không đúng!");
            return;
        }
        if (newPwd.length < 6) {
            showWarn("Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }
        if (Arrays.equals(current, newPwd)) {
            showWarn("Mật khẩu mới không được trùng mật khẩu hiện tại!");
            return;
        }
        if (!Arrays.equals(newPwd, confirm)) {
            showWarn("Mật khẩu xác nhận không khớp!");
            return;
        }

        boolean ok = DatabaseService.updatePassword(currentUser.getUsername(), sha256(new String(newPwd)));
        if (!ok) {
            showWarn("Đổi mật khẩu thất bại! Vui lòng thử lại.");
            return;
        }

        resetFields();
        JOptionPane.showMessageDialog(this,
                "<html><b>✅ Đổi mật khẩu thành công!</b><br>"
                        + "Vui lòng dùng mật khẩu mới khi đăng nhập lần sau.</html>",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetFields() {
        pfCurrent.setText("");
        pfNew.setText("");
        pfConfirm.setText("");
        chkShow.setSelected(false);
        toggleShow(false);
        lblStrength.setText("Độ mạnh: —");
        lblStrength.setForeground(TEXT_MUTED);
        strengthBarFill.setPreferredSize(new Dimension(0, 6));
        strengthBarFill.getParent().revalidate();
        strengthBarFill.getParent().repaint();
        updateTips("");
    }

    private void toggleShow(boolean show) {
        char echo = show ? (char) 0 : '•';
        pfCurrent.setEchoChar(echo);
        pfNew.setEchoChar(echo);
        pfConfirm.setEchoChar(echo);
    }

    // ── UI Helpers ────────────────────────────────────────────────
    private JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        pf.setBackground(BG_FIELD);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(PRIMARY);
        pf.setEchoChar('•');
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        pf.setPreferredSize(new Dimension(0, 44));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Focus highlight
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                pf.setBorder(new CompoundBorder(new LineBorder(PRIMARY, 2, true), new EmptyBorder(7, 11, 7, 11)));
            }

            public void focusLost(FocusEvent e) {
                pf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return pf;
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        lbl.setForeground(TEXT_LABEL);

        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Nút chính với gradient paint */
    private JButton gradientBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isPressed() ? PRIMARY_DARK : new Color(0x3B82F6);
                Color c2 = getModel().isPressed() ? PRIMARY : new Color(0x6366F1);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Nút phụ outline */
    private JButton outlineBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0xF3F4F6) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.setColor(TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, "⚠  " + msg, "Lưu ý", JOptionPane.WARNING_MESSAGE);
    }

    private String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}