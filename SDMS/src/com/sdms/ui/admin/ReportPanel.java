package com.sdms.ui.admin;

import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Panel thống kê và báo cáo tổng hợp cho Admin.
 * Gồm 4 tab: Tổng quan | Phòng-Sinh viên | Tài chính | Vi phạm.
 * Mỗi tab dùng card thống kê + bảng chi tiết.
 */
public class ReportPanel extends JPanel {

    public ReportPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(),   BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("📊  Thống kê & Báo cáo");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Dashboard / Thống kê & Báo cáo");
        breadcrumb.setFont(UITheme.FONT_TINY);
        breadcrumb.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title,      BorderLayout.NORTH);
        left.add(breadcrumb, BorderLayout.SOUTH);

        // Nút xuất báo cáo tổng hợp
        JButton btnExport = UITheme.successBtn("📥 Xuất báo cáo tổng hợp");
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "✅ Đã xuất báo cáo tổng hợp thành file Excel!", "Thông báo",
            JOptionPane.INFORMATION_MESSAGE));

        p.add(left,      BorderLayout.WEST);
        p.add(btnExport, BorderLayout.EAST);
        return p;
    }

    // ── Tab container ─────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UITheme.FONT_LABEL);
        tabs.setBackground(UITheme.BG_LIGHT);

        tabs.addTab("🏠  Tổng quan",      buildOverviewTab());
        tabs.addTab("🛏  Phòng & Sinh viên", buildRoomTab());
        tabs.addTab("💰  Tài chính",       buildFinanceTab());
        tabs.addTab("⚠  Vi phạm",         buildViolationTab());

        return tabs;
    }

    // ══════════════════════════════════════════════════════════════
    // TAB 1: TỔNG QUAN
    // ══════════════════════════════════════════════════════════════
    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Hàng 1: KPI cards lớn (4 card)
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);

        int totalStudents = DatabaseService.getAllStudents().size();
        int totalRooms    = 	DatabaseService.getAllRooms().size();
        long occupiedRooms = 	DatabaseService.getAllRooms().stream()
            .filter(r -> r.getOccupied() > 0).count();
        double occupancyRate = totalRooms > 0
            ? (double) occupiedRooms / totalRooms * 100 : 0;

        kpiRow.add(kpiCard("👨‍🎓 Sinh viên",
            String.valueOf(totalStudents), "đang lưu trú",
            UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));
        kpiRow.add(kpiCard("🛏 Phòng đang dùng",
            occupiedRooms + "/" + totalRooms,
            String.format("%.0f%% công suất", occupancyRate),
            UITheme.SUCCESS_TEXT, UITheme.SUCCESS_BG));
        kpiRow.add(kpiCard("📄 Hợp đồng hiệu lực",
            "47", "trong tháng 06/2026",
            UITheme.INFO_TEXT, UITheme.INFO_BG));
        kpiRow.add(kpiCard("⚠ Vi phạm chờ xử lý",
            "3", "cần xử lý ngay",
            UITheme.DANGER, UITheme.DANGER_BG));

        // Hàng 2: thống kê tài chính tháng này
        JPanel finRow = new JPanel(new GridLayout(1, 3, 14, 0));
        finRow.setOpaque(false);

        finRow.add(finCard("💵 Thu tiền phòng",   "39,950,000 đ", "06/2026", true));
        finRow.add(finCard("⚡ Thu điện nước",     "8,234,000 đ",  "06/2026", true));
        finRow.add(finCard("⚠ Tiền phạt vi phạm", "1,800,000 đ",  "06/2026", false));

        // Bảng hoạt động gần đây
        JPanel recentPanel = buildRecentActivityTable();

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 14));
        top.setOpaque(false);
        top.add(kpiRow);
        top.add(finRow);

        p.add(top,         BorderLayout.NORTH);
        p.add(recentPanel, BorderLayout.CENTER);
        return p;
    }

    /** Card KPI lớn — icon + số + phụ đề */
    private JPanel kpiCard(String title, String value, String subtitle, Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(UITheme.FONT_TITLE.getName(), Font.BOLD, 28));
        lblValue.setForeground(accent);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UITheme.FONT_TINY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        // Thanh màu trên cùng
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(0, 4));

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(lblTitle, BorderLayout.NORTH);
        content.add(lblValue, BorderLayout.CENTER);
        content.add(lblSub,   BorderLayout.SOUTH);

        card.add(bar,     BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /** Card tài chính nhỏ */
    private JPanel finCard(String title, String amount, String period, boolean positive) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblAmount = new JLabel(amount);
        lblAmount.setFont(UITheme.FONT_H2);
        lblAmount.setForeground(positive ? UITheme.SUCCESS_TEXT : UITheme.DANGER);

        JLabel lblPeriod = new JLabel("Tháng " + period);
        lblPeriod.setFont(UITheme.FONT_TINY);
        lblPeriod.setForeground(UITheme.TEXT_MUTED);

        card.add(lblTitle,  BorderLayout.NORTH);
        card.add(lblAmount, BorderLayout.CENTER);
        card.add(lblPeriod, BorderLayout.SOUTH);
        return card;
    }

    /** Bảng hoạt động gần đây */
    private JPanel buildRecentActivityTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("🕐  Hoạt động gần đây");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] cols = {"Thời gian", "Loại", "Mô tả", "Người thực hiện"};
        Object[][] rows = {
            {"08/06/2026 09:15", "Hợp đồng",   "Ký hợp đồng mới cho SV001248 — phòng A301", "Admin"},
            {"08/06/2026 08:50", "Điện nước",   "Chốt chỉ số tháng 05/2026 — phòng B204",   "Admin"},
            {"07/06/2026 15:30", "Vi phạm",     "Ghi nhận vi phạm VP0007 — SV001240",        "Admin"},
            {"07/06/2026 14:00", "Thông báo",   "Gửi thông báo kiểm tra phòng định kỳ",      "Admin"},
            {"06/06/2026 10:20", "Sinh viên",   "Cập nhật thông tin SV001243",               "Admin"},
            {"05/06/2026 16:00", "Hóa đơn",     "Tạo hóa đơn tháng 06/2026 cho 52 phòng",   "Hệ thống"},
        };

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        tbl.setFont(UITheme.FONT_BODY);
        tbl.setRowHeight(34);
        tbl.setShowVerticalLines(false);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(UITheme.BORDER);
        tbl.getTableHeader().setFont(UITheme.FONT_LABEL);
        tbl.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        tbl.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(130);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(380);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Renderer cột Loại — badge màu
        tbl.getColumnModel().getColumn(1).setCellRenderer((t, v, sel, f, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Hợp đồng"  -> UITheme.INFO_BG;
                case "Vi phạm"   -> UITheme.DANGER_BG;
                case "Điện nước" -> UITheme.WARNING_BG;
                case "Hóa đơn"   -> UITheme.SUCCESS_BG;
                default          -> UITheme.BG_SECONDARY;
            };
            Color fg = switch (s) {
                case "Hợp đồng"  -> UITheme.INFO_TEXT;
                case "Vi phạm"   -> UITheme.DANGER_TEXT;
                case "Điện nước" -> UITheme.WARNING_TEXT;
                case "Hóa đơn"   -> UITheme.SUCCESS_TEXT;
                default          -> UITheme.TEXT_SECONDARY;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(null);

        p.add(title,  BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    // TAB 2: PHÒNG & SINH VIÊN
    // ══════════════════════════════════════════════════════════════
    private JPanel buildRoomTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        // KPI row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);

        long totalRooms    = 	DatabaseService.getAllRooms().size();
        long occupied      = 	DatabaseService.getAllRooms().stream().filter(r -> r.getOccupied() > 0).count();
        long vacant        = totalRooms - occupied;
        long totalStudents = DatabaseService.getAllStudents().size();

        kpiRow.add(smallKpi("🏠 Tổng phòng",          String.valueOf(totalRooms),    UITheme.PRIMARY));
        kpiRow.add(smallKpi("✅ Phòng đang ở",          String.valueOf(occupied),      UITheme.SUCCESS_TEXT));
        kpiRow.add(smallKpi("🔓 Phòng trống",           String.valueOf(vacant),        UITheme.WARNING_TEXT));
        kpiRow.add(smallKpi("👨‍🎓 Tổng sinh viên",       String.valueOf(totalStudents), UITheme.INFO_TEXT));

        // Bảng chi tiết phòng
        JPanel tablePanel = buildRoomDetailTable();

        p.add(kpiRow,    BorderLayout.NORTH);
        p.add(tablePanel,BorderLayout.CENTER);
        return p;
    }

    /** Bảng chi tiết thống kê từng phòng */
    private JPanel buildRoomDetailTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("📋  Chi tiết theo phòng");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] cols = {"Phòng", "Tòa", "Loại phòng", "Sức chứa", "Đang ở", "Trống", "Tỉ lệ lấp đầy", "Trạng thái"};
        Object[][] rows = buildRoomRows();

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        styleTable(tbl);
        tbl.getColumnModel().getColumn(6).setCellRenderer(progressRenderer());
        tbl.getColumnModel().getColumn(7).setCellRenderer(roomStatusRenderer());

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(null);

        p.add(title,  BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private Object[][] buildRoomRows() {
        return 	DatabaseService.getAllRooms().stream().map(r -> {
            int cap    = r.getCapacity();
            int curr   = r.getOccupied();
            int vacant = cap - curr;
            double rate = cap > 0 ? (double) curr / cap * 100 : 0;
            return new Object[]{
                r.getId(), r.getId().substring(0,1) + " — " + r.getId().substring(0,1),
                cap <= 4 ? "Phòng 4 người" : "Phòng 6 người",
                cap, curr, vacant,
                String.format("%.0f%%", rate),
                curr == 0 ? "Trống" : curr == cap ? "Đầy" : "Còn chỗ"
            };
        }).toArray(Object[][]::new);
    }

    // ══════════════════════════════════════════════════════════════
    // TAB 3: TÀI CHÍNH
    // ══════════════════════════════════════════════════════════════
    private JPanel buildFinanceTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        // KPI
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(smallKpi("💵 Tiền phòng T6",  "39,950,000 đ", UITheme.SUCCESS_TEXT));
        kpiRow.add(smallKpi("⚡ Điện nước T6",   "8,234,000 đ",  UITheme.WARNING_TEXT));
        kpiRow.add(smallKpi("📥 Đã thu",          "44,830,000 đ", UITheme.PRIMARY));
        kpiRow.add(smallKpi("📤 Còn nợ",          "3,354,000 đ",  UITheme.DANGER));

        // Bảng hóa đơn tháng
        JPanel tablePanel = buildInvoiceTable();

        p.add(kpiRow,     BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildInvoiceTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("🧾  Hóa đơn tháng 06/2026");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] cols = {"Phòng", "Mã SV", "Tên sinh viên", "Tiền phòng", "Điện nước", "Tổng hóa đơn", "Đã thanh toán", "Trạng thái"};
        Object[][] rows = {
            {"A301","SV001248","Nguyễn Văn An",   "850,000 đ","76,000 đ", "926,000 đ",  "926,000 đ",  "Đã thanh toán"},
            {"B204","SV001247","Trần Thị Bình",   "850,000 đ","60,000 đ", "910,000 đ",  "910,000 đ",  "Đã thanh toán"},
            {"A204","SV001243","Vũ Thị Lan",      "850,000 đ","69,000 đ", "919,000 đ",  "0 đ",        "Chưa thanh toán"},
            {"B102","SV001242","Đặng Minh Tuấn",  "850,000 đ","88,000 đ", "938,000 đ",  "0 đ",        "Quá hạn"},
            {"C305","SV001241","Bùi Thị Mai",     "850,000 đ","65,000 đ", "915,000 đ",  "915,000 đ",  "Đã thanh toán"},
            {"D201","SV001240","Ngô Quốc Hùng",   "850,000 đ","56,000 đ", "906,000 đ",  "906,000 đ",  "Đã thanh toán"},
            {"A301","SV001249","Nguyễn Thị Lan Anh","850,000 đ","76,000 đ","926,000 đ", "500,000 đ",  "Thanh toán một phần"},
        };

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        styleTable(tbl);

        // Renderer cột Trạng thái thanh toán
        tbl.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, f, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Đã thanh toán"         -> UITheme.SUCCESS_BG;
                case "Chưa thanh toán"       -> UITheme.WARNING_BG;
                case "Quá hạn"               -> UITheme.DANGER_BG;
                case "Thanh toán một phần"   -> UITheme.INFO_BG;
                default -> UITheme.BG_SECONDARY;
            };
            Color fg = switch (s) {
                case "Đã thanh toán"         -> UITheme.SUCCESS_TEXT;
                case "Chưa thanh toán"       -> UITheme.WARNING_TEXT;
                case "Quá hạn"               -> UITheme.DANGER_TEXT;
                case "Thanh toán một phần"   -> UITheme.INFO_TEXT;
                default -> UITheme.TEXT_SECONDARY;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(null);

        p.add(title,  BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    // TAB 4: VI PHẠM
    // ══════════════════════════════════════════════════════════════
    private JPanel buildViolationTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        // KPI
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(smallKpi("📋 Tổng vi phạm",      "24",           UITheme.TEXT_PRIMARY));
        kpiRow.add(smallKpi("⏳ Chưa xử lý",         "3",            UITheme.DANGER));
        kpiRow.add(smallKpi("✅ Đã xử lý",           "19",           UITheme.SUCCESS_TEXT));
        kpiRow.add(smallKpi("💰 Tổng tiền phạt",     "4,800,000 đ",  UITheme.WARNING_TEXT));

        // Bảng tổng hợp theo loại vi phạm
        JPanel tablePanel = buildViolationSummaryTable();

        p.add(kpiRow,     BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildViolationSummaryTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("📊  Thống kê vi phạm theo loại");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] cols = {"Loại vi phạm", "Số lần", "Tỉ lệ", "Tiền phạt trung bình", "Tổng tiền phạt", "Xu hướng"};
        Object[][] rows = {
            {"Gây tiếng ồn",                   "7", "29.2%", "0 đ",       "0 đ",         "📈 Tăng"},
            {"Vi phạm giờ giấc",                "4", "16.7%", "0 đ",       "0 đ",         "➡ Ổn định"},
            {"Mang khách lạ vào khu nội trú",   "4", "16.7%", "200,000 đ", "800,000 đ",   "📉 Giảm"},
            {"Hút thuốc trong phòng",            "3", "12.5%", "500,000 đ", "1,500,000 đ", "➡ Ổn định"},
            {"Sử dụng thiết bị điện công suất lớn","2","8.3%","300,000 đ","600,000 đ",   "📉 Giảm"},
            {"Phá hỏng tài sản",                "2", "8.3%",  "800,000 đ", "1,600,000 đ", "📈 Tăng"},
            {"Vi phạm vệ sinh chung",            "2", "8.3%",  "0 đ",       "0 đ",         "📉 Giảm"},
        };

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        styleTable(tbl);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(240);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(null);

        p.add(title,  BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Helpers dùng chung ────────────────────────────────────────

    /** KPI card nhỏ */
    private JPanel smallKpi(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UITheme.FONT_SMALL);
        lblLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_H2);
        lblValue.setForeground(accent);

        card.add(lblLabel, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    /** Áp dụng style chuẩn cho JTable */
    private void styleTable(JTable tbl) {
        tbl.setFont(UITheme.FONT_BODY);
        tbl.setRowHeight(36);
        tbl.setShowVerticalLines(false);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(UITheme.BORDER);
        tbl.setBackground(UITheme.WHITE);
        tbl.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        tbl.setSelectionForeground(UITheme.PRIMARY_DARK);
        tbl.getTableHeader().setFont(UITheme.FONT_LABEL);
        tbl.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        tbl.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        tbl.getTableHeader().setReorderingAllowed(false);
    }

    /** Renderer thanh tiến trình tỉ lệ lấp đầy */
    private TableCellRenderer progressRenderer() {
        return (t, v, sel, f, row, col) -> {
            String s = v.toString();
            int pct = 0;
            try { pct = Integer.parseInt(s.replace("%", "").trim()); } catch (Exception ignored) {}
            final int finalPct = pct;

            return new JPanel() {
                { setOpaque(true); setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE); }
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int w = (int)(getWidth() * finalPct / 100.0);
                    Color c = finalPct >= 90 ? UITheme.DANGER
                            : finalPct >= 70 ? UITheme.WARNING_TEXT
                            : UITheme.SUCCESS_TEXT;
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                    g.fillRoundRect(4, 6, w - 8, getHeight() - 12, 6, 6);
                    g.setColor(c);
                    g.setFont(UITheme.FONT_SMALL);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(s, (getWidth() - fm.stringWidth(s))/2,
                        (getHeight() + fm.getAscent())/2 - 2);
                }
            };
        };
    }

    /** Renderer trạng thái phòng */
    private TableCellRenderer roomStatusRenderer() {
        return (t, v, sel, f, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Đầy"     -> UITheme.DANGER_BG;
                case "Còn chỗ" -> UITheme.SUCCESS_BG;
                case "Trống"   -> UITheme.WARNING_BG;
                default        -> UITheme.BG_SECONDARY;
            };
            Color fg = switch (s) {
                case "Đầy"     -> UITheme.DANGER_TEXT;
                case "Còn chỗ" -> UITheme.SUCCESS_TEXT;
                case "Trống"   -> UITheme.WARNING_TEXT;
                default        -> UITheme.TEXT_SECONDARY;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }
}
