package com.sdms.ui.admin;

import com.sdms.model.Utility;
import com.sdms.utils.DataStore;
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
 * Panel quản lý chỉ số điện nước hàng tháng.
 * Hỗ trợ: Thêm, Sửa, Xóa, Tìm kiếm, Lọc theo tháng/phòng, Chốt chỉ số.
 */
public class UtilityPanel extends JPanel {

    // ── Dữ liệu ───────────────────────────────────────────────────
    private final List<Utility> utilities = new ArrayList<>();
    private Utility editingUtility = null;

    // ── Table ─────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JLabel            lblCount;

    // ── Form fields ───────────────────────────────────────────────
    private JTextField        tfId, tfMonth, tfElecPrev, tfElecCurr,
                              tfWaterPrev, tfWaterCurr, tfElecPrice, tfWaterPrice, tfNote;
    private JComboBox<String> cbRoom;

    // ── Cột bảng ─────────────────────────────────────────────────
    private static final String[] COLS = {
        "Mã", "Phòng", "Tháng",
        "Điện đầu", "Điện cuối", "Dùng (kWh)", "Tiền điện",
        "Nước đầu", "Nước cuối", "Dùng (m³)", "Tiền nước",
        "Tổng tiền", "Trạng thái"
    };

    public UtilityPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        initSampleData();
        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTableArea()
        );
        split.setDividerLocation(300);
        split.setDividerSize(2);
        split.setBackground(UITheme.BORDER);
        split.setBorder(null);
        split.setResizeWeight(0.25);
        add(split, BorderLayout.CENTER);
    }

    // ── Tạo dữ liệu mẫu ──────────────────────────────────────────
    private void initSampleData() {
        utilities.add(new Utility("UT0001","A301","06/2026", 1240,1278, 42,46));
        utilities.add(new Utility("UT0002","B204","06/2026", 980, 1010, 18,21));
        utilities.add(new Utility("UT0003","A204","06/2026", 760, 795,  30,33.5));
        utilities.add(new Utility("UT0004","B102","06/2026", 1120,1160, 55,59));
        utilities.add(new Utility("UT0005","C305","06/2026", 880, 915,  22,25.5));
        utilities.add(new Utility("UT0006","D201","06/2026", 600, 628,  14,16.8));
        utilities.add(new Utility("UT0007","A301","05/2026", 1205,1240, 40,42));
        utilities.add(new Utility("UT0008","B204","05/2026", 960, 980,  17,18));
        // Đánh dấu vài bản ghi đã chốt
        utilities.get(6).setConfirmed(true);
        utilities.get(7).setConfirmed(true);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("⚡  Quản lý điện nước");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Dashboard / Quản lý điện nước");
        breadcrumb.setFont(UITheme.FONT_TINY);
        breadcrumb.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title,     BorderLayout.NORTH);
        left.add(breadcrumb,BorderLayout.SOUTH);

        lblCount = new JLabel("Tổng cộng: " + utilities.size() + " bản ghi");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        p.add(left,     BorderLayout.WEST);
        p.add(lblCount, BorderLayout.EAST);
        return p;
    }

    // ── FORM nhập liệu ────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.WHITE);
        wrapper.setBorder(new MatteBorder(0, 0, 0, 1, UITheme.BORDER));

        JPanel form = new JPanel();
        form.setBackground(UITheme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Tiêu đề form
        JLabel sec = new JLabel("NHẬP CHỈ SỐ ĐIỆN NƯỚC");
        sec.setFont(UITheme.FONT_LABEL);
        sec.setForeground(UITheme.PRIMARY);
        sec.setBorder(new EmptyBorder(0, 0, 10, 0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // Khởi tạo trường nhập
        tfId         = UITheme.textField("UT0001");
        tfMonth      = UITheme.textField("06/2026");
        tfElecPrev   = UITheme.textField("Chỉ số điện đầu kỳ");
        tfElecCurr   = UITheme.textField("Chỉ số điện cuối kỳ");
        tfWaterPrev  = UITheme.textField("Chỉ số nước đầu kỳ");
        tfWaterCurr  = UITheme.textField("Chỉ số nước cuối kỳ");
        tfElecPrice  = UITheme.textField(String.valueOf(Utility.DEFAULT_ELECTRIC_UNIT_PRICE));
        tfWaterPrice = UITheme.textField(String.valueOf(Utility.DEFAULT_WATER_UNIT_PRICE));
        tfNote       = UITheme.textField("Ghi chú...");

        // Danh sách phòng
        String[] roomIds = 	DatabaseService.getAllRooms().stream()
            .map(r -> r.getId()).toArray(String[]::new);
        cbRoom = UITheme.comboBox(roomIds);

        // Điền mã mới tự động
        tfId.setText(nextUtilityId());
        tfElecPrice.setText(String.valueOf(Utility.DEFAULT_ELECTRIC_UNIT_PRICE));
        tfWaterPrice.setText(String.valueOf(Utility.DEFAULT_WATER_UNIT_PRICE));

        // Listener tính tiền preview khi nhập chỉ số
        KeyAdapter calcPreview = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { updatePreview(); }
        };
        tfElecPrev.addKeyListener(calcPreview);
        tfElecCurr.addKeyListener(calcPreview);
        tfWaterPrev.addKeyListener(calcPreview);
        tfWaterCurr.addKeyListener(calcPreview);
        tfElecPrice.addKeyListener(calcPreview);
        tfWaterPrice.addKeyListener(calcPreview);

        // Grid 2 cột
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        addField(grid, "Mã bản ghi",      tfId);
        addField(grid, "Tháng *",         tfMonth);
        addField(grid, "Phòng *",         cbRoom);
        // Ô trống để giữ layout
        grid.add(new JPanel() {{ setOpaque(false); }});

        // Separator điện
        JLabel elecSec = sectionLabel("⚡  ĐIỆN (kWh)");
        // Separator nước
        JLabel waterSec = sectionLabel("💧  NƯỚC (m³)");

        JPanel elecGrid = new JPanel(new GridLayout(1, 2, 8, 0));
        elecGrid.setOpaque(false);
        elecGrid.setAlignmentX(LEFT_ALIGNMENT);
        elecGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addField(elecGrid, "Chỉ số đầu kỳ", tfElecPrev);
        addField(elecGrid, "Chỉ số cuối kỳ", tfElecCurr);

        JPanel waterGrid = new JPanel(new GridLayout(1, 2, 8, 0));
        waterGrid.setOpaque(false);
        waterGrid.setAlignmentX(LEFT_ALIGNMENT);
        waterGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addField(waterGrid, "Chỉ số đầu kỳ", tfWaterPrev);
        addField(waterGrid, "Chỉ số cuối kỳ", tfWaterCurr);

        JPanel priceGrid = new JPanel(new GridLayout(1, 2, 8, 0));
        priceGrid.setOpaque(false);
        priceGrid.setAlignmentX(LEFT_ALIGNMENT);
        priceGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addField(priceGrid, "Đơn giá điện (đ/kWh)", tfElecPrice);
        addField(priceGrid, "Đơn giá nước (đ/m³)",  tfWaterPrice);

        // Panel preview tính tiền
        JPanel preview = buildPreviewPanel();

        // Ghi chú full width
        JPanel noteRow = singleRow("Ghi chú", tfNote);

        // Nút thao tác
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnAdd    = UITheme.primaryBtn("➕ Thêm");
        JButton btnEdit   = UITheme.warningBtn("✏ Sửa");
        JButton btnDelete = UITheme.dangerBtn("🗑 Xóa");
        JButton btnReset  = UITheme.outlineBtn("↺ Làm mới");
        btnRow.add(btnAdd); btnRow.add(btnEdit); btnRow.add(btnDelete); btnRow.add(btnReset);

        // Nút chốt chỉ số
        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false);
        btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnConfirm = UITheme.successBtn("✓ Chốt chỉ số");
        JButton btnExport  = UITheme.purpleBtn("📊 Xuất báo cáo");
        btnRow2.add(btnConfirm); btnRow2.add(btnExport);

        // Gắn sự kiện
        btnAdd.addActionListener(e    -> addUtility());
        btnEdit.addActionListener(e   -> editUtility());
        btnDelete.addActionListener(e -> deleteUtility());
        btnReset.addActionListener(e  -> clearForm());
        btnConfirm.addActionListener(e-> confirmUtility());
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "✅ Đã xuất báo cáo điện nước thành công!", "Thông báo",
            JOptionPane.INFORMATION_MESSAGE));

        // Ghép form
        form.add(sec);
        form.add(grid);
        form.add(Box.createVerticalStrut(8));
        form.add(elecSec);
        form.add(Box.createVerticalStrut(4));
        form.add(elecGrid);
        form.add(Box.createVerticalStrut(8));
        form.add(waterSec);
        form.add(Box.createVerticalStrut(4));
        form.add(waterGrid);
        form.add(Box.createVerticalStrut(8));
        form.add(sectionLabel("💰  ĐƠN GIÁ"));
        form.add(Box.createVerticalStrut(4));
        form.add(priceGrid);
        form.add(Box.createVerticalStrut(10));
        form.add(preview);
        form.add(Box.createVerticalStrut(8));
        form.add(noteRow);
        form.add(Box.createVerticalStrut(12));
        form.add(btnRow);
        form.add(Box.createVerticalStrut(6));
        form.add(btnRow2);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Panel preview tính tiền real-time ─────────────────────────
    private JLabel lblPreviewElec, lblPreviewWater, lblPreviewTotal;

    private JPanel buildPreviewPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 6, 0));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        p.setOpaque(false);

        lblPreviewElec  = previewChip("Tiền điện", "0 đ", UITheme.WARNING_BG, UITheme.WARNING_TEXT);
        lblPreviewWater = previewChip("Tiền nước", "0 đ", new Color(0xDBEAFE), UITheme.INFO_TEXT);
        lblPreviewTotal = previewChip("Tổng tiền", "0 đ", UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);

        p.add(wrapChip("⚡ Điện", lblPreviewElec));
        p.add(wrapChip("💧 Nước", lblPreviewWater));
        p.add(wrapChip("💰 Tổng", lblPreviewTotal));
        return p;
    }

    private JLabel previewChip(String label, String value, Color bg, Color fg) {
        JLabel l = new JLabel(value, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(4, 6, 4, 6));
        return l;
    }

    private JPanel wrapChip(String label, JLabel chip) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_TINY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(chip, BorderLayout.CENTER);
        return p;
    }

    /** Cập nhật preview tính tiền khi người dùng nhập */
    private void updatePreview() {
        try {
            double elecPrev  = parseDouble(tfElecPrev.getText());
            double elecCurr  = parseDouble(tfElecCurr.getText());
            double waterPrev = parseDouble(tfWaterPrev.getText());
            double waterCurr = parseDouble(tfWaterCurr.getText());
            long   elecPrice = parseLong(tfElecPrice.getText());
            long   waterPrice= parseLong(tfWaterPrice.getText());

            long elecFee  = Math.round(Math.max(0, elecCurr - elecPrev) * elecPrice);
            long waterFee = Math.round(Math.max(0, waterCurr - waterPrev) * waterPrice);
            long total    = elecFee + waterFee;

            lblPreviewElec.setText(String.format("%,d đ", elecFee));
            lblPreviewWater.setText(String.format("%,d đ", waterFee));
            lblPreviewTotal.setText(String.format("%,d đ", total));
        } catch (Exception ignored) {}
    }

    // ── Khu vực bảng dữ liệu ─────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("🔍  Tìm theo mã, phòng, tháng...");
        tfSearch.setPreferredSize(new Dimension(240, 36));

        // Lọc theo tháng
        JComboBox<String> cbMonthFilter = UITheme.comboBox(
            new String[]{"Tất cả tháng", "06/2026", "05/2026", "04/2026", "03/2026"}
        );
        cbMonthFilter.setPreferredSize(new Dimension(130, 36));

        // Lọc theo trạng thái chốt
        JComboBox<String> cbConfirmFilter = UITheme.comboBox(
            new String[]{"Tất cả", "✓ Đã chốt", "⏳ Chưa chốt"}
        );
        cbConfirmFilter.setPreferredSize(new Dimension(120, 36));

        JButton btnRefresh = UITheme.outlineBtn("↺");
        btnRefresh.setPreferredSize(new Dimension(44, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbMonthFilter);
        toolbar.add(cbConfirmFilter);
        toolbar.add(btnRefresh);

        // Khởi tạo bảng
        tableModel = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable(utilities);

        table = new JTable(tableModel);
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Độ rộng cột
        int[] widths = {70, 65, 70, 72, 72, 80, 85, 72, 72, 75, 85, 90, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer cột Mã
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer());
        // Renderer cột Tổng tiền — nổi bật
        table.getColumnModel().getColumn(11).setCellRenderer(totalRenderer());
        // Renderer cột Trạng thái chốt
        table.getColumnModel().getColumn(12).setCellRenderer(confirmRenderer());

        // Click hàng → đổ vào form
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromRow(row);
            }
        });

        // Tìm kiếm real-time
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(
                    (String) cbMonthFilter.getSelectedItem(),
                    (String) cbConfirmFilter.getSelectedItem(),
                    tfSearch.getText()
                );
            }
        });

        cbMonthFilter.addActionListener(e -> filterTable(
            (String) cbMonthFilter.getSelectedItem(),
            (String) cbConfirmFilter.getSelectedItem(),
            tfSearch.getText()
        ));

        cbConfirmFilter.addActionListener(e -> filterTable(
            (String) cbMonthFilter.getSelectedItem(),
            (String) cbConfirmFilter.getSelectedItem(),
            tfSearch.getText()
        ));

        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            cbMonthFilter.setSelectedIndex(0);
            cbConfirmFilter.setSelectedIndex(0);
            refreshTable(utilities);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Thanh tổng kết cuối bảng
        JPanel summaryBar = buildSummaryBar();

        p.add(toolbar,     BorderLayout.NORTH);
        p.add(scroll,      BorderLayout.CENTER);
        p.add(summaryBar,  BorderLayout.SOUTH);
        return p;
    }

    /** Thanh tổng kết: tổng tiền điện, nước của tháng hiển thị */
    private JPanel buildSummaryBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        p.setOpaque(false);

        long totalElec  = utilities.stream().mapToLong(Utility::getElectricFee).sum();
        long totalWater = utilities.stream().mapToLong(Utility::getWaterFee).sum();
        long totalAll   = utilities.stream().mapToLong(Utility::getTotalFee).sum();
        long confirmed  = utilities.stream().filter(Utility::isConfirmed).count();

        p.add(UITheme.badge("Tổng điện: " + String.format("%,d đ", totalElec),
            UITheme.WARNING_BG, UITheme.WARNING_TEXT));
        p.add(UITheme.badge("Tổng nước: " + String.format("%,d đ", totalWater),
            new Color(0xDBEAFE), UITheme.INFO_TEXT));
        p.add(UITheme.badge("Tổng cộng: " + String.format("%,d đ", totalAll),
            UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT));
        p.add(UITheme.badge("Đã chốt: " + confirmed + "/" + utilities.size(),
            UITheme.BG_SECONDARY, UITheme.TEXT_SECONDARY));
        return p;
    }

    // ── Logic CRUD ────────────────────────────────────────────────

    /** Thêm bản ghi điện nước mới */
    private void addUtility() {
        if (tfMonth.getText().trim().isEmpty()) {
            showWarn("Vui lòng nhập tháng (VD: 06/2026)!"); return;
        }
        try {
            double ePrev = parseDouble(tfElecPrev.getText());
            double eCurr = parseDouble(tfElecCurr.getText());
            double wPrev = parseDouble(tfWaterPrev.getText());
            double wCurr = parseDouble(tfWaterCurr.getText());
            if (eCurr < ePrev) { showWarn("Chỉ số điện cuối kỳ phải ≥ đầu kỳ!"); return; }
            if (wCurr < wPrev) { showWarn("Chỉ số nước cuối kỳ phải ≥ đầu kỳ!"); return; }

            Utility u = new Utility(
                tfId.getText().trim(),
                (String) cbRoom.getSelectedItem(),
                tfMonth.getText().trim(),
                ePrev, eCurr, wPrev, wCurr,
                parseLong(tfElecPrice.getText()),
                parseLong(tfWaterPrice.getText()),
                tfNote.getText().trim(),
                false
            );
            utilities.add(u);
            refreshTable(utilities);
            clearForm();
            showSuccess("Thêm chỉ số điện nước thành công!");
        } catch (Exception ex) {
            showWarn("Vui lòng nhập đúng định dạng số cho các chỉ số!");
        }
    }

    /** Cập nhật bản ghi đang chọn */
    private void editUtility() {
        if (editingUtility == null) { showWarn("Chọn bản ghi cần sửa từ bảng!"); return; }
        try {
            double ePrev = parseDouble(tfElecPrev.getText());
            double eCurr = parseDouble(tfElecCurr.getText());
            double wPrev = parseDouble(tfWaterPrev.getText());
            double wCurr = parseDouble(tfWaterCurr.getText());

            editingUtility.setRoomId((String) cbRoom.getSelectedItem());
            editingUtility.setMonth(tfMonth.getText().trim());
            editingUtility.setElectricPrev(ePrev);
            editingUtility.setElectricCurr(eCurr);
            editingUtility.setWaterPrev(wPrev);
            editingUtility.setWaterCurr(wCurr);
            editingUtility.setElectricUnitPrice(parseLong(tfElecPrice.getText()));
            editingUtility.setWaterUnitPrice(parseLong(tfWaterPrice.getText()));
            editingUtility.setNote(tfNote.getText().trim());
            refreshTable(utilities);
            showSuccess("Cập nhật thành công!");
        } catch (Exception ex) {
            showWarn("Vui lòng nhập đúng định dạng số!");
        }
    }

    /** Xóa bản ghi đang chọn */
    private void deleteUtility() {
        if (editingUtility == null) { showWarn("Chọn bản ghi cần xóa!"); return; }
        if (editingUtility.isConfirmed()) {
            showWarn("Không thể xóa bản ghi đã chốt chỉ số!"); return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "Xóa bản ghi " + editingUtility.getId() + " — phòng "
            + editingUtility.getRoomId() + " tháng " + editingUtility.getMonth() + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            utilities.remove(editingUtility);
            editingUtility = null;
            refreshTable(utilities);
            clearForm();
        }
    }

    /** Chốt chỉ số — không cho phép sửa sau khi chốt */
    private void confirmUtility() {
        if (editingUtility == null) { showWarn("Chọn bản ghi cần chốt!"); return; }
        if (editingUtility.isConfirmed()) {
            showWarn("Bản ghi này đã được chốt rồi!"); return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "<html>Chốt chỉ số phòng <b>" + editingUtility.getRoomId()
            + "</b> tháng <b>" + editingUtility.getMonth() + "</b>?<br>"
            + "Sau khi chốt sẽ không thể chỉnh sửa.</html>",
            "Xác nhận chốt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            editingUtility.setConfirmed(true);
            refreshTable(utilities);
            showSuccess("Đã chốt chỉ số thành công!");
        }
    }

    /** Lọc bảng theo tháng, trạng thái chốt và từ khóa */
    private void filterTable(String monthFilter, String confirmFilter, String keyword) {
        String q = keyword.toLowerCase().trim();
        List<Utility> filtered = utilities.stream()
            .filter(u -> {
                boolean matchMonth   = "Tất cả tháng".equals(monthFilter)
                    || u.getMonth().equals(monthFilter);
                boolean matchConfirm = "Tất cả".equals(confirmFilter)
                    || ("✓ Đã chốt".equals(confirmFilter)  && u.isConfirmed())
                    || ("⏳ Chưa chốt".equals(confirmFilter) && !u.isConfirmed());
                boolean matchSearch  = q.isEmpty()
                    || u.getId().toLowerCase().contains(q)
                    || u.getRoomId().toLowerCase().contains(q)
                    || u.getMonth().contains(q);
                return matchMonth && matchConfirm && matchSearch;
            })
            .collect(Collectors.toList());
        refreshTable(filtered);
    }

    /** Làm mới toàn bộ hàng trong bảng */
    private void refreshTable(List<Utility> list) {
        tableModel.setRowCount(0);
        for (Utility u : list) tableModel.addRow(u.toRow());
        lblCount.setText("Tổng cộng: " + utilities.size() + " bản ghi");
    }

    /** Click hàng → đổ dữ liệu vào form */
    private void fillFormFromRow(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        editingUtility = utilities.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst().orElse(null);
        if (editingUtility == null) return;

        tfId.setText(editingUtility.getId());
        tfMonth.setText(editingUtility.getMonth());
        tfElecPrev.setText(String.valueOf(editingUtility.getElectricPrev()));
        tfElecCurr.setText(String.valueOf(editingUtility.getElectricCurr()));
        tfWaterPrev.setText(String.valueOf(editingUtility.getWaterPrev()));
        tfWaterCurr.setText(String.valueOf(editingUtility.getWaterCurr()));
        tfElecPrice.setText(String.valueOf(editingUtility.getElectricUnitPrice()));
        tfWaterPrice.setText(String.valueOf(editingUtility.getWaterUnitPrice()));
        tfNote.setText(editingUtility.getNote());

        for (int i = 0; i < cbRoom.getItemCount(); i++) {
            if (editingUtility.getRoomId().equals(cbRoom.getItemAt(i))) {
                cbRoom.setSelectedIndex(i); break;
            }
        }
        updatePreview(); // Cập nhật preview tính tiền
    }

    /** Reset form về trạng thái thêm mới */
    private void clearForm() {
        editingUtility = null;
        tfId.setText(nextUtilityId());
        tfMonth.setText("06/2026");
        tfElecPrev.setText(""); tfElecCurr.setText("");
        tfWaterPrev.setText(""); tfWaterCurr.setText("");
        tfElecPrice.setText(String.valueOf(Utility.DEFAULT_ELECTRIC_UNIT_PRICE));
        tfWaterPrice.setText(String.valueOf(Utility.DEFAULT_WATER_UNIT_PRICE));
        tfNote.setText("");
        cbRoom.setSelectedIndex(0);
        lblPreviewElec.setText("0 đ");
        lblPreviewWater.setText("0 đ");
        lblPreviewTotal.setText("0 đ");
    }

    // ── Tiện ích ─────────────────────────────────────────────────

    private String nextUtilityId() {
        if (utilities.isEmpty()) return "UT0001";
        return Utility.nextId(utilities.get(utilities.size() - 1).getId());
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return 0; }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(2, 0, 2, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return l;
    }

    private JPanel singleRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(UITheme.formLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void addField(JPanel grid, String label, JComponent field) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.add(UITheme.formLabel(label), BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        grid.add(cell);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, "⚠ " + msg, "Lưu ý", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, "✅ " + msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Cell Renderers ────────────────────────────────────────────

    private TableCellRenderer idRenderer() {
        return (t, v, sel, focus, row, col) -> {
            JLabel l = new JLabel(v.toString());
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.PRIMARY);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        };
    }

    private TableCellRenderer totalRenderer() {
        return (t, v, sel, focus, row, col) -> {
            JLabel l = new JLabel(v.toString(), SwingConstants.RIGHT);
            l.setFont(UITheme.FONT_BOLD);
            l.setForeground(UITheme.SUCCESS_TEXT);
            l.setBorder(new EmptyBorder(0, 0, 0, 8));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        };
    }

    private TableCellRenderer confirmRenderer() {
        return (t, v, sel, focus, row, col) -> {
            String s  = v.toString();
            boolean ok = s.startsWith("✓");
            JLabel lbl = UITheme.badge(s,
                ok ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
                ok ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }
}
