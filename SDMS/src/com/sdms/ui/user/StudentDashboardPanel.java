package com.sdms.ui.user;

import com.sdms.model.*;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trang chủ sinh viên (Student Dashboard).
 * - Thông báo: lấy từ DB (khi admin tạo vi phạm / hóa đơn), không dùng mẫu cứng.
 * - Card Hợp đồng: có vòng tròn % hiệu lực.
 * - Card Hóa đơn: chỉ hiển thị tháng + tổng tiền; nút bấm chuyển sang trang Hóa đơn.
 */
public class StudentDashboardPanel extends JPanel {

    private final User     currentUser;
    private final Student  student;
    private final Room     room;
    private final Invoice  latestInvoice;
    private final Contract activeContract;
    private final long     violationCount;

    /** Callback sang trang Thông báo */
    private Runnable onViewAllNotifications;
    /** Callback sang trang Hóa đơn */
    private Runnable onNavigateToInvoice;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StudentDashboardPanel(User currentUser) {
        this.currentUser    = currentUser;
        this.student        = findStudent();
        this.room           = findRoom();
        this.latestInvoice  = findLatestInvoice();
        this.activeContract = findActiveContract();
        this.violationCount = countViolations();

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnViewAllNotifications(Runnable cb) { this.onViewAllNotifications = cb; }
    public void setOnNavigateToInvoice(Runnable cb)    { this.onNavigateToInvoice    = cb; }

    // ── Tìm dữ liệu từ DB ────────────────────────────────────────

    private Student findStudent() {
        String sid = currentUser.getStudentId();
        if (sid == null || sid.isEmpty()) return null;
        return DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equals(sid))
            .findFirst().orElse(null);
    }

    private Room findRoom() {
        // ✅ FIX: thêm null check cho getRoomId() tránh NullPointerException
        if (student == null || student.getRoomId() == null || student.getRoomId().isEmpty()) return null;
        return DatabaseService.getAllRooms().stream()
            .filter(r -> r.getId().equals(student.getRoomId()))
            .findFirst().orElse(null);
    }

    private Invoice findLatestInvoice() {
        if (student == null) return null;
        List<Invoice> invoices = DatabaseService.getInvoicesByStudent(student.getId());
        return invoices.stream().filter(i -> !i.isPaid()).findFirst()
            .orElse(invoices.isEmpty() ? null : invoices.get(0));
    }

    private Contract findActiveContract() {
        if (student == null) return null;
        return DatabaseService.getContractsByStudent(student.getId()).stream()
            .filter(c -> c.getStatus() == Contract.Status.ACTIVE)
            .findFirst().orElse(null);
    }

    private long countViolations() {
        if (student == null) return 0;
        return DatabaseService.getViolationsByStudent(student.getId()).size();
    }

    // ── Nội dung chính ───────────────────────────────────────────

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

    // ── Banner chào mừng ─────────────────────────────────────────

    private JPanel buildWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                    0, 0, UITheme.PRIMARY,
                    getWidth(), getHeight(), UITheme.PURPLE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
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

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);

        String svId = student != null ? student.getId() : "—";
        JLabel lblId = new JLabel(svId, SwingConstants.RIGHT);
        lblId.setFont(UITheme.FONT_BOLD);
        lblId.setForeground(new Color(255, 255, 255, 220));

        String statusText = student != null ? student.getStatus() : "—";
        JLabel lblStatus = UITheme.badge("● " + statusText,
            new Color(255, 255, 255, 40), Color.WHITE);
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

        // Card 1: Hóa đơn
        long total = latestInvoice != null ? latestInvoice.getTotal() : 0;
        boolean paid = latestInvoice != null && latestInvoice.isPaid();
        row.add(summaryCard("🧾 Hóa đơn tháng này",
            latestInvoice != null ? String.format("%,d đ", total) : "—",
            paid ? "✅ Đã thanh toán" : (latestInvoice == null ? "Không có hóa đơn" : "⏳ Chưa thanh toán"),
            paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT,
            paid ? UITheme.SUCCESS_BG   : UITheme.WARNING_BG));

        // Card 2: Phòng
        String roomId   = room != null ? room.getId()   : "—";
        String roomType = room != null ? room.getType() : "Chưa có phòng";
        row.add(summaryCard("🛏 Phòng đang ở", roomId, roomType,
            UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));

        // Card 3: Hợp đồng (có vòng tròn %)
        row.add(buildContractCard());

        // Card 4: Vi phạm
        row.add(summaryCard("⚠ Vi phạm",
            violationCount + " lần", "Trong học kỳ này",
            violationCount > 0 ? UITheme.DANGER      : UITheme.TEXT_SECONDARY,
            violationCount > 0 ? UITheme.DANGER_BG   : UITheme.BG_SECONDARY));

        return row;
    }

    /** Card hợp đồng với vòng tròn % thời gian đã ở */
    private JPanel buildContractCard() {
        boolean hasContract = activeContract != null;
        int     percent     = hasContract ? activeContract.getElapsedPercent() : 0;
        String  valueText   = hasContract ? "Còn hiệu lực" : "Không có";
        String  subText     = hasContract ? "Hết hạn: " + activeContract.getEndDateStr() : "Liên hệ BQL";
        Color   accent      = hasContract ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT;
        Color   bg          = hasContract ? UITheme.SUCCESS_BG   : UITheme.WARNING_BG;

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel("📄 Hợp đồng");
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JPanel centerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        centerRow.setOpaque(false);

        final int   fp = percent;
        final Color fa = accent;
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), pad = 4;
                g2.setColor(new Color(220, 220, 220));
                g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(pad, pad, w - 2 * pad, h - 2 * pad, 0, 360);
                g2.setColor(fa);
                int sweep = (int) (fp * 360 / 100.0);
                g2.drawArc(pad, pad, w - 2 * pad, h - 2 * pad, 90, -sweep);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(fa);
                String txt = fp + "%";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt,
                    (w - fm.stringWidth(txt)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(46, 46); }
        };
        circle.setOpaque(false);

        JLabel lblValue = new JLabel(valueText);
        lblValue.setFont(UITheme.FONT_H2);
        lblValue.setForeground(accent);

        centerRow.add(circle);
        centerRow.add(lblValue);

        JLabel lblSub = UITheme.badge(subText, bg, accent);

        card.add(lblTitle,  BorderLayout.NORTH);
        card.add(centerRow, BorderLayout.CENTER);
        card.add(lblSub,    BorderLayout.SOUTH);
        return card;
    }

    /** Card tóm tắt thông thường (không có vòng tròn) */
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

    // ── Hàng dưới: Hóa đơn + Thông báo ─────────────────────────

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));
        row.add(buildInvoiceCard());
        row.add(buildNotificationCard());
        return row;
    }

    private JPanel buildInvoiceCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("🧾 Hóa đơn tháng hiện tại");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        String month = latestInvoice != null ? latestInvoice.getMonth() : "—";
        JLabel lblMonth = UITheme.badge("Tháng " + month, UITheme.INFO_BG, UITheme.INFO_TEXT);
        header.add(title,    BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.EAST);

        long    totalAmt = latestInvoice != null ? latestInvoice.getTotal() : 0;
        boolean paid     = latestInvoice != null && latestInvoice.isPaid();

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel lblTotal = new JLabel(
            latestInvoice != null ? String.format("%,d đ", totalAmt) : "Không có hóa đơn"
        );
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTotal.setForeground(paid ? UITheme.SUCCESS_TEXT : UITheme.PRIMARY);
        centerPanel.add(lblTotal);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);

        JLabel lblStatus = UITheme.badge(
            paid ? "✅ Đã thanh toán"
                 : (latestInvoice == null ? "Không có hóa đơn" : "⏳ Chưa thanh toán"),
            paid ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
            paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT
        );

        JButton btnPay = UITheme.primaryBtn(paid ? "✓ Xem hóa đơn" : "💳 Thanh toán ngay");
        btnPay.setEnabled(latestInvoice != null);
        btnPay.addActionListener(e -> {
            if (onNavigateToInvoice != null) onNavigateToInvoice.run();
        });

        bottom.add(lblStatus, BorderLayout.WEST);
        bottom.add(btnPay,    BorderLayout.EAST);

        card.add(header,      BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottom,      BorderLayout.SOUTH);
        return card;
    }

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

        String sid    = student != null ? student.getId()     : "";
        String roomId = student != null ? student.getRoomId() : "";
        List<Notification> dbNotices = sid.isEmpty()
            ? java.util.Collections.emptyList()
            : DatabaseService.getNotificationsForStudent(sid, roomId)
                .stream().limit(4).collect(Collectors.toList());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        if (dbNotices.isEmpty()) {
            JLabel empty = new JLabel("Chưa có thông báo nào.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (Notification n : dbNotices) {
                listPanel.add(noticeItem(
                    n.getTypeIcon(),
                    n.getTitle(),
                    n.getRelativeTime(),
                    !n.isRead()
                ));
                listPanel.add(Box.createVerticalStrut(6));
            }
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

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Chào buổi sáng";
        if (hour < 18) return "Chào buổi chiều";
        return "Chào buổi tối";
    }
}