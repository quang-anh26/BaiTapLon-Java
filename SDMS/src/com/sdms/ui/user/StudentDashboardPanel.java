package com.sdms.ui.user;

import com.sdms.model.*;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Trang chủ của cổng sinh viên (Student Dashboard).
 * Hiển thị: Chào mừng | Thông tin tóm tắt | Hóa đơn tháng | Thông báo mới nhất.
 */
public class StudentDashboardPanel extends JPanel {

    private final User    currentUser;
    private final Student student;    // Thông tin sinh viên đăng nhập
    private final Room    room;       // Phòng đang ở
    private final Invoice latestInvoice; // Hóa đơn mới nhất

    /** Callback để điều hướng sang trang Thông báo khi bấm "Xem tất cả" */
    private Runnable onViewAllNotifications;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StudentDashboardPanel(User currentUser) {
        this.currentUser   = currentUser;
        this.student       = findStudent();
        this.room          = findRoom();
        this.latestInvoice = findLatestInvoice();

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    /** Đặt callback khi sinh viên bấm "Xem tất cả thông báo" */
    public void setOnViewAllNotifications(Runnable callback) {
        this.onViewAllNotifications = callback;
    }

    // ── Tìm thông tin sinh viên từ DataStore ─────────────────────
    private Student findStudent() {
        String sid = currentUser.getStudentId();
        if (sid == null) return null;
        return DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equals(sid))
            .findFirst().orElse(null);
    }

    // ── Tìm phòng của sinh viên ───────────────────────────────────
    private Room findRoom() {
        if (student == null || student.getRoomId().isEmpty()) return null;
        return DatabaseService.getAllRooms().stream()
            .filter(r -> r.getId().equals(student.getRoomId()))
            .findFirst().orElse(null);
    }

    // ── Tìm hóa đơn mới nhất của sinh viên ───────────────────────
    private Invoice findLatestInvoice() {
        if (student == null) return null;
        List<Invoice> invoices = DatabaseService.getAllInvoices();
        return invoices.stream()
            .filter(i -> i.getStudentId().equals(student.getId()))
            .reduce((a, b) -> b) // lấy cuối cùng
            .orElse(null);
    }

    // ── Nội dung chính ────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_LIGHT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        p.add(buildWelcomeBanner());
        p.add(Box.createVerticalStrut(18));
        p.add(buildSummaryCards());
        p.add(Box.createVerticalStrut(18));
        p.add(buildBottomRow());

        return p;
    }

    // ── Banner chào mừng ──────────────────────────────────────────
    private JPanel buildWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nền từ PRIMARY → PURPLE
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY,
                    getWidth(), getHeight(), UITheme.PURPLE
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Vòng trang trí mờ
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 140, -40, 200, 200);
                g2.fillOval(getWidth() - 60, getHeight() - 60, 100, 100);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        banner.setAlignmentX(LEFT_ALIGNMENT);

        // Nội dung bên trái
        String name  = student != null ? student.getFullName() : currentUser.getFullName();
        String greet = getGreeting();

        JLabel lblGreet = new JLabel(greet + ", " + name + " 👋");
        lblGreet.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGreet.setForeground(Color.WHITE);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy",
            new java.util.Locale("vi", "VN")));
        JLabel lblDate = new JLabel("📅 " + today);
        lblDate.setFont(UITheme.FONT_SMALL);
        lblDate.setForeground(new Color(255, 255, 255, 200));

        String roomInfo = room != null
            ? "🛏 Phòng " + room.getId() + "  |  " + room.getType()
            : "🛏 Chưa có phòng";
        JLabel lblRoom = new JLabel(roomInfo);
        lblRoom.setFont(UITheme.FONT_SMALL);
        lblRoom.setForeground(new Color(255, 255, 255, 180));

        JPanel left = new JPanel(new GridLayout(3, 1, 0, 4));
        left.setOpaque(false);
        left.add(lblGreet);
        left.add(lblDate);
        left.add(lblRoom);

        // Mã SV + trạng thái bên phải
        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);

        String svId = student != null ? student.getId() : "—";
        JLabel lblId = new JLabel(svId, SwingConstants.RIGHT);
        lblId.setFont(UITheme.FONT_BOLD);
        lblId.setForeground(new Color(255, 255, 255, 220));

        String statusText = student != null ? student.getStatus() : "—";
        JLabel lblStatus = UITheme.badge("● " + statusText,
            new Color(255,255,255,40), Color.WHITE);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        right.add(lblId,     BorderLayout.NORTH);
        right.add(lblStatus, BorderLayout.SOUTH);

        banner.add(left,  BorderLayout.CENTER);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    // ── 4 Card tóm tắt ───────────────────────────────────────────
    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Card 1: Hóa đơn tháng này
        long total = latestInvoice != null ? latestInvoice.getTotal() : 0;
        boolean paid = latestInvoice != null && latestInvoice.isPaid();
        row.add(summaryCard("🧾 Hóa đơn tháng này",
            String.format("%,d đ", total),
            paid ? "✅ Đã thanh toán" : "⏳ Chưa thanh toán",
            paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT,
            paid ? UITheme.SUCCESS_BG   : UITheme.WARNING_BG));

        // Card 2: Phòng đang ở
        String roomId   = room != null ? room.getId() : "—";
        String roomType = room != null ? room.getType() : "Chưa có phòng";
        row.add(summaryCard("🛏 Phòng đang ở",
            roomId, roomType,
            UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));

        // Card 3: Hợp đồng
        row.add(summaryCard("📄 Hợp đồng",
            "Còn hiệu lực",
            "Hết hạn: 31/08/2026",
            UITheme.SUCCESS_TEXT, UITheme.SUCCESS_BG));

        // Card 4: Vi phạm
        row.add(summaryCard("⚠ Vi phạm",
            "0 lần",
            "Trong học kỳ này",
            UITheme.TEXT_SECONDARY, UITheme.BG_SECONDARY));

        return row;
    }

    /** Card tóm tắt nhỏ */
    private JPanel summaryCard(String title, String value, String sub,
                                Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_H2);
        lblValue.setForeground(accent);

        JLabel lblSub = UITheme.badge(sub, bg, accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub,   BorderLayout.SOUTH);
        return card;
    }

    // ── Hàng dưới: Hóa đơn + Thông báo ──────────────────────────
    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        row.add(buildInvoiceCard());
        row.add(buildNotificationCard());
        return row;
    }

    // ── Card hóa đơn chi tiết ─────────────────────────────────────
    private JPanel buildInvoiceCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        // Header card
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("🧾 Hóa đơn tháng hiện tại");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        String month = latestInvoice != null ? latestInvoice.getMonth() : "06/2026";
        JLabel lblMonth = UITheme.badge("Tháng " + month,
            UITheme.INFO_BG, UITheme.INFO_TEXT);
        header.add(title,    BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.EAST);

        // Nội dung chi tiết
        JPanel detail = new JPanel(new GridLayout(0, 2, 0, 8));
        detail.setOpaque(false);

        long roomFee  = latestInvoice != null ? latestInvoice.getRoomFee()     : 850_000;
        long elecFee  = latestInvoice != null ? latestInvoice.getElectricFee() : 76_000;
        long waterFee = latestInvoice != null ? latestInvoice.getWaterFee()    : 0;
        long total    = latestInvoice != null ? latestInvoice.getTotal()       : 926_000;
        boolean paid  = latestInvoice != null && latestInvoice.isPaid();

        addInvoiceRow(detail, "Tiền phòng:",      String.format("%,d đ", roomFee),  false);
        addInvoiceRow(detail, "Tiền điện:",        String.format("%,d đ", elecFee),  false);
        addInvoiceRow(detail, "Tiền nước:",        String.format("%,d đ", waterFee), false);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);

        addInvoiceRow(detail, "Tổng cộng:",
            String.format("%,d đ", total), true); // bold

        // Trạng thái + nút thanh toán
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);

        JLabel lblStatus = UITheme.badge(
            paid ? "✅ Đã thanh toán" : "⏳ Chưa thanh toán",
            paid ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
            paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT
        );

        JButton btnPay = UITheme.primaryBtn(paid ? "✓ Đã thanh toán" : "💳 Thanh toán ngay");
        btnPay.setEnabled(!paid);
        btnPay.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "<html>✅ Thanh toán thành công!<br>Số tiền: "
                + String.format("%,d đ", total) + "</html>",
                "Thanh toán", JOptionPane.INFORMATION_MESSAGE);
        });

        bottom.add(lblStatus, BorderLayout.WEST);
        bottom.add(btnPay,    BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(detail, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    /** Thêm một hàng label:value vào panel chi tiết hóa đơn */
    private void addInvoiceRow(JPanel p, String label, String value, boolean bold) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(bold ? UITheme.FONT_BOLD : UITheme.FONT_BODY);
        lbl.setForeground(bold ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value, SwingConstants.RIGHT);
        val.setFont(bold ? UITheme.FONT_BOLD : UITheme.FONT_BODY);
        val.setForeground(bold ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);

        p.add(lbl);
        p.add(val);
    }

    // ── Card thông báo mới nhất ───────────────────────────────────
    private JPanel buildNotificationCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel title = new JLabel("🔔 Thông báo mới nhất");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Danh sách thông báo mẫu
        Object[][] notices = {
            {"🔴", "Hóa đơn tháng 06/2026 đã được tạo. Vui lòng thanh toán trước 15/06.",    "1 giờ trước",  true},
            {"📄", "Hợp đồng thuê phòng của bạn còn 82 ngày. Liên hệ BQL để gia hạn.",        "2 ngày trước", true},
            {"🔍", "Lịch kiểm tra phòng định kỳ: 10/06/2026 lúc 09:00.",                      "3 ngày trước", false},
            {"🔵", "Nhắc nhở: Tổng vệ sinh khu vực chung vào thứ 7 tuần này.",                "5 ngày trước", false},
        };

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (Object[] n : notices) {
            listPanel.add(noticeItem(
                (String)  n[0],
                (String)  n[1],
                (String)  n[2],
                (Boolean) n[3]
            ));
            listPanel.add(Box.createVerticalStrut(6));
        }

        JButton btnAll = UITheme.outlineBtn("Xem tất cả thông báo →");
        btnAll.setAlignmentX(LEFT_ALIGNMENT);
        btnAll.addActionListener(e -> {
            if (onViewAllNotifications != null) onViewAllNotifications.run();
        });

        card.add(title,     BorderLayout.NORTH);
        card.add(listPanel, BorderLayout.CENTER);
        card.add(btnAll,    BorderLayout.SOUTH);
        return card;
    }

    /** Một dòng thông báo */
    private JPanel noticeItem(String icon, String text, String time, boolean unread) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        p.setBackground(unread ? new Color(0xEFF6FF) : UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(unread ? new Color(0xBFDBFE) : UITheme.BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcon.setPreferredSize(new Dimension(26, 26));

        JLabel lblText = new JLabel("<html><body style='width:220px'>" + text + "</body></html>");
        lblText.setFont(unread ? UITheme.FONT_BOLD : UITheme.FONT_BODY);
        lblText.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblTime = new JLabel(time);
        lblTime.setFont(UITheme.FONT_TINY);
        lblTime.setForeground(UITheme.TEXT_MUTED);
        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTime.setPreferredSize(new Dimension(80, 20));

        p.add(lblIcon, BorderLayout.WEST);
        p.add(lblText, BorderLayout.CENTER);
        p.add(lblTime, BorderLayout.EAST);
        return p;
    }

    // ── Tiện ích ─────────────────────────────────────────────────

    /** Lời chào theo giờ hiện tại */
    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Chào buổi sáng";
        if (hour < 18) return "Chào buổi chiều";
        return "Chào buổi tối";
    }
}
