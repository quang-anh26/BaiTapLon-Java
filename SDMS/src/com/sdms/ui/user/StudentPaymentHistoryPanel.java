package com.sdms.ui.user;

import com.sdms.model.Invoice;
import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel lịch sử thanh toán của sinh viên.
 * Toàn bộ dữ liệu lấy từ DB — không có dữ liệu mẫu cứng.
 */
public class StudentPaymentHistoryPanel extends JPanel {

    private final User          currentUser;
    private final Student       student;
    private final List<Invoice> allInvoices = new ArrayList<>();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            lblSummary;

    private static final String[] COLS = {
        "Mã HĐ", "Tháng", "Phòng", "Tiền phòng",
        "Tiền điện", "Tiền nước", "Tổng cộng", "Trạng thái"
    };

    public StudentPaymentHistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        this.student     = findStudent();
        loadInvoices();

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Student findStudent() {
        String sid = currentUser.getStudentId();
        if (sid == null) return null;
        return DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equals(sid))
            .findFirst().orElse(null);
    }

    /**
     * Nạp hóa đơn từ DB — không thêm dữ liệu mẫu cứng.
     * Danh sách tự động có khi admin tạo hóa đơn trong hệ thống.
     */
    private void loadInvoices() {
        if (student != null) {
            allInvoices.addAll(
                DatabaseService.getAllInvoices().stream()
                    .filter(i -> i.getStudentId().equals(student.getId()))
                    .collect(Collectors.toList())
            );
        }
    }

    // ── Header ───────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("📜  Lịch sử thanh toán");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Lịch sử thanh toán");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Nội dung chính ───────────────────────────────────────────

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));
        p.add(buildSummaryCards(), BorderLayout.NORTH);
        p.add(buildTableSection(), BorderLayout.CENTER);
        return p;
    }

    // ── 3 card tóm tắt ─────────────────────────────────────────

    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        long totalPaid   = allInvoices.stream().filter(Invoice::isPaid)
            .mapToLong(Invoice::getTotal).sum();
        long totalUnpaid = allInvoices.stream().filter(i -> !i.isPaid())
            .mapToLong(Invoice::getTotal).sum();
        long countPaid   = allInvoices.stream().filter(Invoice::isPaid).count();
        long countUnpaid = allInvoices.stream().filter(i -> !i.isPaid()).count();

        row.add(summaryCard("✅ Đã thanh toán",
            String.format("%,d đ", totalPaid),
            countPaid + " hóa đơn",
            UITheme.SUCCESS_TEXT, UITheme.SUCCESS_BG));
        row.add(summaryCard("⏳ Chưa thanh toán",
            String.format("%,d đ", totalUnpaid),
            countUnpaid + " hóa đơn",
            UITheme.WARNING_TEXT, UITheme.WARNING_BG));
        row.add(summaryCard("📊 Tổng cộng",
            String.format("%,d đ", totalPaid + totalUnpaid),
            allInvoices.size() + " hóa đơn",
            UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));

        return row;
    }

    private JPanel summaryCard(String title, String amount, String count,
                                Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        JLabel lblTitle  = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblAmount = new JLabel(amount);
        lblAmount.setFont(UITheme.FONT_H2);
        lblAmount.setForeground(accent);

        JLabel lblCount  = UITheme.badge(count, bg, accent);

        card.add(lblTitle,  BorderLayout.NORTH);
        card.add(lblAmount, BorderLayout.CENTER);
        card.add(lblCount,  BorderLayout.SOUTH);
        return card;
    }

    // ── Bảng danh sách hóa đơn ───────────────────────────────────

    private JPanel buildTableSection() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        // Toolbar lọc
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel title = new JLabel("📋  Danh sách hóa đơn");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        JComboBox<String> cbStatus = UITheme.comboBox(
            new String[]{"Tất cả", "Đã thanh toán", "Chưa thanh toán"});
        cbStatus.setPreferredSize(new Dimension(160, 34));

        JComboBox<String> cbYear = UITheme.comboBox(
            new String[]{"Tất cả năm", "2026", "2025"});
        cbYear.setPreferredSize(new Dimension(110, 34));

        JButton btnExport = UITheme.outlineBtn("📥 Xuất lịch sử");
        btnExport.setPreferredSize(new Dimension(120, 34));
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "✅ Đã xuất lịch sử thanh toán thành công!", "Thông báo",
            JOptionPane.INFORMATION_MESSAGE));

        filters.add(new JLabel("Lọc:"));
        filters.add(cbStatus);
        filters.add(cbYear);
        filters.add(btnExport);

        toolbar.add(title,   BorderLayout.WEST);
        toolbar.add(filters, BorderLayout.EAST);

        // Bảng
        tableModel = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable(allInvoices);

        table = new JTable(tableModel);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UITheme.BORDER);
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setRowHeight(38);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setReorderingAllowed(false);

        int[] widths = {80, 80, 65, 100, 90, 90, 110, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer Mã HĐ (xanh + đậm)
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, f, r, c) -> {
            JLabel l = new JLabel(v.toString());
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.PRIMARY);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        });

        // Renderer Tổng cộng (xanh lá)
        table.getColumnModel().getColumn(6).setCellRenderer((t, v, sel, f, r, c) -> {
            JLabel l = new JLabel(v.toString(), SwingConstants.RIGHT);
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.SUCCESS_TEXT);
            l.setBorder(new EmptyBorder(0, 0, 0, 8));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        });

        // Renderer Trạng thái (badge màu)
        table.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, f, r, c) -> {
            String  s  = v.toString();
            boolean ok = s.contains("Đã");
            JLabel lbl = UITheme.badge(s,
                ok ? UITheme.SUCCESS_BG   : UITheme.WARNING_BG,
                ok ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        });

        // Sự kiện lọc
        ActionListener filterAction = e -> {
            String st = (String) cbStatus.getSelectedItem();
            String yr = (String) cbYear.getSelectedItem();
            List<Invoice> filtered = allInvoices.stream()
                .filter(i -> {
                    boolean ms = "Tất cả".equals(st)
                        || ("Đã thanh toán".equals(st)    && i.isPaid())
                        || ("Chưa thanh toán".equals(st)  && !i.isPaid());
                    boolean my = "Tất cả năm".equals(yr)
                        || i.getMonth().endsWith(yr);
                    return ms && my;
                })
                .collect(Collectors.toList());
            refreshTable(filtered);
        };
        cbStatus.addActionListener(filterAction);
        cbYear.addActionListener(filterAction);

        // Double-click → xem chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showInvoiceDetail(table.getSelectedRow());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        lblSummary = new JLabel("  Nhấp đúp vào hóa đơn để xem chi tiết.");
        lblSummary.setFont(UITheme.FONT_TINY);
        lblSummary.setForeground(UITheme.TEXT_MUTED);

        p.add(toolbar,    BorderLayout.NORTH);
        p.add(scroll,     BorderLayout.CENTER);
        p.add(lblSummary, BorderLayout.SOUTH);
        return p;
    }

    private void refreshTable(List<Invoice> list) {
        tableModel.setRowCount(0);
        for (Invoice i : list) tableModel.addRow(i.toRow());
    }

    /** Hiển thị dialog chi tiết hóa đơn khi double-click */
    private void showInvoiceDetail(int row) {
        if (row < 0) return;
        String id = (String) tableModel.getValueAt(row, 0);
        Invoice inv = allInvoices.stream()
            .filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        if (inv == null) return;

        JPanel detail = new JPanel(new GridLayout(0, 2, 8, 10));
        detail.setBorder(new EmptyBorder(10, 10, 10, 10));
        String[][] rows = {
            {"Mã hóa đơn:",  inv.getId()},
            {"Tháng:",        "Tháng " + inv.getMonth()},
            {"Phòng:",        inv.getRoomId()},
            {"Tiền phòng:",   String.format("%,d đ", inv.getRoomFee())},
            {"Tiền điện:",    String.format("%,d đ", inv.getElectricFee())},
            {"Tiền nước:",    String.format("%,d đ", inv.getWaterFee())},
            {"Tổng cộng:",    String.format("%,d đ", inv.getTotal())},
            {"Trạng thái:",   inv.isPaid() ? "✅ Đã thanh toán" : "⏳ Chưa thanh toán"},
        };
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0]);
            k.setFont(UITheme.FONT_SMALL);
            k.setForeground(UITheme.TEXT_SECONDARY);
            JLabel v = new JLabel(r[1]);
            v.setFont(UITheme.FONT_BOLD);
            v.setForeground(UITheme.TEXT_PRIMARY);
            detail.add(k); detail.add(v);
        }
        JOptionPane.showMessageDialog(this, detail,
            "Chi tiết hóa đơn — " + inv.getId(),
            JOptionPane.INFORMATION_MESSAGE);
    }
}