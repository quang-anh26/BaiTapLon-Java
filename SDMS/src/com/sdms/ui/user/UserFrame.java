package com.sdms.ui.user;

import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.ui.login.LoginFrame;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UserFrame extends JFrame {

    private final User   currentUser;
    private       JPanel contentArea;
    private       String activeMenu = "Trang chủ";

    private static final String[] MENU = {
        "Trang chủ", "Thông tin cá nhân", "Thông tin phòng",
        "Hóa đơn", "Lịch sử thanh toán", "Thông báo", "Đổi mật khẩu"
    };

    public UserFrame(User user) {
        this.currentUser = user;
        setTitle("SDMS — Cổng Sinh Viên");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmLogout(); }
        });
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── SIDEBAR ──────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.SIDEBAR_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sb.setPreferredSize(new Dimension(200, 0));

        JPanel top = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255, 255, 255, 18));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(20, 0, 16, 0));

        // Avatar
        JPanel avt = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), getHeight(), UITheme.PURPLE));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String init = initials(currentUser.getFullName());
                g2.drawString(init,
                    (getWidth() - fm.stringWidth(init)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(60, 60); }
        };
        avt.setOpaque(false);
        avt.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel(currentUser.getFullName(), SwingConstants.CENTER);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setBorder(new EmptyBorder(10, 0, 2, 0));

        JLabel code = new JLabel(currentUser.getStudentId(), SwingConstants.CENTER);
        code.setFont(UITheme.FONT_TINY);
        code.setForeground(new Color(255, 255, 255, 120));
        code.setAlignmentX(CENTER_ALIGNMENT);

        // Room badge
        JPanel roomBadge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235, 60));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.setColor(new Color(96, 165, 250, 120));
                g2.setStroke(new BasicStroke(0.8f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roomBadge.setOpaque(false);
        roomBadge.setMaximumSize(new Dimension(120, 26));
        roomBadge.setAlignmentX(CENTER_ALIGNMENT);
        roomBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));

        // ✅ FIX: lấy phòng động từ DB thay vì cứng "A301"
        String dynamicRoom = "—";
        String sid = currentUser.getStudentId();
        if (sid != null && !sid.isEmpty()) {
            Student sv = DatabaseService.getAllStudents().stream()
                .filter(s -> s.getId().equals(sid))
                .findFirst().orElse(null);
            if (sv != null && sv.getRoomId() != null && !sv.getRoomId().isEmpty())
                dynamicRoom = sv.getRoomId();
        }
        JLabel roomLbl = new JLabel("🚪  Phòng " + dynamicRoom);
        roomLbl.setFont(UITheme.FONT_TINY);
        roomLbl.setForeground(new Color(0x93C5FD));
        roomBadge.add(roomLbl);

        top.add(avt);
        top.add(name);
        top.add(code);
        top.add(Box.createVerticalStrut(8));
        top.add(roomBadge);
        top.setBounds(0, 0, 200, 180);

        // Nav items
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBounds(0, 182, 200, 420);
        for (String m : MENU) nav.add(buildMenuItem(m));

        // Logout
        JPanel logout = buildMenuItemColored("↩  Đăng xuất", new Color(0xFCA5A5));
        logout.setBounds(0, 650, 200, 40);
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { confirmLogout(); }
        });

        sb.add(top);
        sb.add(nav);
        sb.add(logout);
        return sb;
    }

    private JPanel buildMenuItem(String name) {
        boolean active = name.equals(activeMenu);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                if (active || getClientProperty("hover") == Boolean.TRUE) {
                    g.setColor(active ? new Color(37, 99, 235, 40) : new Color(255, 255, 255, 12));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    if (active) { g.setColor(UITheme.PRIMARY); g.fillRect(0, 0, 3, getHeight()); }
                }
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(200, 40));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String icon = switch (name) {
            case "Trang chủ"          -> "⊞";
            case "Thông tin cá nhân"  -> "👤";
            case "Thông tin phòng"    -> "🚪";
            case "Hóa đơn"            -> "🧾";
            case "Lịch sử thanh toán" -> "📋";
            case "Thông báo"          -> "🔔";
            case "Đổi mật khẩu"       -> "🔑";
            default -> "•";
        };
        JLabel ico = new JLabel(icon);
        JLabel lbl = new JLabel(name.equals("Thông báo") ? name + " (3)" : name);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(active ? new Color(0x93C5FD) : new Color(255, 255, 255, 150));
        p.add(ico);
        p.add(lbl);

        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeMenu = name;
                switchPanel(name);
                rebuildSidebar();
            }
            @Override public void mouseEntered(MouseEvent e) {
                p.putClientProperty("hover", Boolean.TRUE); p.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                p.putClientProperty("hover", null); p.repaint();
            }
        });
        return p;
    }

    private JPanel buildMenuItemColored(String text, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(fg);
        p.add(lbl);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }

    private void rebuildSidebar() {
        getContentPane().remove(
            ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.WEST));
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ── MAIN ─────────────────────────────────────────────────────

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_LIGHT);
        main.add(buildTopHeader(), BorderLayout.NORTH);
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_LIGHT);
        contentArea.add(buildDashboard(), BorderLayout.CENTER);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private StudentDashboardPanel buildDashboard() {
        StudentDashboardPanel dash = new StudentDashboardPanel(currentUser);
        dash.setOnViewAllNotifications(() -> {
            activeMenu = "Thông báo";
            switchPanel("Thông báo");
            rebuildSidebar();
        });
        dash.setOnNavigateToInvoice(() -> {
            activeMenu = "Hóa đơn";
            switchPanel("Hóa đơn");
            rebuildSidebar();
        });
        return dash;
    }

    private JPanel buildTopHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        h.setPreferredSize(new Dimension(0, UITheme.HEADER_HEIGHT));
        h.setBorder(new EmptyBorder(0, 18, 0, 18));

        JLabel page = new JLabel("Trang chủ sinh viên  ·  Năm học 2024–2025");
        page.setFont(UITheme.FONT_BODY);
        page.setForeground(UITheme.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel ok   = UITheme.badge("✓  Hợp đồng còn hiệu lực", UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);
        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        right.add(ok);
        right.add(bell);

        h.add(page,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── Chuyển panel ─────────────────────────────────────────────

    private void switchPanel(String name) {
        contentArea.removeAll();
        JPanel p = switch (name) {
            case "Trang chủ"           -> buildDashboard();
            case "Thông tin cá nhân"   -> new StudentProfilePanel(currentUser);
            case "Thông tin phòng"     -> new StudentRoomPanel(currentUser);
            case "Hóa đơn"             -> new StudentInvoicePanel(currentUser);
            case "Lịch sử thanh toán"  -> new StudentPaymentHistoryPanel(currentUser);
            case "Thông báo"           -> new StudentNotificationPanel(currentUser);
            case "Đổi mật khẩu"        -> new ChangePasswordPanel(currentUser);
            default -> {
                JPanel ph = new JPanel(new GridBagLayout());
                ph.setBackground(UITheme.BG_LIGHT);
                JLabel l = new JLabel(
                    "<html><center><div style='font-size:36px'>🚧</div><br><b>"
                    + name + "</b><br><span style='color:gray'>Đang phát triển</span></center></html>",
                    SwingConstants.CENTER);
                l.setFont(UITheme.FONT_H2);
                ph.add(l);
                yield ph;
            }
        };
        contentArea.add(p, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Bạn có muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2)
            return String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0);
        return fullName.length() > 0 ? String.valueOf(fullName.charAt(0)) : "U";
    }
}