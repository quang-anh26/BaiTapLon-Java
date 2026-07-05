package com.sdms.ui.admin;

import com.sdms.model.Contract;
import com.sdms.model.Invoice;
import com.sdms.model.Utility;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;
import java.awt.*;
import java.awt.event.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Panel quản lý chỉ số điện nước hàng tháng.
 * Logic nâng cao:
 *  1. Khi chọn phòng + tháng → tự động điền chỉ số đầu kỳ từ cuối kỳ tháng trước.
 *  2. Chặn tháng bị "nhảy cóc": phòng phải có dữ liệu liên tiếp (không bỏ tháng giữa).
 *  3. Khi chốt: chọn từng sinh viên trong phòng → hóa đơn chỉ tạo cho người đó;
 *     tiền phòng = tổng hợp đồng của tất cả thành viên đang active trong phòng.
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

    // ── Formatter tháng MM/yyyy ───────────────────────────────────
    private static final DateTimeFormatter MONTH_FMT =
        DateTimeFormatter.ofPattern("MM/yyyy");

    public UtilityPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        utilities.addAll(DatabaseService.getAllUtilities());
        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG_LIGHT);
        JPanel formPanel = buildForm();
        formPanel.setPreferredSize(new Dimension(480, Integer.MAX_VALUE));
        formPanel.setMinimumSize(new Dimension(480, 0));
        formPanel.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));
        center.add(formPanel, BorderLayout.WEST);
        center.add(buildTableArea(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
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
        left.add(title, BorderLayout.NORTH);
        left.add(breadcrumb, BorderLayout.SOUTH);
        lblCount = new JLabel("Tổng cộng: " + utilities.size() + " bản ghi");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);
        p.add(left, BorderLayout.WEST);
        p.add(lblCount, BorderLayout.EAST);
        return p;
    }

    // ── FORM nhập liệu ────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() { return new Dimension(480, super.getPreferredSize().height); }
            @Override public Dimension getMinimumSize()  { return new Dimension(480, 0); }
            @Override public Dimension getMaximumSize()  { return new Dimension(480, Integer.MAX_VALUE); }
        };
        wrapper.setBackground(UITheme.WHITE);
        wrapper.setBorder(new MatteBorder(0, 0, 0, 1, UITheme.BORDER));

        JPanel form = new JPanel();
        form.setBackground(UITheme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel sec = new JLabel("NHẬP CHỈ SỐ ĐIỆN NƯỚC");
        sec.setFont(UITheme.FONT_LABEL);
        sec.setForeground(UITheme.PRIMARY);
        sec.setBorder(new EmptyBorder(0, 0, 10, 0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        tfId         = UITheme.textField("");
        tfMonth      = UITheme.textField("");
        tfElecPrev   = UITheme.textField("");
        tfElecCurr   = UITheme.textField("");
        tfWaterPrev  = UITheme.textField("");
        tfWaterCurr  = UITheme.textField("");
        tfElecPrice  = UITheme.textField(String.valueOf(Utility.DEFAULT_ELECTRIC_UNIT_PRICE));
        tfWaterPrice = UITheme.textField(String.valueOf(Utility.DEFAULT_WATER_UNIT_PRICE));
        tfNote       = UITheme.textField("");

        // Chỉ số đầu kỳ chỉ đọc — do hệ thống tự điền
        tfElecPrev.setEditable(false);
        tfElecPrev.setBackground(new Color(0xF3F4F6));
        tfWaterPrev.setEditable(false);
        tfWaterPrev.setBackground(new Color(0xF3F4F6));

        String[] roomIds = DatabaseService.getAllRooms().stream()
            .map(r -> r.getId()).toArray(String[]::new);
        cbRoom = UITheme.comboBox(roomIds);

        tfId.setText(nextUtilityId());
        tfId.setEditable(false);
        tfId.setBackground(new Color(0xF3F4F6));

        // Khi đổi phòng hoặc nhập tháng → tự điền đầu kỳ
        cbRoom.addActionListener(e -> onRoomOrMonthChanged());
        tfMonth.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { onRoomOrMonthChanged(); }
        });

        KeyAdapter calcPreview = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { updatePreview(); }
        };
        tfElecCurr.addKeyListener(calcPreview);
        tfWaterCurr.addKeyListener(calcPreview);
        tfElecPrice.addKeyListener(calcPreview);
        tfWaterPrice.addKeyListener(calcPreview);

        JPanel row1 = makeRow2(makeFieldPanel("MÃ BẢN GHI", tfId), makeFieldPanel("THÁNG *", tfMonth));
        JPanel row2 = makeRow1("PHÒNG *", cbRoom);
        JLabel elecSec  = sectionLabel("⚡  ĐIỆN (kWh)");
        JLabel waterSec = sectionLabel("💧  NƯỚC (m³)");

        // Tooltip cho đầu kỳ
        tfElecPrev.setToolTipText("Tự động lấy từ chỉ số cuối kỳ tháng trước");
        tfWaterPrev.setToolTipText("Tự động lấy từ chỉ số cuối kỳ tháng trước");

        JPanel elecGrid  = makeRow2(makeFieldPanel("CHỈ SỐ ĐẦU KỲ", tfElecPrev),  makeFieldPanel("CHỈ SỐ CUỐI KỲ", tfElecCurr));
        JPanel waterGrid = makeRow2(makeFieldPanel("CHỈ SỐ ĐẦU KỲ", tfWaterPrev), makeFieldPanel("CHỈ SỐ CUỐI KỲ", tfWaterCurr));
        JPanel priceGrid = makeRow2(makeFieldPanel("ĐƠN GIÁ ĐIỆN (đ/kWh)", tfElecPrice), makeFieldPanel("ĐƠN GIÁ NƯỚC (đ/m³)", tfWaterPrice));
        JPanel preview = buildPreviewPanel();
        JPanel noteRow = makeRow1("GHI CHÚ", tfNote);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnAdd    = UITheme.primaryBtn("Thêm");
        JButton btnEdit   = UITheme.warningBtn("Sửa");
        JButton btnDelete = UITheme.dangerBtn("Xóa");
        JButton btnReset  = UITheme.outlineBtn("Làm mới");
        btnRow.add(btnAdd); btnRow.add(btnEdit); btnRow.add(btnDelete); btnRow.add(btnReset);

        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false); btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnConfirm = UITheme.successBtn("Chốt chỉ số");
        JButton btnExport  = UITheme.purpleBtn("Xuất báo cáo");
        btnRow2.add(btnConfirm); btnRow2.add(btnExport);

        btnAdd.addActionListener(e    -> addUtility());
        btnEdit.addActionListener(e   -> editUtility());
        btnDelete.addActionListener(e -> deleteUtility());
        btnReset.addActionListener(e  -> clearForm());
        btnConfirm.addActionListener(e-> confirmUtility());
        btnExport.addActionListener(e -> exportUtilityReport());

        form.add(sec);       form.add(Box.createVerticalStrut(4));
        form.add(row1);      form.add(Box.createVerticalStrut(8));
        form.add(row2);      form.add(Box.createVerticalStrut(8));
        form.add(elecSec);   form.add(Box.createVerticalStrut(4));
        form.add(elecGrid);  form.add(Box.createVerticalStrut(8));
        form.add(waterSec);  form.add(Box.createVerticalStrut(4));
        form.add(waterGrid); form.add(Box.createVerticalStrut(8));
        form.add(sectionLabel("💰  ĐƠN GIÁ")); form.add(Box.createVerticalStrut(4));
        form.add(priceGrid); form.add(Box.createVerticalStrut(10));
        form.add(preview);   form.add(Box.createVerticalStrut(8));
        form.add(noteRow);   form.add(Box.createVerticalStrut(12));
        form.add(btnRow);    form.add(Box.createVerticalStrut(6));
        form.add(btnRow2);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Logic 1: Tự động điền chỉ số đầu kỳ ─────────────────────
    /**
     * Khi chọn phòng hoặc nhập tháng:
     *  - Lấy bản ghi tháng (month-1) của phòng đó.
     *  - Điền electricCurr → tfElecPrev, waterCurr → tfWaterPrev.
     *  - Nếu không có dữ liệu tháng trước → để trống (tháng đầu tiên của phòng).
     */
    private void onRoomOrMonthChanged() {
        String roomId = (String) cbRoom.getSelectedItem();
        String monthStr = tfMonth.getText().trim();
        if (roomId == null || monthStr.isEmpty()) return;

        YearMonth selectedMonth;
        try {
            selectedMonth = YearMonth.parse(monthStr, MONTH_FMT);
        } catch (Exception ex) {
            return; // tháng chưa hợp lệ, không điền
        }

        YearMonth prevMonth = selectedMonth.minusMonths(1);
        String prevMonthStr = prevMonth.format(MONTH_FMT);

        // Tìm bản ghi tháng trước của phòng này
        Utility prev = utilities.stream()
            .filter(u -> u.getRoomId().equals(roomId) && u.getMonth().equals(prevMonthStr))
            .findFirst().orElse(null);

        // Nếu không có trong cache → thử DB
        if (prev == null) {
            prev = DatabaseService.getUtility(roomId, prevMonthStr);
        }

        if (prev != null) {
            tfElecPrev.setText(String.valueOf(prev.getElectricCurr()));
            tfWaterPrev.setText(String.valueOf(prev.getWaterCurr()));
        } else {
            // Tháng đầu tiên của phòng hoặc chưa có dữ liệu → để trống cho nhập tay
            tfElecPrev.setEditable(true);
            tfElecPrev.setBackground(UITheme.WHITE);
            tfWaterPrev.setEditable(true);
            tfWaterPrev.setBackground(UITheme.WHITE);
            tfElecPrev.setText("");
            tfWaterPrev.setText("");
        }
        updatePreview();
    }

    // ── Logic 2: Kiểm tra tháng không được nhảy cóc ──────────────
    /**
     * Kiểm tra tháng chọn hợp lệ:
     *  - Bản ghi phòng đó phải có dữ liệu liên tiếp không gián đoạn.
     *  - Ví dụ: đã có T2, T3 → T4 hợp lệ; T5 không hợp lệ (bỏ qua T4).
     *  - Tháng đầu tiên (phòng chưa có bản ghi nào) → luôn hợp lệ.
     * @return null nếu hợp lệ, chuỗi lỗi nếu không hợp lệ.
     */
    private String validateMonthSequence(String roomId, YearMonth targetMonth) {
        List<YearMonth> existingMonths = utilities.stream()
            .filter(u -> u.getRoomId().equals(roomId))
            .filter(u -> !u.equals(editingUtility)) // bỏ qua bản ghi đang sửa
            .map(u -> {
                try { return YearMonth.parse(u.getMonth(), MONTH_FMT); }
                catch (Exception e) { return null; }
            })
            .filter(m -> m != null)
            .sorted()
            .collect(Collectors.toList());

        if (existingMonths.isEmpty()) return null; // phòng chưa có dữ liệu → OK

        YearMonth latestMonth = existingMonths.get(existingMonths.size() - 1);

        // Kiểm tra tháng đã tồn tại chưa
        if (existingMonths.contains(targetMonth)) {
            return "Phòng " + roomId + " đã có dữ liệu tháng " + targetMonth.format(MONTH_FMT) + "!";
        }

        // Tháng mới phải là tháng kế tiếp của tháng mới nhất
        if (!targetMonth.equals(latestMonth.plusMonths(1))) {
            return "Tháng không hợp lệ! Phòng " + roomId + " hiện có dữ liệu đến "
                + latestMonth.format(MONTH_FMT)
                + ".\nTháng tiếp theo phải là " + latestMonth.plusMonths(1).format(MONTH_FMT) + ".";
        }
        return null;
    }

    // ── Panel preview tính tiền real-time ─────────────────────────
    private JLabel lblPreviewElec, lblPreviewWater, lblPreviewTotal;

    private JPanel buildPreviewPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 6, 0));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        p.setOpaque(false);
        lblPreviewElec  = previewChip("Tiền điện", "2000 đ", UITheme.WARNING_BG, UITheme.WARNING_TEXT);
        lblPreviewWater = previewChip("Tiền nước", "6000 đ", new Color(0xDBEAFE), UITheme.INFO_TEXT);
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

    private void updatePreview() {
        try {
            double elecPrev   = parseDouble(tfElecPrev.getText());
            double elecCurr   = parseDouble(tfElecCurr.getText());
            double waterPrev  = parseDouble(tfWaterPrev.getText());
            double waterCurr  = parseDouble(tfWaterCurr.getText());
            long   elecPrice  = parseLong(tfElecPrice.getText());
            long   waterPrice = parseLong(tfWaterPrice.getText());
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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("");
        tfSearch.setPreferredSize(new Dimension(240, 36));

        JComboBox<String> cbMonthFilter = UITheme.comboBox(
            new String[]{"Tất cả tháng", "06/2026", "05/2026", "04/2026", "03/2026"}
        );
        cbMonthFilter.setPreferredSize(new Dimension(130, 36));

        JComboBox<String> cbConfirmFilter = UITheme.comboBox(
            new String[]{"Tất cả", "✓ Đã chốt", "⏳ Chưa chốt"}
        );
        cbConfirmFilter.setPreferredSize(new Dimension(120, 36));

        JButton btnRefresh = UITheme.outlineBtn("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbMonthFilter);
        toolbar.add(cbConfirmFilter);
        toolbar.add(btnRefresh);

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

        int[] widths = {70, 65, 70, 72, 72, 80, 85, 72, 72, 75, 85, 90, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer());
        table.getColumnModel().getColumn(11).setCellRenderer(totalRenderer());
        table.getColumnModel().getColumn(12).setCellRenderer(confirmRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromRow(row);
            }
        });

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
            utilities.clear();
            utilities.addAll(DatabaseService.getAllUtilities());
            refreshTable(utilities);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel summaryBar = buildSummaryBar();

        p.add(toolbar,    BorderLayout.NORTH);
        p.add(scroll,     BorderLayout.CENTER);
        p.add(summaryBar, BorderLayout.SOUTH);
        return p;
    }

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

    private void addUtility() {
        String monthStr = tfMonth.getText().trim();
        if (monthStr.isEmpty()) {
            showWarn("Vui lòng nhập tháng (VD: 06/2026)!"); return;
        }
        if (!monthStr.matches("\\d{2}/\\d{4}")) {
            showWarn("Tháng không đúng định dạng! Vui lòng nhập theo dạng MM/yyyy (VD: 06/2026)."); return;
        }

        String roomId = (String) cbRoom.getSelectedItem();
        YearMonth targetMonth;
        try {
            targetMonth = YearMonth.parse(monthStr, MONTH_FMT);
        } catch (Exception ex) {
            showWarn("Tháng không hợp lệ!"); return;
        }

        // Logic 2: Kiểm tra tháng không nhảy cóc
        String monthError = validateMonthSequence(roomId, targetMonth);
        if (monthError != null) {
            showWarn(monthError); return;
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
                roomId,
                monthStr,
                ePrev, eCurr, wPrev, wCurr,
                parseLong(tfElecPrice.getText()),
                parseLong(tfWaterPrice.getText()),
                tfNote.getText().trim(),
                false
            );
            if (!DatabaseService.addUtility(u)) {
                showWarn("Lưu vào database thất bại! Kiểm tra kết nối."); return;
            }
            utilities.add(u);
            refreshTable(utilities);
            clearForm();
            showSuccess("Thêm chỉ số điện nước thành công!");
        } catch (Exception ex) {
            showWarn("Vui lòng nhập đúng định dạng số cho các chỉ số!");
        }
    }

    private void editUtility() {
        if (editingUtility == null) { showWarn("Chọn bản ghi cần sửa từ bảng!"); return; }
        if (editingUtility.isConfirmed()) {
            showWarn("Không thể sửa bản ghi đã chốt chỉ số!"); return;
        }
        try {
            double ePrev = parseDouble(tfElecPrev.getText());
            double eCurr = parseDouble(tfElecCurr.getText());
            double wPrev = parseDouble(tfWaterPrev.getText());
            double wCurr = parseDouble(tfWaterCurr.getText());
            if (eCurr < ePrev) { showWarn("Chỉ số điện cuối kỳ phải ≥ đầu kỳ!"); return; }
            if (wCurr < wPrev) { showWarn("Chỉ số nước cuối kỳ phải ≥ đầu kỳ!"); return; }

            editingUtility.setRoomId((String) cbRoom.getSelectedItem());
            editingUtility.setMonth(tfMonth.getText().trim());
            editingUtility.setElectricPrev(ePrev);
            editingUtility.setElectricCurr(eCurr);
            editingUtility.setWaterPrev(wPrev);
            editingUtility.setWaterCurr(wCurr);
            editingUtility.setElectricUnitPrice(parseLong(tfElecPrice.getText()));
            editingUtility.setWaterUnitPrice(parseLong(tfWaterPrice.getText()));
            editingUtility.setNote(tfNote.getText().trim());
            if (!DatabaseService.updateUtility(editingUtility)) {
                showWarn("Cập nhật database thất bại! Kiểm tra kết nối."); return;
            }
            refreshTable(utilities);
            showSuccess("Cập nhật thành công!");
        } catch (Exception ex) {
            showWarn("Vui lòng nhập đúng định dạng số!");
        }
    }

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
            if (!DatabaseService.deleteUtility(editingUtility.getId())) {
                showWarn("Xóa database thất bại! Kiểm tra kết nối."); return;
            }
            utilities.remove(editingUtility);
            editingUtility = null;
            refreshTable(utilities);
            clearForm();
        }
    }

    // ── Logic 3: Chốt chỉ số — chọn từng sinh viên trong phòng ──
    /**
     * Khi chốt:
     *  1. Lấy danh sách tất cả hợp đồng ACTIVE trong phòng.
     *  2. Tính tổng tiền phòng = cộng monthlyFee của tất cả hợp đồng active (toàn bộ thành viên).
     *  3. Hiển thị dialog cho admin chọn sinh viên cụ thể cần tạo hóa đơn.
     *  4. Hóa đơn chỉ tạo cho sinh viên được chọn, với:
     *     - roomFee = tổng tiền phòng tất cả thành viên (toàn bộ phòng)
     *     - electricFee, waterFee = tính từ bản ghi điện nước
     */
    private void confirmUtility() {
        if (editingUtility == null) { showWarn("Chọn bản ghi cần chốt!"); return; }
        if (editingUtility.isConfirmed()) {
            showWarn("Bản ghi này đã được chốt rồi!"); return;
        }

        String roomId = editingUtility.getRoomId();
        String month  = editingUtility.getMonth();

        // Lấy tất cả hợp đồng ACTIVE trong phòng
        List<Contract> activeContracts = DatabaseService.getAllContracts().stream()
            .filter(c -> c.getRoomId().equals(roomId) && c.getStatus() == Contract.Status.ACTIVE)
            .collect(Collectors.toList());

        if (activeContracts.isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this,
                "<html>Phòng <b>" + roomId + "</b> chưa có hợp đồng hiệu lực.<br>"
                + "Vẫn chốt chỉ số mà không tạo hóa đơn?</html>",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                doConfirmWithoutInvoice();
            }
            return;
        }

        // Tổng tiền phòng = cộng toàn bộ hợp đồng active
        long totalRoomFee = activeContracts.stream()
            .mapToLong(Contract::getMonthlyFee)
            .sum();

        // Danh sách sinh viên để chọn
        String[] studentOptions = activeContracts.stream()
            .map(c -> c.getStudentId() + " - " + c.getStudentName()
                    + " (" + String.format("%,d đ/tháng", c.getMonthlyFee()) + ")")
            .toArray(String[]::new);

        // Dialog chọn sinh viên
        JPanel dlgPanel = new JPanel(new BorderLayout(0, 10));
        dlgPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel info = new JLabel(
            "<html><b>Phòng " + roomId + " — Tháng " + month + "</b><br>"
            + "Tổng tiền phòng (tất cả " + activeContracts.size() + " thành viên): "
            + "<b>" + String.format("%,d đ", totalRoomFee) + "</b><br>"
            + "Tiền điện nước: <b>" + String.format("%,d đ", editingUtility.getTotalFee()) + "</b><br><br>"
            + "Chọn sinh viên cần tạo hóa đơn:</html>"
        );
        info.setFont(UITheme.FONT_SMALL);

        JComboBox<String> cbStudent = new JComboBox<>(studentOptions);
        cbStudent.setPreferredSize(new Dimension(400, 36));

        dlgPanel.add(info, BorderLayout.NORTH);
        dlgPanel.add(cbStudent, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this, dlgPanel,
            "Chốt chỉ số & Tạo hóa đơn",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        int idx = cbStudent.getSelectedIndex();
        if (idx < 0) return;
        Contract selectedContract = activeContracts.get(idx);

        // Kiểm tra hóa đơn đã tồn tại cho sinh viên này trong tháng này
        boolean invoiceExists = DatabaseService.getAllInvoices().stream()
            .anyMatch(inv -> inv.getStudentId().equals(selectedContract.getStudentId())
                         && inv.getMonth().equals(month));
        if (invoiceExists) {
            showWarn("Sinh viên " + selectedContract.getStudentName()
                + " đã có hóa đơn tháng " + month + " rồi!");
            return;
        }

        // Chốt bản ghi điện nước
        editingUtility.setConfirmed(true);
        if (!DatabaseService.updateUtility(editingUtility)) {
            showWarn("Lưu trạng thái chốt thất bại! Kiểm tra kết nối."); return;
        }

        // Tạo hóa đơn CHỈ cho sinh viên được chọn
        // roomFee = tổng tiền phòng của tất cả thành viên trong phòng
        Invoice inv = new Invoice(
            DatabaseService.nextInvoiceId(),
            selectedContract.getStudentId(),
            selectedContract.getStudentName(),
            roomId,
            month,
            totalRoomFee,                       // tổng tiền phòng tất cả thành viên
            editingUtility.getElectricFee(),
            editingUtility.getWaterFee(),
            false
        );

        if (DatabaseService.addInvoice(inv)) {
            refreshTable(utilities);
            showSuccess("Đã chốt chỉ số và tạo hóa đơn " + inv.getId()
                + " cho " + selectedContract.getStudentName()
                + " — phòng " + roomId + " tháng " + month + "!"
                + "\n💰 Tiền phòng (cả phòng): " + String.format("%,d đ", totalRoomFee)
                + "\n⚡ Tiền điện: " + String.format("%,d đ", editingUtility.getElectricFee())
                + "\n💧 Tiền nước: " + String.format("%,d đ", editingUtility.getWaterFee()));
        } else {
            showSuccess("Đã chốt chỉ số!\n⚠ Tạo hóa đơn tự động thất bại, vui lòng kiểm tra lại.");
        }
        refreshTable(utilities);
    }

    private void doConfirmWithoutInvoice() {
        editingUtility.setConfirmed(true);
        if (!DatabaseService.updateUtility(editingUtility)) {
            showWarn("Lưu trạng thái chốt thất bại! Kiểm tra kết nối."); return;
        }
        refreshTable(utilities);
        showSuccess("Đã chốt chỉ số! (Không tạo hóa đơn vì phòng chưa có hợp đồng hiệu lực.)");
    }

    private void filterTable(String monthFilter, String confirmFilter, String keyword) {
        String q = keyword.toLowerCase().trim();
        List<Utility> filtered = utilities.stream()
            .filter(u -> {
                boolean matchMonth   = "Tất cả tháng".equals(monthFilter) || u.getMonth().equals(monthFilter);
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

    private void refreshTable(List<Utility> list) {
        tableModel.setRowCount(0);
        for (Utility u : list) tableModel.addRow(u.toRow());
        lblCount.setText("Tổng cộng: " + utilities.size() + " bản ghi");
    }

    private void fillFormFromRow(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        editingUtility = utilities.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst().orElse(null);
        if (editingUtility == null) return;

        // Khi fill từ bảng → cho phép sửa đầu kỳ nếu bản ghi chưa chốt
        boolean isConfirmed = editingUtility.isConfirmed();
        tfElecPrev.setEditable(!isConfirmed);
        tfElecPrev.setBackground(isConfirmed ? new Color(0xF3F4F6) : UITheme.WHITE);
        tfWaterPrev.setEditable(!isConfirmed);
        tfWaterPrev.setBackground(isConfirmed ? new Color(0xF3F4F6) : UITheme.WHITE);

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
        updatePreview();
    }

    private void clearForm() {
        editingUtility = null;
        tfId.setText(nextUtilityId());
        tfMonth.setText("");
        tfElecPrev.setEditable(false);
        tfElecPrev.setBackground(new Color(0xF3F4F6));
        tfWaterPrev.setEditable(false);
        tfWaterPrev.setBackground(new Color(0xF3F4F6));
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
    private String nextUtilityId() { return DatabaseService.nextUtilityId(); }

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

    private JPanel makeFieldPanel(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeRow2(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.add(left); row.add(right);
        return row;
    }

    private JPanel makeRow1(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.add(makeFieldPanel(label, field), BorderLayout.CENTER);
        return row;
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

    private void exportUtilityReport() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setSelectedFile(new java.io.File("BaoCao_DienNuoc.csv"));
        fc.setDialogTitle("Lưu báo cáo điện nước");
        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new java.io.File(file.getPath() + ".csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF');
            pw.println("Mã BG,Phòng,Tháng,Điện đầu,Điện cuối,Dùng (kWh),Tiền điện,Nước đầu,Nước cuối,Dùng (m³),Tiền nước,Tổng tiền,Trạng thái");
            for (Utility u : utilities) {
                pw.printf("%s,%s,%s,%.1f,%.1f,%.1f,%d,%.1f,%.1f,%.1f,%d,%d,%s%n",
                    u.getId(), u.getRoomId(), u.getMonth(),
                    u.getElectricPrev(), u.getElectricCurr(),
                    u.getElectricCurr() - u.getElectricPrev(), u.getElectricFee(),
                    u.getWaterPrev(), u.getWaterCurr(),
                    u.getWaterCurr() - u.getWaterPrev(), u.getWaterFee(),
                    u.getElectricFee() + u.getWaterFee(),
                    u.isConfirmed() ? "Đã chốt" : "Chưa chốt");
            }
            JOptionPane.showMessageDialog(this,
                "✅ Đã xuất " + utilities.size() + " bản ghi ra:\n" + file.getAbsolutePath(),
                "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Xuất thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}