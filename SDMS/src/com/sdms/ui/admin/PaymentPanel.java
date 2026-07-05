package com.sdms.ui.admin;

import com.sdms.model.Contract;
import com.sdms.model.Invoice;
import com.sdms.model.Notification;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel quản lý thanh toán hóa đơn.
 *
 * Logic nâng cao:
 *  1. Tạo hóa đơn trước hạn: chọn sinh viên cụ thể trong phòng → hóa đơn chỉ tạo
 *     cho người đó; tiền phòng = tổng monthlyFee của TẤT CẢ thành viên active trong phòng.
 *  2. Xác nhận thanh toán bằng double-click vào hàng chưa thanh toán.
 *  3. Summary cards tự động cập nhật sau mỗi thao tác.
 *  4. Lọc theo tháng, trạng thái, tìm kiếm real-time.
 */
public class PaymentPanel extends JPanel {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM/yyyy");

    private DefaultTableModel model;
    private JTable  table;
    private JTextField tfSearch;
    private JComboBox<String> cbMonth;
    private JComboBox<String> cbStatus;

    // Summary card labels — cập nhật động
    private JLabel lblRevValue, lblPendingValue, lblCountValue;

    private static final String[] COLS = {
        "Mã HĐ", "Sinh viên", "Phòng", "Tháng",
        "Tiền phòng", "Tiền điện", "Tiền nước", "Tổng tiền", "Trạng thái"
    };

    public PaymentPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));
        JLabel title = new JLabel("💳  Quản lý thanh toán");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);
        p.add(title, BorderLayout.WEST);
        return p;
    }

    // ── Body ─────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(UITheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(14, 16, 14, 16));

        // ── Summary cards ────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);

        lblRevValue     = cardValueLabel(UITheme.SUCCESS);
        lblPendingValue = cardValueLabel(UITheme.WARNING);
        lblCountValue   = cardValueLabel(UITheme.DANGER);

        cards.add(buildSummaryCard("💰", "Đã thu tháng này",   lblRevValue));
        cards.add(buildSummaryCard("⏳", "Chưa thanh toán",    lblPendingValue));
        cards.add(buildSummaryCard("📋", "Hóa đơn chờ xử lý", lblCountValue));
        updateSummaryCards();

        // ── Toolbar ──────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("🔍  Tìm theo tên sinh viên, phòng...");
        tfSearch.setPreferredSize(new Dimension(240, 36));

        // Sinh danh sách tháng động: 12 tháng gần nhất
        cbMonth = UITheme.comboBox(buildMonthOptions());
        cbMonth.setPreferredSize(new Dimension(130, 36));

        cbStatus = UITheme.comboBox(new String[]{"Tất cả", "Đã thanh toán", "Chưa thanh toán"});
        cbStatus.setPreferredSize(new Dimension(150, 36));

        JButton btnPrepay = UITheme.primaryBtn("⚡ Tạo hóa đơn trước hạn");
        btnPrepay.addActionListener(e -> openPrepayDialog());

        JButton btnExcel = UITheme.successBtn("📊 Xuất Excel");
        btnExcel.addActionListener(e -> exportToExcel());

        toolbar.add(tfSearch);
        toolbar.add(cbMonth);
        toolbar.add(cbStatus);
        toolbar.add(btnPrepay);
        toolbar.add(btnExcel);

        // ── Table ────────────────────────────────────────────────
        model = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable();

        // Listeners lọc real-time
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshTable(); }
        });
        cbMonth.addActionListener(e -> refreshTable());
        cbStatus.addActionListener(e -> refreshTable());

        table = new JTable(model);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UITheme.BORDER);
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setSelectionForeground(UITheme.PRIMARY_DARK);
        table.setRowHeight(38);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setReorderingAllowed(false);

        // Độ rộng cột
        int[] colWidths = {70, 140, 65, 80, 110, 95, 95, 105, 120};
        for (int i = 0; i < colWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        // Renderer Mã HĐ — in đậm màu primary
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, focus, row, col) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.PRIMARY);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        });

        // Renderer Tổng tiền — in đậm màu success
        table.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, focus, row, col) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString(), SwingConstants.RIGHT);
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.SUCCESS_TEXT);
            l.setBorder(new EmptyBorder(0, 0, 0, 8));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        });

        // Renderer Trạng thái — badge màu
        table.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, focus, row, col) -> {
            boolean paid = "Đã thanh toán".equals(v == null ? "" : v.toString());
            JLabel lbl = UITheme.badge(
                v == null ? "" : v.toString(),
                paid ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
                paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT
            );
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        });

        // Double-click → xác nhận thanh toán
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0) return;
                    String id = (String) model.getValueAt(row, 0);
                    handleMarkPaid(id);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JLabel hint = new JLabel("💡 Double-click vào hóa đơn chưa thanh toán để xác nhận thanh toán");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(cards,   BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        body.add(top,    BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        body.add(hint,   BorderLayout.SOUTH);
        return body;
    }

    // ── Summary cards ─────────────────────────────────────────────
    private JLabel cardValueLabel(Color color) {
        JLabel l = new JLabel("—");
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(color);
        return l;
    }

    private JPanel buildSummaryCard(String icon, String label, JLabel valueLabel) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(10, 0));
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        info.add(valueLabel);
        info.add(lbl);
        card.add(ico,  BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    /** Cập nhật giá trị 3 summary cards từ dữ liệu mới nhất */
    private void updateSummaryCards() {
        List<Invoice> all = DatabaseService.getAllInvoices();
        long totalRev = all.stream().filter(Invoice::isPaid).mapToLong(Invoice::getTotal).sum();
        long pending  = all.stream().filter(i -> !i.isPaid()).mapToLong(Invoice::getTotal).sum();
        long count    = all.stream().filter(i -> !i.isPaid()).count();
        if (lblRevValue     != null) lblRevValue.setText(String.format("%,d đ", totalRev));
        if (lblPendingValue != null) lblPendingValue.setText(String.format("%,d đ", pending));
        if (lblCountValue   != null) lblCountValue.setText(count + " hóa đơn");
    }

    // ── Lọc & Refresh bảng ───────────────────────────────────────
    private void refreshTable() {
        String search = tfSearch  != null ? tfSearch.getText().trim().toLowerCase()    : "";
        String month  = cbMonth   != null ? (String) cbMonth.getSelectedItem()         : "Tất cả tháng";
        String status = cbStatus  != null ? (String) cbStatus.getSelectedItem()        : "Tất cả";

        model.setRowCount(0);
        for (Invoice inv : DatabaseService.getAllInvoices()) {
            if (!"Tất cả tháng".equals(month)) {
                if (inv.getMonth() == null || !inv.getMonth().equals(month)) continue;
            }
            if (!"Tất cả".equals(status)) {
                boolean wantPaid = "Đã thanh toán".equals(status);
                if (inv.isPaid() != wantPaid) continue;
            }
            if (!search.isEmpty()) {
                String combined = (inv.getStudentName() == null ? "" : inv.getStudentName().toLowerCase())
                                + " " + (inv.getRoomId() == null ? "" : inv.getRoomId().toLowerCase())
                                + " " + (inv.getId() == null ? "" : inv.getId().toLowerCase());
                if (!combined.contains(search)) continue;
            }
            model.addRow(inv.toRow());
        }
    }

    // ── Xác nhận thanh toán (double-click) ───────────────────────
    private void handleMarkPaid(String invoiceId) {
        Invoice inv = DatabaseService.getAllInvoices().stream()
            .filter(i -> i.getId().equals(invoiceId))
            .findFirst().orElse(null);
        if (inv == null) return;

        if (inv.isPaid()) {
            JOptionPane.showMessageDialog(this,
                "ℹ Hóa đơn " + invoiceId + " đã được thanh toán rồi.",
                "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Dialog xác nhận chi tiết
        JPanel dlg = new JPanel(new BorderLayout(0, 8));
        dlg.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel info = new JLabel(
            "<html><b>Xác nhận thanh toán hóa đơn " + invoiceId + "</b><br><br>"
            + "Sinh viên: <b>" + inv.getStudentName() + "</b><br>"
            + "Phòng: <b>" + inv.getRoomId() + "</b> — Tháng: <b>" + inv.getMonth() + "</b><br><br>"
            + "Tiền phòng:&nbsp;&nbsp;&nbsp;" + String.format("%,d đ", inv.getRoomFee()) + "<br>"
            + "Tiền điện:&nbsp;&nbsp;&nbsp;&nbsp;" + String.format("%,d đ", inv.getElectricFee()) + "<br>"
            + "Tiền nước:&nbsp;&nbsp;&nbsp;&nbsp;" + String.format("%,d đ", inv.getWaterFee()) + "<br>"
            + "<hr><b>Tổng cộng: " + String.format("%,d đ", inv.getTotal()) + "</b></html>"
        );
        dlg.add(info, BorderLayout.CENTER);

        int r = JOptionPane.showConfirmDialog(this, dlg,
            "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;

        if (DatabaseService.markInvoicePaid(inv.getId(), true)) {
            inv.setPaid(true);
            refreshTable();
            updateSummaryCards();

            // Gửi thông báo xác nhận cho sinh viên
            Notification noti = new Notification(
                DatabaseService.nextNotificationId(),
                "Xác nhận thanh toán tháng " + inv.getMonth(),
                "Hóa đơn " + inv.getId() + " tháng " + inv.getMonth()
                    + " của bạn đã được xác nhận thanh toán thành công. "
                    + "Tổng tiền: " + String.format("%,d đ", inv.getTotal()) + ".",
                Notification.Type.INVOICE,
                Notification.Target.STUDENT,
                inv.getStudentId(),
                java.time.LocalDateTime.now(),
                "Quản trị viên",
                false, false
            );
            DatabaseService.addNotification(noti);

            JOptionPane.showMessageDialog(this,
                "✅ Đã xác nhận thanh toán hóa đơn " + invoiceId + "!\n"
                + "📨 Đã gửi thông báo tới sinh viên " + inv.getStudentName() + ".",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Lưu thất bại! Kiểm tra kết nối database.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Tạo hóa đơn trước hạn ────────────────────────────────────
    /**
     * Logic:
     *  1. Chọn PHÒNG → hiển thị danh sách sinh viên ACTIVE trong phòng đó.
     *  2. Admin chọn sinh viên cụ thể cần tạo hóa đơn.
     *  3. Tiền phòng = tổng monthlyFee của TẤT CẢ hợp đồng active trong phòng.
     *  4. Hóa đơn tạo chỉ cho sinh viên được chọn với roomFee = tổng tiền phòng.
     *  5. Gửi thông báo cho sinh viên đó.
     */
    private void openPrepayDialog() {
        // Lấy danh sách phòng có ít nhất 1 hợp đồng active
        List<Contract> allActive = DatabaseService.getAllContracts().stream()
            .filter(c -> c.getStatus() == Contract.Status.ACTIVE)
            .collect(Collectors.toList());

        if (allActive.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "⚠ Không có sinh viên nào đang có hợp đồng hiệu lực!",
                "Không có dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Danh sách phòng duy nhất (có active contract)
        List<String> roomList = allActive.stream()
            .map(Contract::getRoomId)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        // ── Bước 1: Chọn phòng ───────────────────────────────────
        JComboBox<String> cbRoom = new JComboBox<>(roomList.toArray(new String[0]));
        cbRoom.setPreferredSize(new Dimension(200, 32));

        // Tháng: 6 tháng tới kể cả hiện tại
        YearMonth now = YearMonth.now();
        String[] monthOptions = new String[6];
        for (int i = 0; i < 6; i++) {
            YearMonth ym = now.plusMonths(i);
            monthOptions[i] = String.format("%02d/%d", ym.getMonthValue(), ym.getYear());
        }
        JComboBox<String> cbTargetMonth = new JComboBox<>(monthOptions);
        cbTargetMonth.setEditable(true);
        cbTargetMonth.setPreferredSize(new Dimension(200, 32));

        // Panel bước 1
        JPanel step1 = new JPanel(new GridLayout(0, 2, 8, 10));
        step1.setBorder(new EmptyBorder(8, 8, 8, 8));
        step1.add(new JLabel("Phòng:"));
        step1.add(cbRoom);
        step1.add(new JLabel("Tháng thanh toán:"));
        step1.add(cbTargetMonth);

        int r1 = JOptionPane.showConfirmDialog(this, step1,
            "Tạo hóa đơn trước hạn — Bước 1: Chọn phòng & tháng",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r1 != JOptionPane.OK_OPTION) return;

        String selectedRoom  = (String) cbRoom.getSelectedItem();
        String selectedMonth = ((String) cbTargetMonth.getEditor().getItem()).trim();

        // Validate tháng
        if (!selectedMonth.matches("\\d{2}/\\d{4}")) {
            JOptionPane.showMessageDialog(this,
                "⚠ Tháng không hợp lệ! Nhập theo dạng MM/yyyy, ví dụ 09/2026.",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hợp đồng active trong phòng được chọn
        List<Contract> roomContracts = allActive.stream()
            .filter(c -> c.getRoomId().equals(selectedRoom))
            .collect(Collectors.toList());

        if (roomContracts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "⚠ Phòng " + selectedRoom + " không có sinh viên nào đang active!",
                "Không có dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tổng tiền phòng = cộng TẤT CẢ hợp đồng active trong phòng
        long totalRoomFee = roomContracts.stream()
            .mapToLong(Contract::getMonthlyFee)
            .sum();

        // ── Bước 2: Chọn sinh viên cụ thể ───────────────────────
        String[] studentOptions = roomContracts.stream()
            .map(c -> c.getStudentId() + " — " + c.getStudentName()
                    + "  [" + String.format("%,d đ/tháng", c.getMonthlyFee()) + "]")
            .toArray(String[]::new);

        JComboBox<String> cbStudent = new JComboBox<>(studentOptions);
        cbStudent.setPreferredSize(new Dimension(360, 32));

        JPanel step2 = new JPanel(new BorderLayout(0, 10));
        step2.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel infoLbl = new JLabel(
            "<html>"
            + "<b>Phòng " + selectedRoom + " — Tháng " + selectedMonth + "</b><br><br>"
            + "Số thành viên đang active: <b>" + roomContracts.size() + " người</b><br>"
            + "Tổng tiền phòng (tất cả thành viên): "
            + "<b style='color:green'>" + String.format("%,d đ", totalRoomFee) + "</b><br><br>"
            + "Chọn sinh viên cần tạo hóa đơn:"
            + "</html>"
        );
        infoLbl.setFont(UITheme.FONT_SMALL);

        step2.add(infoLbl,   BorderLayout.NORTH);
        step2.add(cbStudent, BorderLayout.CENTER);

        // Hiển thị chi tiết từng thành viên
        JPanel memberList = new JPanel();
        memberList.setLayout(new BoxLayout(memberList, BoxLayout.Y_AXIS));
        memberList.setOpaque(false);
        memberList.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            "Chi tiết thành viên trong phòng",
            TitledBorder.LEFT, TitledBorder.TOP,
            UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY
        ));
        for (Contract c : roomContracts) {
            JLabel ml = new JLabel("  • " + c.getStudentName()
                + " (" + c.getStudentId() + ")  —  "
                + String.format("%,d đ/tháng", c.getMonthlyFee()));
            ml.setFont(UITheme.FONT_SMALL);
            ml.setForeground(UITheme.TEXT_PRIMARY);
            memberList.add(ml);
            memberList.add(Box.createVerticalStrut(3));
        }

        JPanel step2Full = new JPanel(new BorderLayout(0, 10));
        step2Full.add(step2,      BorderLayout.NORTH);
        step2Full.add(memberList, BorderLayout.CENTER);

        int r2 = JOptionPane.showConfirmDialog(this, step2Full,
            "Tạo hóa đơn trước hạn — Bước 2: Chọn sinh viên",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r2 != JOptionPane.OK_OPTION) return;

        int idx = cbStudent.getSelectedIndex();
        if (idx < 0) return;
        Contract selectedContract = roomContracts.get(idx);

        // Kiểm tra hóa đơn đã tồn tại cho sinh viên/tháng này chưa
        boolean exists = DatabaseService.getAllInvoices().stream()
            .anyMatch(inv -> inv.getStudentId().equals(selectedContract.getStudentId())
                         && inv.getMonth().equals(selectedMonth));
        if (exists) {
            JOptionPane.showMessageDialog(this,
                "⚠ Sinh viên <b>" + selectedContract.getStudentName() + "</b> đã có hóa đơn tháng "
                + selectedMonth + " rồi! Không thể tạo trùng.",
                "Đã tồn tại", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Tạo hóa đơn ─────────────────────────────────────────
        // roomFee = tổng toàn bộ thành viên phòng
        // electricFee / waterFee = 0 (chưa có chỉ số — hóa đơn trước hạn)
        Invoice inv = new Invoice(
            DatabaseService.nextInvoiceId(),
            selectedContract.getStudentId(),
            selectedContract.getStudentName(),
            selectedRoom,
            selectedMonth,
            totalRoomFee,   // tổng tiền phòng tất cả thành viên
            0L,             // điện: chưa có chỉ số
            0L,             // nước: chưa có chỉ số
            false           // chưa thanh toán
        );

        if (!DatabaseService.addInvoice(inv)) {
            JOptionPane.showMessageDialog(this,
                "❌ Tạo hóa đơn thất bại! Kiểm tra kết nối database.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Gửi thông báo cho sinh viên được chọn
        Notification noti = new Notification(
            DatabaseService.nextNotificationId(),
            "Hóa đơn tháng " + selectedMonth + " — Phòng " + selectedRoom,
            "Bạn có hóa đơn tiền phòng tháng " + selectedMonth + " với tổng tiền phòng "
                + String.format("%,d đ", totalRoomFee)
                + ". Vui lòng thanh toán trước hạn. "
                + "Mã hóa đơn: " + inv.getId() + ".",
            Notification.Type.INVOICE,
            Notification.Target.STUDENT,
            selectedContract.getStudentId(),
            java.time.LocalDateTime.now(),
            "Quản trị viên",
            false, false
        );
        DatabaseService.addNotification(noti);

        refreshTable();
        updateSummaryCards();

        JOptionPane.showMessageDialog(this,
            "✅ Đã tạo hóa đơn " + inv.getId() + " thành công!\n\n"
            + "Sinh viên: " + selectedContract.getStudentName() + "\n"
            + "Phòng: " + selectedRoom + " — Tháng: " + selectedMonth + "\n"
            + "Tiền phòng (cả phòng " + roomContracts.size() + " người): "
                + String.format("%,d đ", totalRoomFee) + "\n\n"
            + "📨 Đã gửi thông báo tới sinh viên.",
            "Tạo hóa đơn thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Xuất CSV ─────────────────────────────────────────────────
    private void exportToExcel() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setSelectedFile(new java.io.File("HoaDon_ThanhToan.csv"));
        fc.setDialogTitle("Lưu file xuất Excel");
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new java.io.File(file.getPath() + ".csv");

        List<Invoice> toExport = DatabaseService.getAllInvoices();

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF'); // BOM — Excel đọc UTF-8
            pw.println("Mã HĐ,Sinh viên,Phòng,Tháng,Tiền phòng,Tiền điện,Tiền nước,Tổng tiền,Trạng thái");
            for (Invoice inv : toExport) {
                pw.printf("%s,%s,%s,%s,%d,%d,%d,%d,%s%n",
                    inv.getId(),
                    inv.getStudentName(),
                    inv.getRoomId(),
                    inv.getMonth(),
                    inv.getRoomFee(),
                    inv.getElectricFee(),
                    inv.getWaterFee(),
                    inv.getTotal(),
                    inv.isPaid() ? "Đã thanh toán" : "Chưa thanh toán"
                );
            }
            JOptionPane.showMessageDialog(this,
                "✅ Đã xuất " + toExport.size() + " hóa đơn ra:\n" + file.getAbsolutePath(),
                "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Xuất thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Tiện ích ─────────────────────────────────────────────────

    /** Sinh danh sách tháng động: "Tất cả tháng" + 12 tháng gần nhất */
    private String[] buildMonthOptions() {
        YearMonth now = YearMonth.now();
        List<String> opts = new ArrayList<>();
        opts.add("Tất cả tháng");
        for (int i = 0; i < 12; i++) {
            YearMonth ym = now.minusMonths(i);
            opts.add(String.format("%02d/%d", ym.getMonthValue(), ym.getYear()));
        }
        return opts.toArray(new String[0]);
    }
}