package com.sdms.ui.admin;

import com.sdms.utils.DatabaseService;
import com.sdms.utils.DataStore.PendingAccount;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AccountPanel – Giao diện đăng ký tài khoản sinh viên
 * Theme: Trắng–Xanh dương, flat design, tỉ lệ cân đối
 */
public class AccountRegister extends JPanel {

    // ── Bảng màu White–Blue ─────────────────────────────────────────────────────────
    private static final Color BG_PAGE      = new Color(0xF0, 0xF4, 0xFA);   // nền ngoài xanh nhạt
    private static final Color BG_CARD      = new Color(0xFF, 0xFF, 0xFF);   // card trắng
    private static final Color BG_INPUT     = new Color(0xF7, 0xF9, 0xFC);   // input xám rất nhạt
    private static final Color BG_INPUT_FOC = new Color(0xEE, 0xF5, 0xFF);   // input khi focus
    private static final Color ACCENT       = new Color(0x22, 0x7B, 0xF5);   // xanh chủ đạo
    private static final Color ACCENT_LIGHT = new Color(0xD6, 0xE8, 0xFF);   // xanh nhạt
    private static final Color ACCENT_DARK  = new Color(0x18, 0x5A, 0xC8);   // xanh đậm (hover btn)
    private static final Color BORDER_NORM  = new Color(0xD8, 0xE2, 0xF0);   // border thường
    private static final Color BORDER_FOC   = new Color(0x22, 0x7B, 0xF5);   // border focus
    private static final Color COLOR_GREEN  = new Color(0x16, 0xA3, 0x4A);
    private static final Color COLOR_RED    = new Color(0xDC, 0x26, 0x26);
    private static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);   // gần đen
    private static final Color TEXT_SECOND  = new Color(0x4A, 0x5A, 0x75);   // xanh xám
    private static final Color TEXT_MUTED   = new Color(0x90, 0xA0, 0xBB);   // placeholder
    private static final Color TEXT_LABEL   = new Color(0x1E, 0x3A, 0x70);   // label đậm
    private static final Color SECTION_BG   = new Color(0xEA, 0xF1, 0xFF);   // nền section header
    private static final Color DIVIDER      = new Color(0xE2, 0xEA, 0xF6);

    // ── Font ────────────────────────────────────────────────────────────────────────
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,  19);
    private static final Font F_SUB     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_SECTION = new Font("Segoe UI", Font.BOLD,  10);
    private static final Font F_LABEL   = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font F_INPUT   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_BTN     = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font F_ERR     = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Fields ──────────────────────────────────────────────────────────────────────
    private JTextField     tfUsername, tfFullName, tfPhone, tfDob, tfCccd;
    private JPasswordField pfPassword, pfConfirm;
    private JComboBox<String> cbGender;
    private JLabel         lblFileName;
    private File           selectedFile = null;
    private final Map<String, JLabel> errorLabels = new LinkedHashMap<>();
    
    // Callback for navigation
    private Runnable onBackToLogin;

    public AccountRegister() {
        this(null);
    }

    public AccountRegister(Runnable onBackToLogin) {
        this.onBackToLogin = onBackToLogin;
        setBackground(BG_PAGE);
        setLayout(new BorderLayout());

        // ── SCROLL PANE ────────────────────────────────────────────────────────────────
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_PAGE);
        scrollPane.getViewport().setBackground(BG_PAGE);
        
        // ── MAIN CONTENT PANEL ─────────────────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_PAGE);
        
        // ── CARD ────────────────────────────────────────────────────────────────────
        JPanel card = new RoundedPanel(16, BG_CARD, BORDER_NORM);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        card.setPreferredSize(new Dimension(780, 700));
        card.setMaximumSize(new Dimension(780, 700));

        // ── HEADER ──────────────────────────────────────────────────────────────────
        JLabel title = label("Đăng ký Tài khoản", F_TITLE, TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(vgap(4));

        JLabel sub = label("Vui lòng điền chính xác thông tin theo CCCD và giấy báo trúng tuyển", F_SUB, TEXT_SECOND);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        card.add(sub);
        card.add(vgap(18));
        card.add(divider());
        card.add(vgap(16));

        // ── SECTION 1: THÔNG TIN CÁ NHÂN ────────────────────────────────────────────
        card.add(sectionHeader("Thông tin cá nhân"));
        card.add(vgap(12));

        JPanel g1 = grid2(18, 10);
        g1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));

        tfUsername = input("VD: 22IT1234...");
        tfFullName = input("Nguyễn Văn A...");
        tfPhone    = input("0912 345 678...");
        tfCccd     = input("035xxxxxxxxx...");
        cbGender   = combo("Nam", "Nữ", "Khác");

        tfDob = input("dd/MM/yyyy");
        tfDob.setEditable(false);
        JButton btnCal = calBtn();
        JPanel dobWrap = new JPanel(new BorderLayout(6, 0));
        dobWrap.setOpaque(false);
        dobWrap.add(inputWrap(tfDob), BorderLayout.CENTER);
        dobWrap.add(btnCal, BorderLayout.EAST);

        g1.add(fieldBlock("Mã sinh viên (Tên đăng nhập)", "username", true, inputWrap(tfUsername)));
        g1.add(fieldBlock("Họ và tên đầy đủ",             "fullName", true, inputWrap(tfFullName)));
        g1.add(fieldBlock("Số điện thoại",                 "phone",    true, inputWrap(tfPhone)));
        g1.add(fieldBlock("Ngày sinh",                     "dob",      true, dobWrap));
        g1.add(fieldBlock("Số CCCD",                       "cccd",     true, inputWrap(tfCccd)));
        g1.add(fieldBlock("Giới tính",                     "gender",   false, cbGender));

        card.add(g1);
        card.add(vgap(16));

        // ── SECTION 2: BẢO MẬT ──────────────────────────────────────────────────────
        card.add(sectionHeader("Bảo mật tài khoản"));
        card.add(vgap(12));

        JPanel g2 = grid2(18, 0);
        g2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        pfPassword = passField();
        pfConfirm  = passField();
        g2.add(fieldBlock("Mật khẩu",          "password", true, passRow(pfPassword)));
        g2.add(fieldBlock("Xác nhận mật khẩu", "confirm",  true, passRow(pfConfirm)));
        card.add(g2);
        card.add(vgap(16));

        // ── SECTION 3: MINH CHỨNG ───────────────────────────────────────────────────
        card.add(sectionHeader("Minh chứng trúng tuyển"));
        card.add(vgap(10));
        JPanel uploadZone = uploadZone();
        card.add(uploadZone);
        card.add(vgap(22));

        // ── BUTTONS ─────────────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 14, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnBackLogin = secondaryBtn("← Quay lại Đăng nhập");
        JButton btnBack = ghostBtn("Làm lại");
        JButton btnReg  = primaryBtn("Đăng Ký Ngay  →");
        btnRow.add(btnBackLogin);
        btnRow.add(btnBack);
        btnRow.add(btnReg);
        card.add(btnRow);

        // ── EVENTS ───────────────────────────────────────────────────────────────────
        btnCal.addActionListener(e -> showDatePicker());
        btnReg.addActionListener(e -> handleRegistration());
        btnBack.addActionListener(e -> clearForm());
        btnBackLogin.addActionListener(e -> {
            if (onBackToLogin != null) {
                onBackToLogin.run();
            }
        });

        // Add card to content panel with centering
        contentPanel.add(card, new GridBagConstraints());
        
        // Add content panel to scroll pane
        scrollPane.setViewportView(contentPanel);
        
        // Add scroll pane to main panel
        add(scrollPane, BorderLayout.CENTER);
    }

    // ────────────────────────────────────────────────────────────────────────────────
    //  UI Builder helpers
    // ────────────────────────────────────────────────────────────────────────────────

    private JComponent divider() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DIVIDER);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setPreferredSize(new Dimension(100, 1));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** Section header với nền xanh nhạt */
    private JPanel sectionHeader(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SECTION_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 10, 5, 10));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // Thanh dọc xanh bên trái
        JPanel bar = new JPanel();
        bar.setBackground(ACCENT);
        bar.setPreferredSize(new Dimension(3, 14));
        p.add(bar);
        p.add(Box.createHorizontalStrut(7));

        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(F_SECTION);
        lbl.setForeground(ACCENT);
        p.add(lbl);
        return p;
    }

    /** Input field bo tròn, focus glow xanh */
    private JTextField input(String ph) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? BG_INPUT_FOC : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(ph, 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isFocusOwner()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 50));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 7, 7);
                }
                g2.setColor(isFocusOwner() ? BORDER_FOC : BORDER_NORM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(9, 10, 9, 10));
        tf.setFont(F_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setPreferredSize(new Dimension(0, 38));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { tf.repaint(); }
            public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    private JPanel inputWrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPasswordField passField() {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? BG_INPUT_FOC : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isFocusOwner()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 50));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 7, 7);
                }
                g2.setColor(isFocusOwner() ? BORDER_FOC : BORDER_NORM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        pf.setOpaque(false);
        pf.setBackground(BG_INPUT);
        pf.setBorder(new EmptyBorder(9, 10, 9, 38));
        pf.setFont(F_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT);
        pf.setEchoChar('●');
        pf.setPreferredSize(new Dimension(0, 38));
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { pf.repaint(); }
            public void focusLost(FocusEvent e)   { pf.repaint(); }
        });
        return pf;
    }

    private JPanel passRow(JPasswordField pf) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(pf.isFocusOwner() ? BG_INPUT_FOC : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (pf.isFocusOwner()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 50));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 7, 7);
                }
                g2.setColor(pf.isFocusOwner() ? BORDER_FOC : BORDER_NORM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);

        // Eye toggle
        JToggleButton eye = new JToggleButton("👁") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(isSelected()
                    ? new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30)
                    : new Color(0,0,0,0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        eye.setOpaque(false); eye.setContentAreaFilled(false); eye.setFocusPainted(false);
        eye.setPreferredSize(new Dimension(36, 38));
        eye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        eye.setForeground(TEXT_MUTED);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addActionListener(e -> {
            pf.setEchoChar(eye.isSelected() ? (char) 0 : '●');
            eye.setForeground(eye.isSelected() ? ACCENT : TEXT_MUTED);
            p.repaint();
        });

        p.add(pf, BorderLayout.CENTER);
        p.add(eye, BorderLayout.EAST);
        return p;
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_NORM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        cb.setOpaque(false);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(F_INPUT);
        cb.setBorder(new EmptyBorder(4, 8, 4, 8));
        cb.setPreferredSize(new Dimension(0, 38));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? ACCENT_LIGHT : Color.WHITE);
                setForeground(sel ? ACCENT : TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 12, 6, 12));
                setFont(F_INPUT);
                return this;
            }
        });
        return cb;
    }

    /** Calendar button */
    private JButton calBtn() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_DARK
                         : getModel().isRollover() ? ACCENT
                         : ACCENT_LIGHT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // icon lịch vẽ tay
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? Color.WHITE : ACCENT);
                int cx = getWidth()/2 - 7, cy = getHeight()/2 - 7;
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx, cy+2, 14, 12, 3, 3);
                g2.drawLine(cx+4, cy, cx+4, cy+4);
                g2.drawLine(cx+10, cy, cx+10, cy+4);
                g2.drawLine(cx, cy+6, cx+14, cy+6);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_NORM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel fieldBlock(String labelTxt, String key, boolean req, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JPanel lr = row(FlowLayout.LEFT);
        JLabel l = new JLabel(labelTxt);
        l.setFont(F_LABEL); l.setForeground(TEXT_LABEL);
        lr.add(l);
        if (req) {
            JLabel r = new JLabel(" *");
            r.setFont(F_LABEL); r.setForeground(COLOR_RED);
            lr.add(r);
        }
        lr.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lr);
        p.add(vgap(5));
        comp.setAlignmentX(LEFT_ALIGNMENT);
        p.add(comp);

        JLabel err = new JLabel(" ");
        err.setFont(F_ERR); err.setForeground(COLOR_RED);
        err.setAlignmentX(LEFT_ALIGNMENT);
        p.add(err);
        errorLabels.put(key, err);
        return p;
    }

    private JPanel uploadZone() {
        JPanel zone = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(selectedFile != null ? new Color(0xEA, 0xF6, 0xEE) : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color bc = selectedFile != null ? COLOR_GREEN : BORDER_NORM;
                float[] dash = {5f, 4f};
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1f, dash, 0f));
                g2.setColor(bc);
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        zone.setOpaque(false);
        zone.setBorder(new EmptyBorder(13, 16, 13, 16));
        zone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        zone.setAlignmentX(LEFT_ALIGNMENT);
        zone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon box
        JLabel iconBox = new JLabel("📁", SwingConstants.CENTER);
        iconBox.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconBox.setPreferredSize(new Dimension(40, 40));
        iconBox.setOpaque(true);
        iconBox.setBackground(ACCENT_LIGHT);
        iconBox.setBorder(BorderFactory.createLineBorder(
            new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80), 1, true));

        // Text
        JPanel tp = new JPanel();
        tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
        tp.setOpaque(false);
        JLabel tl = new JLabel("Chọn ảnh hoặc file minh chứng trúng tuyển");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tl.setForeground(TEXT_PRIMARY);
        lblFileName = new JLabel("Hỗ trợ: JPG, PNG, PDF — nhấp vào đây để chọn");
        lblFileName.setFont(F_SMALL);
        lblFileName.setForeground(TEXT_MUTED);
        tp.add(tl); tp.add(Box.createVerticalStrut(2)); tp.add(lblFileName);

        zone.add(iconBox, BorderLayout.WEST);
        zone.add(tp, BorderLayout.CENTER);

        zone.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Ảnh & PDF", "jpg", "jpeg", "png", "pdf"));
                if (fc.showOpenDialog(AccountRegister.this) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fc.getSelectedFile();
                    iconBox.setText("📄");
                    iconBox.setBackground(new Color(0xD1, 0xFA, 0xDF));
                    lblFileName.setText("✓  " + selectedFile.getName() +
                        "  (" + (selectedFile.length() / 1024) + " KB)");
                    lblFileName.setForeground(COLOR_GREEN);
                    clearError("file");
                    zone.repaint();
                }
            }
        });
        return zone;
    }

    private JButton primaryBtn(String txt) {
        JButton btn = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? ACCENT_DARK
                        : getModel().isRollover() ? new Color(0x1D, 0x6E, 0xDA)
                        : ACCENT;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                // Subtle shine trên cùng
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight()/2, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setFont(F_BTN); btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    private JButton secondaryBtn(String txt) {
        JButton btn = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_DARK
                         : getModel().isRollover() ? new Color(0x1D, 0x6E, 0xDA)
                         : ACCENT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setFont(F_BTN); btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    private JButton ghostBtn(String txt) {
        JButton btn = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_LIGHT
                         : getModel().isRollover() ? new Color(0xE8, 0xF0, 0xFF)
                         : BG_INPUT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(getModel().isRollover() ? ACCENT : BORDER_NORM);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setFont(F_BTN);
        btn.setForeground(TEXT_SECOND);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    // ── Date Picker ─────────────────────────────────────────────────────────────────
    // ── Date Picker ─────────────────────────────────────────────────────────────────
private void showDatePicker() {
    JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn ngày sinh", true);
    d.getContentPane().setBackground(Color.WHITE);
    d.setLayout(new BorderLayout());
    d.setSize(420, 160);  // Tăng kích thước từ 320x130 lên 420x160
    d.setLocationRelativeTo(this);
    d.setResizable(false);

    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 20));  // Tăng gap
    p.setBackground(Color.WHITE);

    JComboBox<Integer> cbDay = new JComboBox<>();
    JComboBox<Integer> cbMonth = new JComboBox<>();
    JComboBox<Integer> cbYear = new JComboBox<>();
    
    for (int i = 1; i <= 31; i++) cbDay.addItem(i);
    for (int i = 1; i <= 12; i++) cbMonth.addItem(i);
    int cy = Calendar.getInstance().get(Calendar.YEAR);
    for (int y = cy - 30; y <= cy; y++) cbYear.addItem(y);
    cbYear.setSelectedItem(cy - 19);
    
    // Đặt preferred size rõ ràng cho các combo box
    cbDay.setPreferredSize(new Dimension(70, 32));
    cbMonth.setPreferredSize(new Dimension(70, 32));
    cbYear.setPreferredSize(new Dimension(85, 32));
    
    for (JComboBox<?> c : new JComboBox[]{cbDay, cbMonth, cbYear}) {
        c.setBackground(BG_INPUT);
        c.setForeground(TEXT_PRIMARY);
        c.setFont(F_INPUT);
        c.setBorder(BorderFactory.createLineBorder(BORDER_NORM, 1, true));
    }

    // Panel chứa label + combo (dùng GridBagLayout để canh đẹp hơn)
    JPanel selectorPanel = new JPanel(new GridBagLayout());
    selectorPanel.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 8, 0, 8);
    
    // Ngày
    gbc.gridx = 0; gbc.gridy = 0;
    JLabel lblDay = new JLabel("Ngày");
    lblDay.setFont(F_LABEL);
    lblDay.setForeground(TEXT_SECOND);
    selectorPanel.add(lblDay, gbc);
    
    gbc.gridy = 1;
    selectorPanel.add(cbDay, gbc);
    
    // Tháng
    gbc.gridx = 1;
    gbc.gridy = 0;
    JLabel lblMonth = new JLabel("Tháng");
    lblMonth.setFont(F_LABEL);
    lblMonth.setForeground(TEXT_SECOND);
    selectorPanel.add(lblMonth, gbc);
    
    gbc.gridy = 1;
    selectorPanel.add(cbMonth, gbc);
    
    // Năm
    gbc.gridx = 2;
    gbc.gridy = 0;
    JLabel lblYear = new JLabel("Năm");
    lblYear.setFont(F_LABEL);
    lblYear.setForeground(TEXT_SECOND);
    selectorPanel.add(lblYear, gbc);
    
    gbc.gridy = 1;
    selectorPanel.add(cbYear, gbc);
    
    d.add(selectorPanel, BorderLayout.CENTER);

    JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
    bot.setBackground(new Color(0xF4, 0xF7, 0xFF));
    bot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_NORM));
    JButton ok = primaryBtn("  Xác nhận  ");
    ok.setPreferredSize(new Dimension(130, 36));
    ok.addActionListener(ev -> {
        tfDob.setText(String.format("%02d/%02d/%d",
            cbDay.getSelectedItem(), cbMonth.getSelectedItem(), cbYear.getSelectedItem()));
        clearError("dob");
        d.dispose();
    });
    bot.add(ok);
    d.add(bot, BorderLayout.SOUTH);
    d.setVisible(true);
}

    // ── Validation ──────────────────────────────────────────────────────────────────
    private void handleRegistration() {
        clearAllErrors();
        boolean ok = true;

        String un = tfUsername.getText().trim(), fn = tfFullName.getText().trim(),
               ph = tfPhone.getText().trim(),    dob = tfDob.getText().trim(),
               cc = tfCccd.getText().trim();
        String pw = new String(pfPassword.getPassword()), cf = new String(pfConfirm.getPassword());

        if (un.isEmpty())                 { setError("username","Không được để trống"); ok=false; }
        if (fn.isEmpty())                 { setError("fullName","Không được để trống"); ok=false; }
        if (ph.isEmpty())                 { setError("phone","Không được để trống"); ok=false; }
        else if (!ph.matches("^0\\d{9}$")){ setError("phone","Phải 10 số, bắt đầu bằng 0"); ok=false; }
        if (dob.isEmpty())                { setError("dob","Vui lòng chọn ngày sinh"); ok=false; }
        if (cc.isEmpty())                 { setError("cccd","Không được để trống"); ok=false; }
        else if (!cc.matches("^035\\d{9}$")){ setError("cccd","Phải 12 số, bắt đầu bằng 035"); ok=false; }
        if (pw.length() < 4)              { setError("password","Tối thiểu 4 ký tự"); ok=false; }
        if (!pw.equals(cf))               { setError("confirm","Mật khẩu không khớp"); ok=false; }
        if (selectedFile == null)         { setError("file","Vui lòng tải lên minh chứng"); ok=false; }

        if (!ok) return;

        // Sinh mã đơn và thời gian đăng ký
        String newId = DatabaseService.nextPendingId();
        String registeredAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String gender = (String) cbGender.getSelectedItem();

        PendingAccount pa = new PendingAccount(
                newId, un, fn, ph, dob, cc, gender, registeredAt);

        boolean saved = DatabaseService.addPendingAccount(pa);
        if (saved) {
            showToast("Đăng ký thành công! Tài khoản \"" + un + "\" đang chờ phê duyệt.", true);
            clearForm();
        } else {
            showToast("Lỗi: Không thể lưu đơn đăng ký. Kiểm tra kết nối SQL Server.", false);
        }
    }

    /** SHA-256 hash helper */
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    private void showToast(String msg, boolean success) {
        JDialog t = new JDialog();
        t.setUndecorated(true); t.setAlwaysOnTop(true);
        t.setBackground(new Color(0,0,0,0));

        JPanel c = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(success ? COLOR_GREEN : COLOR_RED);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                // Thanh màu bên trái
                g2.setColor(success ? COLOR_GREEN : COLOR_RED);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(12, 16, 12, 16));
        c.setBackground(Color.WHITE);

        JLabel ico = new JLabel(success ? "✅" : "⚠️");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel txt = new JLabel("<html>" + msg + "</html>");
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setForeground(TEXT_PRIMARY);

        c.add(ico, BorderLayout.WEST);
        c.add(txt, BorderLayout.CENTER);

        t.setContentPane(c);
        t.pack();
        t.setSize(Math.max(t.getWidth(), 360), t.getHeight() + 4);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        t.setLocation(sc.width - t.getWidth() - 24, sc.height - t.getHeight() - 52);
        t.setVisible(true);
        new Timer(4000, e -> t.dispose()) {{ setRepeats(false); start(); }};
    }

    // ── Utils ────────────────────────────────────────────────────────────────────────
    private JPanel row(int align) {
        JPanel p = new JPanel(new FlowLayout(align, 0, 0));
        p.setOpaque(false);
        return p;
    }
    private JPanel grid2(int hgap, int vgap) {
        JPanel p = new JPanel(new GridLayout(0, 2, hgap, vgap));
        p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }
    private Component vgap(int h) { return Box.createVerticalStrut(h); }
    private JLabel label(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }
    private void setError(String k, String m) {
        JLabel l = errorLabels.get(k); if (l!=null) l.setText("⚠  " + m);
    }
    private void clearError(String k) {
        JLabel l = errorLabels.get(k); if (l!=null) l.setText(" ");
    }
    private void clearAllErrors() { errorLabels.values().forEach(l -> l.setText(" ")); }

    private void clearForm() {
        tfUsername.setText(""); tfFullName.setText(""); tfPhone.setText("");
        tfDob.setText(""); tfCccd.setText("");
        pfPassword.setText(""); pfConfirm.setText("");
        cbGender.setSelectedIndex(0);
        selectedFile = null;
        lblFileName.setText("Hỗ trợ: JPG, PNG, PDF — nhấp vào đây để chọn");
        lblFileName.setForeground(TEXT_MUTED);
        clearAllErrors();
    }

    // ── Rounded card ────────────────────────────────────────────────────────────────
    static class RoundedPanel extends JPanel {
        private final int r; private final Color bg, border;
        RoundedPanel(int r, Color bg, Color border) {
            this.r=r; this.bg=bg; this.border=border; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Drop shadow
            g2.setColor(new Color(0x22, 0x7B, 0xF5, 18));
            g2.fillRoundRect(4, 6, getWidth()-6, getHeight()-6, r+2, r+2);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(2, 4, getWidth()-4, getHeight()-4, r+1, r+1);
            // Card
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, r, r);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-3, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}