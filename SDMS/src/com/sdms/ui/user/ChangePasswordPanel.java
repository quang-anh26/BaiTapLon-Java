package com.sdms.ui.user;

import com.sdms.model.User;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Panel đổi mật khẩu cho sinh viên.
 * Có kiểm tra độ mạnh mật khẩu và validate đầy đủ.
 */
public class ChangePasswordPanel extends JPanel {

    private final User currentUser;

    private JPasswordField pfCurrent, pfNew, pfConfirm;
    private JLabel         lblStrength, lblStrengthBar;
    private JCheckBox      chkShow;

    public ChangePasswordPanel(User currentUser) {
        this.currentUser = currentUser;

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("🔒  Đổi mật khẩu");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Đổi mật khẩu");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Nội dung ──────────────────────────────────────────────────
    private JPanel buildContent() {
        // Căn giữa card
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG_LIGHT);

        JPanel card = buildCard();
        card.setPreferredSize(new Dimension(460, 520));
        outer.add(card);
        return outer;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(28, 30, 28, 30)
        ));

        // Icon khóa lớn
        JLabel lockIcon = new JLabel("🔐", SwingConstants.CENTER);
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lockIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel cardTitle = new JLabel("Thay đổi mật khẩu", SwingConstants.CENTER);
        cardTitle.setFont(UITheme.FONT_H2);
        cardTitle.setForeground(UITheme.TEXT_PRIMARY);
        cardTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel cardSub = new JLabel(
            "Tài khoản: " + currentUser.getUsername(), SwingConstants.CENTER);
        cardSub.setFont(UITheme.FONT_SMALL);
        cardSub.setForeground(UITheme.TEXT_MUTED);
        cardSub.setAlignmentX(CENTER_ALIGNMENT);

        // ── Mật khẩu hiện tại ────────────────────────────────────
        pfCurrent = passwordField();
        JPanel rowCurrent = fieldRow("🔑  Mật khẩu hiện tại *", pfCurrent);

        // ── Mật khẩu mới ─────────────────────────────────────────
        pfNew = passwordField();
        pfNew.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });
        JPanel rowNew = fieldRow("🆕  Mật khẩu mới *", pfNew);

        // Thanh độ mạnh
        JPanel strengthRow = buildStrengthRow();

        // ── Xác nhận mật khẩu mới ────────────────────────────────
        pfConfirm = passwordField();
        JPanel rowConfirm = fieldRow("✅  Xác nhận mật khẩu mới *", pfConfirm);

        // ── Checkbox hiện mật khẩu ───────────────────────────────
        chkShow = new JCheckBox("Hiện mật khẩu");
        chkShow.setFont(UITheme.FONT_SMALL);
        chkShow.setForeground(UITheme.TEXT_SECONDARY);
        chkShow.setOpaque(false);
        chkShow.setFocusPainted(false);
        chkShow.setAlignmentX(LEFT_ALIGNMENT);
        chkShow.addActionListener(e -> toggleShowPassword(chkShow.isSelected()));

        // ── Nút đổi mật khẩu ─────────────────────────────────────
        JButton btnChange = UITheme.primaryBtn("🔒  Xác nhận đổi mật khẩu");
        btnChange.setAlignmentX(CENTER_ALIGNMENT);
        btnChange.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnChange.addActionListener(e -> changePassword());

        JButton btnReset = UITheme.outlineBtn("↺  Nhập lại");
        btnReset.setAlignmentX(CENTER_ALIGNMENT);
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnReset.addActionListener(e -> resetFields());

        // ── Gợi ý mật khẩu mạnh ──────────────────────────────────
        JPanel tips = buildTipsPanel();

        // Ghép
        card.add(lockIcon);
        card.add(Box.createVerticalStrut(6));
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(cardSub);
        card.add(Box.createVerticalStrut(20));
        card.add(rowCurrent);
        card.add(Box.createVerticalStrut(10));
        card.add(rowNew);
        card.add(Box.createVerticalStrut(4));
        card.add(strengthRow);
        card.add(Box.createVerticalStrut(10));
        card.add(rowConfirm);
        card.add(Box.createVerticalStrut(8));
        card.add(chkShow);
        card.add(Box.createVerticalStrut(16));
        card.add(btnChange);
        card.add(Box.createVerticalStrut(8));
        card.add(btnReset);
        card.add(Box.createVerticalStrut(16));
        card.add(tips);
        return card;
    }

    // ── Thanh độ mạnh mật khẩu ───────────────────────────────────
    private JPanel buildStrengthRow() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        lblStrength = new JLabel("Độ mạnh: —");
        lblStrength.setFont(UITheme.FONT_TINY);
        lblStrength.setForeground(UITheme.TEXT_MUTED);

        lblStrengthBar = new JLabel("          ");
        lblStrengthBar.setOpaque(true);
        lblStrengthBar.setBackground(UITheme.BG_SECONDARY);
        lblStrengthBar.setBorder(new EmptyBorder(4, 0, 4, 0));
        lblStrengthBar.setPreferredSize(new Dimension(200, 8));

        p.add(lblStrength,    BorderLayout.WEST);
        p.add(lblStrengthBar, BorderLayout.EAST);
        return p;
    }

    /** Cập nhật thanh độ mạnh khi nhập mật khẩu mới */
    private void updateStrength() {
        String pwd = new String(pfNew.getPassword());
        int score = 0;
        if (pwd.length() >= 8) score++;
        if (pwd.matches(".*[A-Z].*")) score++;
        if (pwd.matches(".*[0-9].*")) score++;
        if (pwd.matches(".*[!@#$%^&*()_+=\\[\\]{}|;':\",./<>?].*")) score++;

        String[] labels = {"Quá yếu", "Yếu", "Trung bình", "Mạnh", "Rất mạnh"};
        Color[]  colors = {
            new Color(0xEF4444), new Color(0xF97316),
            new Color(0xEAB308), new Color(0x22C55E), new Color(0x16A34A)
        };

        if (pwd.isEmpty()) {
            lblStrength.setText("Độ mạnh: —");
            lblStrengthBar.setBackground(UITheme.BG_SECONDARY);
        } else {
            lblStrength.setText("Độ mạnh: " + labels[score]);
            lblStrength.setForeground(colors[score]);
            lblStrengthBar.setBackground(colors[score]);
        }
    }

    // ── Panel gợi ý mật khẩu mạnh ────────────────────────────────
    private JPanel buildTipsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(true);
        p.setBackground(UITheme.BG_SECONDARY);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel title = new JLabel("💡  Gợi ý tạo mật khẩu mạnh");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_SECONDARY);

        String[] tips = {
            "✔  Ít nhất 8 ký tự",
            "✔  Có chữ hoa (A-Z)",
            "✔  Có chữ số (0-9)",
            "✔  Có ký tự đặc biệt (!@#$...)",
        };
        JPanel tipsList = new JPanel(new GridLayout(2, 2, 8, 4));
        tipsList.setOpaque(false);
        for (String t : tips) {
            JLabel l = new JLabel(t);
            l.setFont(UITheme.FONT_TINY);
            l.setForeground(UITheme.TEXT_MUTED);
            tipsList.add(l);
        }

        p.add(title,    BorderLayout.NORTH);
        p.add(tipsList, BorderLayout.CENTER);
        return p;
    }

    // ── Logic đổi mật khẩu ───────────────────────────────────────
    private void changePassword() {
        char[] current = pfCurrent.getPassword();
        char[] newPwd  = pfNew.getPassword();
        char[] confirm = pfConfirm.getPassword();

        // Validate mật khẩu hiện tại
        if (current.length == 0) {
            showWarn("Vui lòng nhập mật khẩu hiện tại!"); return;
        }
        String currentHash = sha256(new String(current));
           if (!currentHash.equals(currentUser.getPassword())) {
           showWarn("Mật khẩu hiện tại không đúng!");
            return;
           }
        // Validate mật khẩu mới
        if (newPwd.length < 6) {
            showWarn("Mật khẩu mới phải có ít nhất 6 ký tự!"); return;
        }
        if (Arrays.equals(current, newPwd)) {
            showWarn("Mật khẩu mới không được trùng với mật khẩu hiện tại!"); return;
        }
        // Validate xác nhận
        if (!Arrays.equals(newPwd, confirm)) {
            showWarn("Mật khẩu xác nhận không khớp!"); return;
        }

       
         String newHash = sha256(new String(newPwd));

         boolean ok = DatabaseService.updatePassword(
           currentUser.getUsername(),
         newHash
        );

       if (!ok) {
         showWarn("Đổi mật khẩu thất bại!");
          return;
        }

        resetFields();

        JOptionPane.showMessageDialog(this,
     "<html>✅ <b>Đổi mật khẩu thành công!</b><br>"
     + "Vui lòng đăng nhập lại bằng mật khẩu mới khi cần.</html>",
      "Thành công",
           JOptionPane.INFORMATION_MESSAGE);
    }

    /** Xóa trắng tất cả fields */
    private void resetFields() {
        pfCurrent.setText("");
        pfNew.setText("");
        pfConfirm.setText("");
        chkShow.setSelected(false);
        toggleShowPassword(false);
        lblStrength.setText("Độ mạnh: —");
        lblStrength.setForeground(UITheme.TEXT_MUTED);
        lblStrengthBar.setBackground(UITheme.BG_SECONDARY);
    }

    /** Hiện/ẩn mật khẩu cho cả 3 trường */
    private void toggleShowPassword(boolean show) {
        char echo = show ? '\0' : '•';
        pfCurrent.setEchoChar(show ? (char)0 : '•');
        pfNew.setEchoChar(show ? (char)0 : '•');
        pfConfirm.setEchoChar(show ? (char)0 : '•');
    }

    // ── Helpers ───────────────────────────────────────────────────

    private JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_BODY);
        pf.setBackground(UITheme.BG_SECONDARY);
        pf.setForeground(UITheme.TEXT_PRIMARY);
        pf.setCaretColor(UITheme.PRIMARY);
        pf.setEchoChar('•');
        pf.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        pf.setPreferredSize(new Dimension(0, 40));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return pf;
    }

    private JPanel fieldRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JLabel lbl = UITheme.formLabel(label);
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, "⚠ " + msg,
            "Lưu ý", JOptionPane.WARNING_MESSAGE);
    }
    private String sha256(String input) {
    try {
        java.security.MessageDigest md =
                java.security.MessageDigest.getInstance("SHA-256");

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

}