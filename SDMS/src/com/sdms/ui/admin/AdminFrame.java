package com.sdms.ui.admin;

import com.sdms.model.User;
import com.sdms.ui.login.LoginFrame;
import com.sdms.utils.UITheme;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;

public class AdminFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private JLabel lblClock;
    private String activeMenu = "Dashboard";

    private static final String[][] MENU_ITEMS = {
            { "Dashboard", "dashboard" },
            { "Quản lý sinh viên", "users" },
            { "Quản lý phòng", "door" },
            { "Quản lý hợp đồng", "file-text" },
            { "Quản lý điện nước", "bolt" },
            { "Quản lý thanh toán", "credit-card" },
            { "Quản lý vi phạm", "alert" },
            { "Thống kê & Báo cáo", "bar-chart" },
            { "Quản lý tài khoản", "user-cog" },
            { "Cài đặt", "settings" },
    };

    public AdminFrame(User user) {
        this.currentUser = user;
        setTitle("SDMS Admin — " + user.getFullName());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        initUI();
        startClock();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);
    }

    // ── SIDEBAR ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.SIDEBAR_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 0));

        // Logo area
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 60));
            }
        };
        logoArea.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel logoIcon = new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(32, 32));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, 32, 32, 8, 8));
                g2.setColor(Color.WHITE);
                g2.fillRect(4, 14, 24, 14);
                g2.fillRect(8, 8, 16, 8);
                g2.fillRect(11, 3, 10, 7);
                g2.dispose();
            }
        };
        JPanel brandText = new JPanel(new GridLayout(2, 1));
        brandText.setOpaque(false);
        JLabel lblBrand = new JLabel("SDMS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBrand.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Admin Portal");
        lblSub.setFont(UITheme.FONT_TINY);
        lblSub.setForeground(new Color(255, 255, 255, 120));
        brandText.add(lblBrand);
        brandText.add(lblSub);
        logoArea.add(logoIcon);
        logoArea.add(brandText);
        logoArea.setBounds(0, 0, UITheme.SIDEBAR_WIDTH, 60);

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(new Color(255, 255, 255, 18));
        sep.setBounds(0, 60, UITheme.SIDEBAR_WIDTH, 1);

        // Nav items
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBounds(0, 68, UITheme.SIDEBAR_WIDTH, 600);

        // Section header
        JLabel secMain = sectionLabel("TỔNG QUAN");
        navPanel.add(secMain);
        navPanel.add(buildMenuItem(MENU_ITEMS[0]));

        JLabel secManage = sectionLabel("QUẢN LÝ");
        navPanel.add(secManage);
        for (int i = 1; i <= 7; i++)
            navPanel.add(buildMenuItem(MENU_ITEMS[i]));

        JLabel secAccount = sectionLabel("TÀI KHOẢN & HỆ THỐNG");
        navPanel.add(secAccount);
        navPanel.add(buildMenuItem(MENU_ITEMS[8]));
        navPanel.add(buildMenuItem(MENU_ITEMS[9]));

        // Wrap navPanel vào ScrollPane để không bị tràn
        JScrollPane navScroll = new JScrollPane(navPanel);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.setBorder(null);
        navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // Panel trên cùng: logo + sep + nav
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);

        JPanel logoAndSep = new JPanel(null);
        logoAndSep.setOpaque(false);
        logoAndSep.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 61));
        logoAndSep.add(logoArea);
        logoAndSep.add(sep);

        topWrapper.add(logoAndSep, BorderLayout.NORTH);
        topWrapper.add(navScroll, BorderLayout.CENTER);

        // Logout ở dưới
        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        JPanel sepLogout = new JPanel();
        sepLogout.setBackground(new Color(255, 255, 255, 18));
        sepLogout.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 1));
        logoutWrapper.add(sepLogout, BorderLayout.NORTH);
        logoutWrapper.add(buildMenuItem(new String[] { "Đăng xuất", "logout" }), BorderLayout.CENTER);

        // Đổi sidebar layout sang BorderLayout
        sidebar.setLayout(new BorderLayout());
        sidebar.add(topWrapper, BorderLayout.CENTER);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

        return sidebar;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(255, 255, 255, 80));
        lbl.setBorder(new EmptyBorder(10, 18, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(UITheme.SIDEBAR_WIDTH, 28));
        return lbl;
    }

    private JPanel buildMenuItem(String[] item) {
        String name = item[0];
        boolean isActive = name.equals(activeMenu);

        JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (isActive || getClientProperty("hover") == Boolean.TRUE) {
                    g2.setColor(isActive ? new Color(37, 99, 235, 50) : new Color(255, 255, 255, 15));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    if (isActive) {
                        g2.setColor(UITheme.PRIMARY);
                        g2.fillRect(0, 0, 3, getHeight());
                    }
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pane.setOpaque(false);
        pane.setMaximumSize(new Dimension(UITheme.SIDEBAR_WIDTH, 40));
        pane.setAlignmentX(LEFT_ALIGNMENT);
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel(menuIcon(name));
        icon.setForeground(isActive ? UITheme.PRIMARY : new Color(255, 255, 255, 150));

        JLabel label = new JLabel(name);
        label.setFont(UITheme.FONT_BODY);
        label.setForeground(isActive ? new Color(0x93C5FD) : new Color(255, 255, 255, 150));

        if (name.equals("Đăng xuất"))
            label.setForeground(new Color(0xFCA5A5));

        pane.add(icon);
        pane.add(label);

        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (name.equals("Đăng xuất")) {
                    confirmLogout();
                    return;
                }
                activeMenu = name;
                switchPanel(name);
                rebuildSidebar();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pane.putClientProperty("hover", Boolean.TRUE);
                pane.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pane.putClientProperty("hover", null);
                pane.repaint();
            }
        });
        return pane;
    }

    private String menuIcon(String name) {
        return switch (name) {
            case "Dashboard" -> "⊞";
            case "Quản lý sinh viên" -> "👥";
            case "Quản lý phòng" -> "🚪";
            case "Quản lý hợp đồng" -> "📄";
            case "Quản lý điện nước" -> "⚡";
            case "Quản lý thanh toán" -> "💳";
            case "Quản lý vi phạm" -> "⚠";
            case "Thống kê & Báo cáo" -> "📊";
            case "Quản lý tài khoản" -> "👤";
            case "Cài đặt" -> "⚙";
            case "Đăng xuất" -> "↩";
            default -> "•";
        };
    }

    private void rebuildSidebar() {
        getContentPane().remove(((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.WEST));
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ── MAIN AREA ─────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_LIGHT);
        main.add(buildHeader(), BorderLayout.NORTH);
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_LIGHT);
        contentArea.add(buildDashboardPanel(), BorderLayout.CENTER);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(UITheme.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UITheme.BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setPreferredSize(new Dimension(0, UITheme.HEADER_HEIGHT));
        header.setOpaque(false);
        header.setBorder(new javax.swing.border.EmptyBorder(0, 16, 0, 16));

        // Clock
        lblClock = new JLabel("00:00");
        lblClock.setFont(UITheme.FONT_SMALL);
        lblClock.setForeground(UITheme.TEXT_SECONDARY);

        // Notification bell
        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String initials = "AD";
                g2.drawString(initials, (getWidth() - fm.stringWidth(initials)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }
        };

        JLabel lblAdmin = new JLabel("<html><b style='color:#0f172a'>" + currentUser.getFullName() + "</b></html>");
        lblAdmin.setFont(UITheme.FONT_SMALL);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);
        rightBox.add(lblClock);
        rightBox.add(bell);
        rightBox.add(avatar);
        rightBox.add(lblAdmin);

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    /** Được gọi từ các panel con (ví dụ DashboardPanel) để chuyển sang menu khác */
    public void navigateTo(String menuName) {
        activeMenu = menuName;
        switchPanel(menuName);
        rebuildSidebar();
    }

    private void switchPanel(String name) {
        contentArea.removeAll();
        JPanel panel = switch (name) {
            case "Dashboard" -> buildDashboardPanel();
            case "Quản lý sinh viên" -> new StudentPanel();
            case "Quản lý phòng" -> new RoomPanel();
            case "Quản lý hợp đồng" -> new ContractPanel();
            case "Quản lý điện nước" -> new UtilityPanel();
            case "Quản lý thanh toán" -> new PaymentPanel();
            case "Quản lý vi phạm" -> new ViolationPanel();
            case "Thống kê & Báo cáo" -> new ReportPanel();
            case "Quản lý tài khoản" -> new AccountManagementPanel();
            case "Cài đặt" -> new SettingsPanel(currentUser);
            default -> buildComingSoon(name);
        };
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private DashboardPanel buildDashboardPanel() {
        DashboardPanel dash = new DashboardPanel();
        dash.setOnNavigateToRooms(() -> {
            activeMenu = "Quản lý phòng";
            switchPanel("Quản lý phòng");
            rebuildSidebar();
        });
        return dash;
    }

    private JPanel buildComingSoon(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG_LIGHT);
        JLabel lbl = new JLabel(
                "<html><center><div style='font-size:40px'>🚧</div><br><b>" + name
                        + "</b><br><span style='color:gray'>Tính năng đang phát triển</span></center></html>",
                SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_H2);
        p.add(lbl);
        return p;
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> lblClock.setText(new SimpleDateFormat("HH:mm:ss").format(new Date())));
        t.start();
    }

    private void confirmExit() {
        // Nhấn X → đóng ứng dụng hoàn toàn
        int r = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn thoát ứng dụng không?", "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất không?", "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            final int savedState = getExtendedState();
            dispose();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            SwingUtilities.invokeLater(() -> login.setExtendedState(savedState));
        }
    }
}