package com.sdms.ui.user;

import com.sdms.model.Invoice;
import com.sdms.model.User;
import com.sdms.ui.login.LoginFrame;
import com.sdms.utils.DataStore;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UserFrame extends JFrame {

    private final User     currentUser;
    private       JPanel   contentArea;
    private       String   activeMenu = "Trang chủ";

    private static final String[] MENU = {
        "Trang chủ","Thông tin cá nhân","Thông tin phòng",
        "Hóa đơn","Lịch sử thanh toán","Thông báo","Đổi mật khẩu"
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

    // ── SIDEBAR ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.SIDEBAR_DARK); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        sb.setPreferredSize(new Dimension(200, 0));

        // User info top
        JPanel top = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255,255,255,18));
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(20, 0, 16, 0));

        // Avatar circle
        JPanel avt = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,UITheme.PRIMARY,getWidth(),getHeight(),UITheme.PURPLE));
                g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI",Font.BOLD,22));
                FontMetrics fm=g2.getFontMetrics();
                String init=initials(currentUser.getFullName());
                g2.drawString(init,(getWidth()-fm.stringWidth(init))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(60,60); }
        };
        avt.setOpaque(false);
        avt.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel(currentUser.getFullName(), SwingConstants.CENTER);
        name.setFont(new Font("Segoe UI",Font.BOLD,13));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setBorder(new EmptyBorder(10,0,2,0));

        JLabel code = new JLabel(currentUser.getStudentId(), SwingConstants.CENTER);
        code.setFont(UITheme.FONT_TINY);
        code.setForeground(new Color(255,255,255,120));
        code.setAlignmentX(CENTER_ALIGNMENT);

        // Room badge
        JPanel roomBadge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37,99,235,60));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),getHeight(),getHeight()));
                g2.setColor(new Color(96,165,250,120));
                g2.setStroke(new BasicStroke(0.8f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,getHeight(),getHeight()));
                g2.dispose(); super.paintComponent(g);
            }
        };
        roomBadge.setOpaque(false);
        roomBadge.setMaximumSize(new Dimension(120,26));
        roomBadge.setAlignmentX(CENTER_ALIGNMENT);
        roomBadge.setLayout(new FlowLayout(FlowLayout.CENTER,4,2));
        JLabel roomLbl = new JLabel("🚪  Phòng A301");
        roomLbl.setFont(UITheme.FONT_TINY); roomLbl.setForeground(new Color(0x93C5FD));
        roomBadge.add(roomLbl);

        top.add(avt); top.add(name); top.add(code); top.add(Box.createVerticalStrut(8)); top.add(roomBadge);
        top.setBounds(0,0,200,180);

        // Nav items
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBounds(0,182,200,420);

        for (String m : MENU) nav.add(buildMenuItem(m));

        // Logout
        JPanel logout = buildMenuItemColored("↩  Đăng xuất", new Color(0xFCA5A5));
        logout.setBounds(0, 650, 200, 40);
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { confirmLogout(); }
        });

        sb.add(top); sb.add(nav); sb.add(logout);
        return sb;
    }

    private JPanel buildMenuItem(String name) {
        boolean active = name.equals(activeMenu);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                if (active || getClientProperty("hover") == Boolean.TRUE) {
                    g.setColor(active ? new Color(37,99,235,40) : new Color(255,255,255,12));
                    g.fillRect(0,0,getWidth(),getHeight());
                    if (active) { g.setColor(UITheme.PRIMARY); g.fillRect(0,0,3,getHeight()); }
                }
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(200,40));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String icon = switch(name) {
            case "Trang chủ"          -> "⊞";
            case "Thông tin cá nhân"  -> "👤";
            case "Thông tin phòng"    -> "🚪";
            case "Hóa đơn"            -> "🧾";
            case "Lịch sử thanh toán" -> "📋";
            case "Thông báo"          -> "🔔";
            case "Đổi mật khẩu"       -> "🔑";
            default -> "•";
        };
        JLabel ico  = new JLabel(icon);
        JLabel lbl  = new JLabel(name.equals("Thông báo") ? name + " (3)" : name);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(active ? new Color(0x93C5FD) : new Color(255,255,255,150));
        p.add(ico); p.add(lbl);

        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeMenu = name; switchPanel(name); rebuildSidebar();
            }
            @Override public void mouseEntered(MouseEvent e) { p.putClientProperty("hover",Boolean.TRUE); p.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { p.putClientProperty("hover",null); p.repaint(); }
        });
        return p;
    }

    private JPanel buildMenuItemColored(String text, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,16,0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BODY); lbl.setForeground(fg);
        p.add(lbl);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }

    private void rebuildSidebar() {
        getContentPane().remove(((BorderLayout)getContentPane().getLayout()).getLayoutComponent(BorderLayout.WEST));
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    // ── MAIN ──────────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_LIGHT);
        main.add(buildTopHeader(), BorderLayout.NORTH);
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_LIGHT);
        StudentDashboardPanel dashboard = new StudentDashboardPanel(currentUser);
        dashboard.setOnViewAllNotifications(() -> {
            activeMenu = "Thông báo";
            switchPanel("Thông báo");
            rebuildSidebar();
        });
        contentArea.add(dashboard, BorderLayout.CENTER);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UITheme.WHITE); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(UITheme.BORDER); g.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
            }
        };
        h.setPreferredSize(new Dimension(0, UITheme.HEADER_HEIGHT));
        h.setBorder(new EmptyBorder(0,18,0,18));

        JLabel page = new JLabel("Trang chủ sinh viên  ·  Năm học 2024–2025");
        page.setFont(UITheme.FONT_BODY); page.setForeground(UITheme.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);
        JLabel ok = UITheme.badge("✓  Hợp đồng còn hiệu lực", UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);
        JLabel bell = new JLabel("🔔"); bell.setFont(new Font("Segoe UI",Font.PLAIN,18));
        right.add(ok); right.add(bell);

        h.add(page, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── HOME PAGE ─────────────────────────────────────────────────
    private JPanel buildHomePage() {
        JPanel root = new JPanel();
        root.setBackground(UITheme.BG_LIGHT);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(18,18,18,18));

        JLabel greeting = new JLabel("Xin chào, " + firstName(currentUser.getFullName()) + " 👋");
        greeting.setFont(new Font("Segoe UI",Font.BOLD,20));
        greeting.setForeground(UITheme.TEXT_PRIMARY);
        greeting.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Hôm nay có hóa đơn tháng 6 đến hạn ngày 15/06/2026. Vui lòng thanh toán đúng hạn.");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4,0,16,0));

        // Info cards row
        JPanel row1 = new JPanel(new GridLayout(1,2,14,0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        row1.add(buildPersonalCard());
        row1.add(buildRoomContractCard());

        // Invoice card
        JPanel invoiceCard = buildInvoiceCard();
        invoiceCard.setAlignmentX(LEFT_ALIGNMENT);
        invoiceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Notifications
        JLabel notifTitle = new JLabel("THÔNG BÁO MỚI");
        notifTitle.setFont(UITheme.FONT_LABEL); notifTitle.setForeground(UITheme.TEXT_SECONDARY);
        notifTitle.setAlignmentX(LEFT_ALIGNMENT);
        notifTitle.setBorder(new EmptyBorder(14,0,8,0));

        JPanel notifs = buildNotifications();
        notifs.setAlignmentX(LEFT_ALIGNMENT);
        notifs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        root.add(greeting); root.add(sub); root.add(row1);
        root.add(Box.createVerticalStrut(14));
        root.add(invoiceCard);
        root.add(notifTitle); root.add(notifs);
        return root;
    }

    private JPanel buildPersonalCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0,10));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        titleRow.setOpaque(false);
        titleRow.add(new JLabel("👤"));
        JLabel t = new JLabel("THÔNG TIN CÁ NHÂN");
        t.setFont(UITheme.FONT_LABEL); t.setForeground(UITheme.TEXT_SECONDARY);
        titleRow.add(t);
        card.add(titleRow, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(5,2,0,6));
        info.setOpaque(false);
        addInfoRow(info,"Mã sinh viên", currentUser.getStudentId(), true);
        addInfoRow(info,"Trường học",   "ĐHBK Hà Nội", false);
        addInfoRow(info,"Khoa",         "Công nghệ TT", false);
        addInfoRow(info,"Lớp",          "20221234", false);
        addInfoRow(info,"SĐT",          "0912 345 670", false);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomContractCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0,10));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        titleRow.setOpaque(false);
        titleRow.add(new JLabel("🚪"));
        JLabel t = new JLabel("PHÒNG & HỢP ĐỒNG");
        t.setFont(UITheme.FONT_LABEL); t.setForeground(UITheme.TEXT_SECONDARY);
        titleRow.add(t);
        card.add(titleRow, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(5,2,0,6));
        info.setOpaque(false);
        addInfoRow(info,"Phòng ở",       "A301 — Tầng 3", true);
        addInfoRow(info,"Loại phòng",    "8 người (KT chung)", false);
        addInfoRow(info,"HĐ bắt đầu",   "01/09/2024", false);
        addInfoRow(info,"HĐ kết thúc",  "31/08/2025", false);
        addInfoRow(info,"Trạng thái",   "Còn hiệu lực ✓", false);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void addInfoRow(JPanel p, String key, String val, boolean highlight) {
        JLabel k = new JLabel(key); k.setFont(UITheme.FONT_SMALL); k.setForeground(UITheme.TEXT_SECONDARY);
        JLabel v = new JLabel(val); v.setFont(UITheme.FONT_SMALL);
        v.setForeground(highlight ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);
        if (highlight) v.setFont(UITheme.FONT_BOLD);
        p.add(k); p.add(v);
    }

    private JPanel buildInvoiceCard() {
        Invoice inv = DataStore.getInvoices().stream()
            .filter(i -> i.getStudentId().equals(currentUser.getStudentId()))
            .findFirst().orElse(null);

        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0,10));

        // Title row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        left.setOpaque(false);
        left.add(new JLabel("🧾"));
        JLabel t = new JLabel("HÓA ĐƠN THÁNG 6/2026");
        t.setFont(UITheme.FONT_LABEL); t.setForeground(UITheme.TEXT_SECONDARY);
        left.add(t);
        JLabel period = new JLabel("Kỳ: 01/06 – 30/06/2026");
        period.setFont(UITheme.FONT_TINY); period.setForeground(UITheme.TEXT_MUTED);
        topRow.add(left, BorderLayout.WEST);
        topRow.add(period, BorderLayout.CENTER);
        boolean paid = inv == null || inv.isPaid();
        JLabel status = UITheme.badge(paid ? "✓ Đã thanh toán" : "⏳ Chưa thanh toán",
                paid ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
                paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT);
        topRow.add(status, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Fee items
        JPanel feeRow = new JPanel(new GridLayout(1,3,12,0));
        feeRow.setOpaque(false);
        feeRow.add(feeItem("🏠","Tiền phòng",    "850.000 đ", UITheme.PRIMARY));
        feeRow.add(feeItem("⚡","Tiền điện (38kWh)","76.000 đ",UITheme.WARNING));
        feeRow.add(feeItem("💧","Tiền nước (4m³)",  "24.000 đ",new Color(0x0891B2)));
        card.add(feeRow, BorderLayout.CENTER);

        // Total + pay button
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new MatteBorder(1,0,0,0,UITheme.BORDER));
        JPanel totalInfo = new JPanel(new GridLayout(2,1));
        totalInfo.setOpaque(false);
        JLabel totalLbl = new JLabel("Tổng cần thanh toán");
        totalLbl.setFont(UITheme.FONT_SMALL); totalLbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel totalVal = new JLabel("950.000 VNĐ");
        totalVal.setFont(new Font("Segoe UI",Font.BOLD,22)); totalVal.setForeground(UITheme.TEXT_PRIMARY);
        totalInfo.add(totalLbl); totalInfo.add(totalVal);

        JButton btnPay = UITheme.primaryBtn("💳  Thanh toán ngay");
        btnPay.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "<html>Cổng thanh toán đang mở...<br>Tổng tiền: <b>950.000 VNĐ</b></html>",
            "Thanh toán",JOptionPane.INFORMATION_MESSAGE));

        bottom.add(totalInfo, BorderLayout.WEST);
        bottom.add(btnPay,    BorderLayout.EAST);
        bottom.setBorder(new EmptyBorder(8,0,0,0));
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel feeItem(String icon, String label, String amount, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_SECONDARY);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER,1,true), new EmptyBorder(10,12,10,12)));
        JLabel ico = new JLabel(icon); ico.setFont(new Font("Segoe UI",Font.PLAIN,20));
        JLabel amt = new JLabel(amount);
        amt.setFont(new Font("Segoe UI",Font.BOLD,16)); amt.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_TINY); lbl.setForeground(UITheme.TEXT_SECONDARY);
        JPanel info = new JPanel(new GridLayout(2,1));
        info.setOpaque(false); info.add(amt); info.add(lbl);
        p.add(ico, BorderLayout.WEST); p.add(info, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNotifications() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        Object[][] notifs = {
            {"🔴","Hóa đơn tháng 6 đã được tạo, vui lòng thanh toán trước 15/06/2026",    "2 giờ trước",UITheme.DANGER},
            {"🟡","Hợp đồng của bạn sẽ hết hạn 31/08/2025. Liên hệ gia hạn sớm.",         "3 ngày trước",UITheme.WARNING},
            {"🔵","Thông báo: Kiểm tra phòng định kỳ ngày 10/06. Sinh viên ở trong phòng.","1 tuần trước",UITheme.PRIMARY},
        };

        for (Object[] n : notifs) {
            JPanel row = new JPanel(new BorderLayout(10,0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,UITheme.BORDER), new EmptyBorder(8,0,8,0)));
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel ico = new JLabel((String)n[0]);
            JPanel info = new JPanel(new GridLayout(2,1));
            info.setOpaque(false);
            JLabel msg = new JLabel((String)n[1]);
            msg.setFont(UITheme.FONT_SMALL); msg.setForeground(UITheme.TEXT_PRIMARY);
            JLabel time = new JLabel((String)n[2]);
            time.setFont(UITheme.FONT_TINY); time.setForeground(UITheme.TEXT_MUTED);
            info.add(msg); info.add(time);

            row.add(ico, BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            card.add(row);
        }
        return card;
    }

    private void switchPanel(String name) {
        contentArea.removeAll();
        JPanel p = switch(name) {
            case "Trang chủ"           -> {
                StudentDashboardPanel dash = new StudentDashboardPanel(currentUser);
                dash.setOnViewAllNotifications(() -> {
                    activeMenu = "Thông báo";
                    switchPanel("Thông báo");
                    rebuildSidebar();
                });
                yield dash;
            }
            case "Thông tin cá nhân"   -> new StudentProfilePanel(currentUser);
            case "Thông tin phòng"     -> new StudentRoomPanel(currentUser);
            case "Hóa đơn"             -> new StudentInvoicePanel(currentUser);
            case "Lịch sử thanh toán"  -> new StudentPaymentHistoryPanel(currentUser);
            case "Thông báo"           -> new StudentNotificationPanel(currentUser);
            case "Đổi mật khẩu"        -> new ChangePasswordPanel(currentUser);
            default -> {
                JPanel ph = new JPanel(new GridBagLayout());
                ph.setBackground(UITheme.BG_LIGHT);
                JLabel l = new JLabel("<html><center><div style='font-size:36px'>🚧</div><br><b>"+name+"</b><br><span style='color:gray'>Đang phát triển</span></center></html>",SwingConstants.CENTER);
                l.setFont(UITheme.FONT_H2); ph.add(l);
                yield ph;
            }
        };
        contentArea.add(p, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,"Bạn có muốn đăng xuất?","Xác nhận",JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { dispose(); new LoginFrame().setVisible(true); }
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) return String.valueOf(parts[0].charAt(0)) + parts[parts.length-1].charAt(0);
        return fullName.length()>0 ? String.valueOf(fullName.charAt(0)) : "U";
    }

    private String firstName(String fullName) {
        String[] p = fullName.trim().split("\\s+");
        return p[p.length-1];
    }
}
