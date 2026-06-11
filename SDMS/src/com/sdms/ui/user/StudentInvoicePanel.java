package com.sdms.ui.user;

import com.sdms.model.Invoice;
import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel xem hóa đơn tháng hiện tại của sinh viên.
 * Hiển thị: Chi tiết hóa đơn | Thanh toán | Hướng dẫn.
 */
public class StudentInvoicePanel extends JPanel {

    private final User    currentUser;
    private final Student student;
    private       Invoice currentInvoice; // Hóa đơn tháng mới nhất chưa trả

    public StudentInvoicePanel(User currentUser) {
        this.currentUser    = currentUser;
        this.student        = findStudent();
        this.currentInvoice = findCurrentInvoice();

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
    }

    private Student findStudent() {
        String sid = currentUser.getStudentId();
        if (sid == null) return null;
        return 	DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equals(sid))
            .findFirst().orElse(null);
    }

    private Invoice findCurrentInvoice() {
        if (student == null) return null;
        // Lấy hóa đơn mới nhất (chưa thanh toán ưu tiên)
        List<Invoice> list = DatabaseService.getAllInvoices().stream()
            .filter(i -> i.getStudentId().equals(student.getId()))
            .collect(Collectors.toList());
        // Ưu tiên hóa đơn chưa trả
        return list.stream().filter(i -> !i.isPaid()).findFirst()
            .orElse(list.isEmpty() ? null : list.get(list.size() - 1));
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("🧾  Hóa đơn tháng này");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Hóa đơn");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        String month = currentInvoice != null ? currentInvoice.getMonth() : "06/2026";
        JLabel lblMonth = UITheme.badge("Tháng " + month,
            UITheme.INFO_BG, UITheme.INFO_TEXT);

        p.add(left,     BorderLayout.WEST);
        p.add(lblMonth, BorderLayout.EAST);
        return p;
    }

    // ── Nội dung chính ────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(UITheme.BG_LIGHT);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        if (currentInvoice == null) {
            content.add(buildNoInvoiceCard());
        } else {
            // Hàng trên: trạng thái lớn + tóm tắt
            content.add(buildStatusBanner());
            content.add(Box.createVerticalStrut(16));
            // Hàng giữa: chi tiết + hướng dẫn
            JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
            middleRow.setOpaque(false);
            middleRow.setAlignmentX(LEFT_ALIGNMENT);
            middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));
            middleRow.add(buildDetailCard());
            middleRow.add(buildPaymentGuideCard());
            content.add(middleRow);
        }
        return content;
    }

    // ── Banner trạng thái lớn ─────────────────────────────────────
    private JPanel buildStatusBanner() {
        boolean paid = currentInvoice.isPaid();
        Color   bg   = paid ? UITheme.SUCCESS_BG  : UITheme.WARNING_BG;
        Color   fg   = paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT;
        Color   border = paid ? new Color(0x6EE7B7) : new Color(0xFCD34D);

        JPanel banner = new JPanel(new BorderLayout(16, 0));
        banner.setBackground(bg);
        banner.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));
        banner.setAlignmentX(LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Icon lớn
        JLabel icon = new JLabel(paid ? "✅" : "⏳");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        // Nội dung
        JLabel lblStatus = new JLabel(paid ? "Đã thanh toán" : "Chưa thanh toán");
        lblStatus.setFont(UITheme.FONT_H2);
        lblStatus.setForeground(fg);

        String dueInfo = paid
            ? "Cảm ơn bạn đã thanh toán đúng hạn!"
            : "Hạn thanh toán: 15/" + currentInvoice.getMonth() + ". Vui lòng thanh toán đúng hạn.";
        JLabel lblDue = new JLabel(dueInfo);
        lblDue.setFont(UITheme.FONT_SMALL);
        lblDue.setForeground(fg);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(lblStatus);
        textPanel.add(lblDue);

        // Tổng tiền + nút
        JLabel lblTotal = new JLabel(String.format("%,d đ", currentInvoice.getTotal()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(fg);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton btnPay = UITheme.primaryBtn(paid ? "✓ Đã thanh toán" : "💳 Thanh toán ngay");
        btnPay.setEnabled(!paid);
        btnPay.addActionListener(e -> processPayment());

        JPanel rightPanel = new JPanel(new BorderLayout(0, 6));
        rightPanel.setOpaque(false);
        rightPanel.add(lblTotal, BorderLayout.CENTER);
        rightPanel.add(btnPay,   BorderLayout.SOUTH);

        banner.add(icon,       BorderLayout.WEST);
        banner.add(textPanel,  BorderLayout.CENTER);
        banner.add(rightPanel, BorderLayout.EAST);
        return banner;
    }

    // ── Card chi tiết hóa đơn ─────────────────────────────────────
    private JPanel buildDetailCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("📋  Chi tiết hóa đơn");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Thông tin hóa đơn
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 10));
        info.setOpaque(false);

        addDetailRow(info, "Mã hóa đơn",    currentInvoice.getId(),         false);
        addDetailRow(info, "Tháng",          "Tháng " + currentInvoice.getMonth(), false);
        addDetailRow(info, "Phòng",          currentInvoice.getRoomId(),     false);
        addDetailRow(info, "Tên sinh viên",  currentInvoice.getStudentName(),false);

        // Separator
        JSeparator sep1 = new JSeparator(); sep1.setForeground(UITheme.BORDER);

        // Chi tiết các khoản
        JPanel items = new JPanel(new GridLayout(0, 2, 8, 8));
        items.setOpaque(false);

        addDetailRow(items, "🏠 Tiền phòng",
            fmt(currentInvoice.getRoomFee()), false);
        addDetailRow(items, "⚡ Tiền điện",
            fmt(currentInvoice.getElectricFee()), false);
        addDetailRow(items, "💧 Tiền nước",
            fmt(currentInvoice.getWaterFee()), false);

        // Separator
        JSeparator sep2 = new JSeparator(); sep2.setForeground(UITheme.BORDER);

        JPanel totalRow = new JPanel(new GridLayout(1, 2, 8, 0));
        totalRow.setOpaque(false);
        JLabel lblTotalKey = new JLabel("💰 TỔNG CỘNG");
        lblTotalKey.setFont(UITheme.FONT_BOLD);
        lblTotalKey.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblTotalVal = new JLabel(fmt(currentInvoice.getTotal()), SwingConstants.RIGHT);
        lblTotalVal.setFont(UITheme.FONT_BOLD);
        lblTotalVal.setForeground(UITheme.PRIMARY);
        totalRow.add(lblTotalKey);
        totalRow.add(lblTotalVal);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(info);
        body.add(Box.createVerticalStrut(10));
        body.add(sep1);
        body.add(Box.createVerticalStrut(10));
        body.add(items);
        body.add(Box.createVerticalStrut(8));
        body.add(sep2);
        body.add(Box.createVerticalStrut(8));
        body.add(totalRow);

        card.add(title, BorderLayout.NORTH);
        card.add(body,  BorderLayout.CENTER);
        return card;
    }

    private void addDetailRow(JPanel p, String label, String value, boolean bold) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value, SwingConstants.RIGHT);
        val.setFont(bold ? UITheme.FONT_BOLD : UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT_PRIMARY);

        p.add(lbl);
        p.add(val);
    }

    // ── Card hướng dẫn thanh toán ─────────────────────────────────
    private JPanel buildPaymentGuideCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("💳  Hướng dẫn thanh toán");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Phương thức 1: Chuyển khoản
        body.add(payMethod("🏦 Chuyển khoản ngân hàng",
            new String[][]{
                {"Ngân hàng", "Vietcombank"},
                {"Số tài khoản", "1234567890"},
                {"Chủ TK", "KTX ĐẠI HỌC SDMS"},
                {"Nội dung CK", student != null
                    ? student.getId() + " " + currentInvoice.getMonth()
                    : currentInvoice.getId()},
            }));

        body.add(Box.createVerticalStrut(12));

        // Phương thức 2: Nộp trực tiếp
        body.add(payMethod("🏢 Nộp tiền mặt tại văn phòng",
            new String[][]{
                {"Địa điểm", "Phòng BQL — Tầng 1 Nhà A"},
                {"Giờ làm việc", "7:30 – 17:00 (Thứ 2 – Thứ 6)"},
                {"Mang theo", "Thẻ SV + phiếu báo hóa đơn"},
            }));

        body.add(Box.createVerticalStrut(12));

        // Lưu ý hạn thanh toán
        JLabel warn = new JLabel(
            "<html>⚠  <b>Lưu ý:</b> Hạn thanh toán là ngày <b>15</b> hàng tháng. "
            + "Quá hạn sẽ bị phạt 2% mỗi ngày trễ.</html>");
        warn.setFont(UITheme.FONT_TINY);
        warn.setForeground(UITheme.DANGER);
        warn.setOpaque(true);
        warn.setBackground(UITheme.DANGER_BG);
        warn.setBorder(new CompoundBorder(
            new LineBorder(new Color(0xFCA5A5), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        body.add(warn);

        // Nút in hóa đơn
        JButton btnPrint = UITheme.outlineBtn("🖨 In hóa đơn");
        btnPrint.setAlignmentX(LEFT_ALIGNMENT);
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "✅ Đã gửi lệnh in hóa đơn!", "In hóa đơn",
            JOptionPane.INFORMATION_MESSAGE));
        body.add(Box.createVerticalStrut(10));
        body.add(btnPrint);

        card.add(title, BorderLayout.NORTH);
        card.add(body,  BorderLayout.CENTER);
        return card;
    }

    /** Tạo block một phương thức thanh toán */
    private JPanel payMethod(String name, String[][] fields) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(name);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JPanel grid = new JPanel(new GridLayout(0, 2, 6, 4));
        grid.setOpaque(true);
        grid.setBackground(UITheme.BG_SECONDARY);
        grid.setBorder(new EmptyBorder(8, 10, 8, 10));

        for (String[] field : fields) {
            JLabel key = new JLabel(field[0] + ":");
            key.setFont(UITheme.FONT_TINY);
            key.setForeground(UITheme.TEXT_MUTED);

            JLabel val = new JLabel(field[1]);
            val.setFont(UITheme.FONT_BOLD);
            val.setForeground(UITheme.TEXT_PRIMARY);

            grid.add(key);
            grid.add(val);
        }

        p.add(lbl,  BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    // ── Không có hóa đơn ─────────────────────────────────────────
    private JPanel buildNoInvoiceCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel icon = new JLabel("🧾", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel msg  = new JLabel("Không có hóa đơn nào cho tháng này.", SwingConstants.CENTER);
        msg.setFont(UITheme.FONT_BODY);
        msg.setForeground(UITheme.TEXT_SECONDARY);

        JPanel c = new JPanel(new GridLayout(2, 1, 0, 10));
        c.setOpaque(false);
        c.add(icon);
        c.add(msg);

        card.add(c, BorderLayout.CENTER);
        return card;
    }

    // ── Thanh toán ────────────────────────────────────────────────
    private void processPayment() {
        if (currentInvoice == null || currentInvoice.isPaid()) return;

        String[] methods = {"Chuyển khoản ngân hàng", "Tiền mặt tại văn phòng"};
        String method = (String) JOptionPane.showInputDialog(this,
            "<html>Xác nhận thanh toán hóa đơn tháng <b>"
            + currentInvoice.getMonth() + "</b><br>"
            + "Số tiền: <b>" + fmt(currentInvoice.getTotal()) + "</b><br><br>"
            + "Phương thức thanh toán:</html>",
            "Xác nhận thanh toán",
            JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);

        if (method != null) {
            currentInvoice.setPaid(true);
            JOptionPane.showMessageDialog(this,
                "<html>✅ <b>Thanh toán thành công!</b><br>"
                + "Số tiền: " + fmt(currentInvoice.getTotal()) + "<br>"
                + "Phương thức: " + method + "</html>",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            // Refresh giao diện
            removeAll();
            setLayout(new BorderLayout());
            add(buildHeader(), BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(buildContent());
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(12);
            add(scroll, BorderLayout.CENTER);
            revalidate(); repaint();
        }
    }

    private String fmt(long amount) {
        return String.format("%,d đ", amount);
    }
}
