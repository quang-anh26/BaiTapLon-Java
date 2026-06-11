package com.sdms.ui.admin;

import com.sdms.model.Contract;
import com.sdms.model.Student;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel quản lý hợp đồng thuê phòng ký túc xá.
 * Hỗ trợ: Thêm, Sửa, Xóa, Tìm kiếm, Lọc theo trạng thái.
 * Layout: Form bên trái — Bảng dữ liệu bên phải (giống StudentPanel).
 */
public class ContractPanel extends JPanel {

    // ── Dữ liệu ───────────────────────────────────────────────────
    private final List<Contract> contracts = new ArrayList<>();
    private Contract editingContract = null;

    // ── Table ─────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JLabel            lblCount;

    // ── Form fields ───────────────────────────────────────────────
    private JTextField        tfId, tfStudentId, tfStudentName, tfStartDate, tfEndDate, tfFee, tfNote;
    private JComboBox<String> cbRoom, cbStatus;

    // ── Cột bảng ─────────────────────────────────────────────────
    private static final String[] COLS = {
        "Mã HĐ", "Mã SV", "Tên sinh viên", "Phòng",
        "Ngày BĐ", "Ngày KT", "Tiền/tháng", "Trạng thái"
    };

    // ── Danh sách phòng lấy từ DataStore ─────────────────────────
    private static final String[] STATUSES = {
        "Đang hiệu lực", "Đã hết hạn", "Đã chấm dứt", "Chờ ký kết"
    };

    public ContractPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        initSampleData();  // Tạo dữ liệu mẫu ban đầu
        add(buildHeader(), BorderLayout.NORTH);

        // Chia đôi: form trái | bảng phải
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTableArea()
        );
        split.setDividerLocation(310);
        split.setDividerSize(2);
        split.setBackground(UITheme.BORDER);
        split.setBorder(null);
        split.setResizeWeight(0.25);
        add(split, BorderLayout.CENTER);
    }

    // ── Tạo dữ liệu mẫu ──────────────────────────────────────────
    private void initSampleData() {
        contracts.add(new Contract("HĐ0001","SV001248","Nguyễn Văn An",   "A301",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
        contracts.add(new Contract("HĐ0002","SV001247","Trần Thị Bình",   "B204",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
        contracts.add(new Contract("HĐ0003","SV001243","Vũ Thị Lan",      "A204",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
        contracts.add(new Contract("HĐ0004","SV001242","Đặng Minh Tuấn",  "B102",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
        contracts.add(new Contract("HĐ0005","SV001241","Bùi Thị Mai",     "C305",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
        contracts.add(new Contract("HĐ0006","SV001244","Hoàng Văn Nam",   "D405",
            LocalDate.of(2023,9,1), LocalDate.of(2024,8,31), 750_000, "Đã rời phòng", Contract.Status.EXPIRED));
        contracts.add(new Contract("HĐ0007","SV001249","Nguyễn Thị Lan Anh","A301",
            LocalDate.of(2024,9,1), LocalDate.of(2025,8,31), 850_000, "", Contract.Status.ACTIVE));
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("📄  Quản lý hợp đồng");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Dashboard / Quản lý hợp đồng");
        breadcrumb.setFont(UITheme.FONT_TINY);
        breadcrumb.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(breadcrumb, BorderLayout.SOUTH);

        lblCount = new JLabel("Tổng cộng: " + contracts.size() + " hợp đồng");
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
        JLabel sec = new JLabel("THÊM / CHỈNH SỬA HỢP ĐỒNG");
        sec.setFont(UITheme.FONT_LABEL);
        sec.setForeground(UITheme.PRIMARY);
        sec.setBorder(new EmptyBorder(0, 0, 10, 0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // Khởi tạo các trường nhập liệu
        tfId          = UITheme.textField("HĐ0001");
        tfStudentId   = UITheme.textField("SV001248");
        tfStudentName = UITheme.textField("Họ và tên sinh viên");
        tfStartDate   = UITheme.textField("dd/MM/yyyy");
        tfEndDate     = UITheme.textField("dd/MM/yyyy");
        tfFee         = UITheme.textField("850000");
        tfNote        = UITheme.textField("Ghi chú thêm...");

        // Lấy danh sách phòng từ DataStore
        String[] roomIds = DatabaseService.getAllRooms().stream()
            .map(r -> r.getId()).toArray(String[]::new);
        cbRoom   = UITheme.comboBox(roomIds);
        cbStatus = UITheme.comboBox(STATUSES);

        // Tự động điền mã HĐ tiếp theo
        tfId.setText(nextContractId());

        // Khi nhập mã SV → tự điền tên SV
        tfStudentId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                autoFillStudentName(tfStudentId.getText().trim());
            }
        });

        // Grid 2 cột cho các trường
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        addField(grid, "Mã hợp đồng *", tfId);
        addField(grid, "Trạng thái",     cbStatus);
        addField(grid, "Mã sinh viên *", tfStudentId);
        addField(grid, "Phòng *",        cbRoom);
        addField(grid, "Ngày bắt đầu *", tfStartDate);
        addField(grid, "Ngày kết thúc *",tfEndDate);
        addField(grid, "Tiền/tháng (đ)", tfFee);

        // Tên SV — full width
        JPanel nameRow = singleRow("Tên sinh viên", tfStudentName);

        // Ghi chú — full width
        JPanel noteRow = singleRow("Ghi chú", tfNote);

        // ── Nút thao tác ─────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnAdd    = UITheme.primaryBtn("➕ Thêm");
        JButton btnEdit   = UITheme.warningBtn("✏ Sửa");
        JButton btnDelete = UITheme.dangerBtn("🗑 Xóa");
        JButton btnReset  = UITheme.outlineBtn("↺ Làm mới");

        btnRow.add(btnAdd);
        btnRow.add(btnEdit);
        btnRow.add(btnDelete);
        btnRow.add(btnReset);

        // Nút xuất báo cáo
        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false);
        btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnExpire = UITheme.dangerBtn("⚠ Chấm dứt HĐ");
        JButton btnExport = UITheme.successBtn("📊 Xuất danh sách");
        btnRow2.add(btnExpire);
        btnRow2.add(btnExport);

        // Gắn sự kiện
        btnAdd.addActionListener(e    -> addContract());
        btnEdit.addActionListener(e   -> editContract());
        btnDelete.addActionListener(e -> deleteContract());
        btnReset.addActionListener(e  -> clearForm());
        btnExpire.addActionListener(e -> terminateContract());
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "✅ Đã xuất danh sách hợp đồng thành công!", "Thông báo",
            JOptionPane.INFORMATION_MESSAGE));

        // Ghép form
        form.add(sec);
        form.add(grid);
        form.add(Box.createVerticalStrut(8));
        form.add(nameRow);
        form.add(Box.createVerticalStrut(6));
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

    /** Tạo một hàng label + field chiếm full chiều ngang */
    private JPanel singleRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(UITheme.formLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Thêm một ô (label trên + field dưới) vào grid */
    private void addField(JPanel grid, String label, JComponent field) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.add(UITheme.formLabel(label), BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        grid.add(cell);
    }

    // ── Khu vực bảng dữ liệu ─────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Thanh toolbar: tìm kiếm + lọc
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("🔍  Tìm mã HĐ, mã SV, tên sinh viên...");
        tfSearch.setPreferredSize(new Dimension(280, 36));

        JComboBox<String> cbFilter = UITheme.comboBox(
            new String[]{"Tất cả trạng thái", "Đang hiệu lực", "Đã hết hạn", "Đã chấm dứt", "Chờ ký kết"}
        );
        cbFilter.setPreferredSize(new Dimension(160, 36));

        JButton btnRefresh = UITheme.outlineBtn("↺ Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbFilter);
        toolbar.add(btnRefresh);

        // Khởi tạo bảng
        tableModel = new DefaultTableModel(null, COLS) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable(contracts);

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

        // Độ rộng cột
        int[] widths = {80, 85, 160, 70, 90, 90, 100, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer: Mã HĐ màu primary
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer());
        // Renderer: Trạng thái badge màu
        table.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());

        // Click hàng → đổ dữ liệu vào form
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromRow(row);
            }
        });

        // Tìm kiếm real-time
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable((String) cbFilter.getSelectedItem(), tfSearch.getText());
            }
        });

        // Lọc theo trạng thái
        cbFilter.addActionListener(e ->
            filterTable((String) cbFilter.getSelectedItem(), tfSearch.getText())
        );

        // Làm mới bảng
        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            cbFilter.setSelectedIndex(0);
            refreshTable(contracts);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);

        // Thống kê nhanh cuối bảng
        JPanel statsBar = buildStatsBar();

        p.add(toolbar,  BorderLayout.NORTH);
        p.add(scroll,   BorderLayout.CENTER);
        p.add(statsBar, BorderLayout.SOUTH);
        return p;
    }

    /** Thanh thống kê nhanh: tổng, đang hiệu lực, hết hạn, chờ ký */
    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        p.setOpaque(false);

        long active    = contracts.stream().filter(c -> c.getStatus() == Contract.Status.ACTIVE).count();
        long expired   = contracts.stream().filter(c -> c.getStatus() == Contract.Status.EXPIRED).count();
        long pending   = contracts.stream().filter(c -> c.getStatus() == Contract.Status.PENDING).count();
        long terminated= contracts.stream().filter(c -> c.getStatus() == Contract.Status.TERMINATED).count();

        p.add(statChip("Tổng: " + contracts.size(),  UITheme.INFO_BG,    UITheme.INFO_TEXT));
        p.add(statChip("Hiệu lực: " + active,        UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT));
        p.add(statChip("Hết hạn: " + expired,        UITheme.DANGER_BG,  UITheme.DANGER_TEXT));
        p.add(statChip("Chờ ký: " + pending,         UITheme.WARNING_BG, UITheme.WARNING_TEXT));
        p.add(statChip("Chấm dứt: " + terminated,    UITheme.BG_SECONDARY, UITheme.TEXT_SECONDARY));
        return p;
    }

    /** Chip thống kê nhỏ */
    private JLabel statChip(String text, Color bg, Color fg) {
        return UITheme.badge(text, bg, fg);
    }

    // ── Logic CRUD ────────────────────────────────────────────────

    /** Thêm hợp đồng mới */
    private void addContract() {
        // Validate bắt buộc
        if (tfStudentId.getText().trim().isEmpty()) {
            showWarn("Vui lòng nhập mã sinh viên!"); return;
        }
        if (tfStartDate.getText().trim().isEmpty() || tfEndDate.getText().trim().isEmpty()) {
            showWarn("Vui lòng nhập ngày bắt đầu và kết thúc!"); return;
        }
        LocalDate start = Contract.parseDate(tfStartDate.getText().trim());
        LocalDate end   = Contract.parseDate(tfEndDate.getText().trim());
        if (start == null || end == null) {
            showWarn("Định dạng ngày không hợp lệ! Dùng dd/MM/yyyy"); return;
        }
        if (!end.isAfter(start)) {
            showWarn("Ngày kết thúc phải sau ngày bắt đầu!"); return;
        }
        long fee = parseFee(tfFee.getText().trim());

        Contract c = new Contract(
            tfId.getText().trim(),
            tfStudentId.getText().trim(),
            tfStudentName.getText().trim(),
            (String) cbRoom.getSelectedItem(),
            start, end, fee,
            tfNote.getText().trim(),
            Contract.Status.valueOf(statusFromText((String) cbStatus.getSelectedItem()))
        );
        contracts.add(c);
        refreshTable(contracts);
        clearForm();
        showSuccess("Thêm hợp đồng thành công!");
    }

    /** Cập nhật hợp đồng đang chọn */
    private void editContract() {
        if (editingContract == null) {
            showWarn("Chọn hợp đồng cần sửa từ bảng!"); return;
        }
        LocalDate start = Contract.parseDate(tfStartDate.getText().trim());
        LocalDate end   = Contract.parseDate(tfEndDate.getText().trim());
        if (start == null || end == null) {
            showWarn("Định dạng ngày không hợp lệ!"); return;
        }
        editingContract.setStudentId(tfStudentId.getText().trim());
        editingContract.setStudentName(tfStudentName.getText().trim());
        editingContract.setRoomId((String) cbRoom.getSelectedItem());
        editingContract.setStartDate(start);
        editingContract.setEndDate(end);
        editingContract.setMonthlyFee(parseFee(tfFee.getText().trim()));
        editingContract.setNote(tfNote.getText().trim());
        editingContract.setStatus(
            Contract.Status.valueOf(statusFromText((String) cbStatus.getSelectedItem()))
        );
        refreshTable(contracts);
        showSuccess("Cập nhật hợp đồng thành công!");
    }

    /** Xóa hợp đồng đang chọn */
    private void deleteContract() {
        if (editingContract == null) {
            showWarn("Chọn hợp đồng cần xóa!"); return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "Xóa hợp đồng " + editingContract.getId() + " của " + editingContract.getStudentName() + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            contracts.remove(editingContract);
            editingContract = null;
            refreshTable(contracts);
            clearForm();
        }
    }

    /** Chấm dứt hợp đồng trước hạn */
    private void terminateContract() {
        if (editingContract == null) {
            showWarn("Chọn hợp đồng cần chấm dứt!"); return;
        }
        if (editingContract.getStatus() != Contract.Status.ACTIVE) {
            showWarn("Chỉ có thể chấm dứt hợp đồng đang hiệu lực!"); return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "<html>Chấm dứt hợp đồng <b>" + editingContract.getId() + "</b>?<br>"
            + "Hành động này không thể hoàn tác.</html>",
            "Xác nhận chấm dứt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            editingContract.setStatus(Contract.Status.TERMINATED);
            editingContract.setEndDate(LocalDate.now());
            refreshTable(contracts);
            showSuccess("Đã chấm dứt hợp đồng " + editingContract.getId());
        }
    }

    /** Lọc bảng theo chuỗi tìm kiếm và trạng thái */
    private void filterTable(String statusFilter, String keyword) {
        String q = keyword.toLowerCase().trim();
        List<Contract> filtered = contracts.stream()
            .filter(c -> {
                boolean matchStatus = "Tất cả trạng thái".equals(statusFilter)
                    || c.getStatusText().equals(statusFilter);
                boolean matchSearch = q.isEmpty()
                    || c.getId().toLowerCase().contains(q)
                    || c.getStudentId().toLowerCase().contains(q)
                    || c.getStudentName().toLowerCase().contains(q)
                    || c.getRoomId().toLowerCase().contains(q);
                return matchStatus && matchSearch;
            })
            .collect(Collectors.toList());
        refreshTable(filtered);
    }

    /** Làm mới toàn bộ hàng trong bảng */
    private void refreshTable(List<Contract> list) {
        tableModel.setRowCount(0);
        for (Contract c : list) tableModel.addRow(c.toRow());
        lblCount.setText("Tổng cộng: " + contracts.size() + " hợp đồng");
    }

    /** Click hàng bảng → đổ dữ liệu vào form */
    private void fillFormFromRow(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        editingContract = contracts.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst().orElse(null);
        if (editingContract == null) return;

        tfId.setText(editingContract.getId());
        tfStudentId.setText(editingContract.getStudentId());
        tfStudentName.setText(editingContract.getStudentName());
        tfStartDate.setText(editingContract.getStartDateStr());
        tfEndDate.setText(editingContract.getEndDateStr());
        tfFee.setText(String.valueOf(editingContract.getMonthlyFee()));
        tfNote.setText(editingContract.getNote());
        cbStatus.setSelectedItem(editingContract.getStatusText());

        // Chọn phòng trong combobox
        for (int i = 0; i < cbRoom.getItemCount(); i++) {
            if (editingContract.getRoomId().equals(cbRoom.getItemAt(i))) {
                cbRoom.setSelectedIndex(i); break;
            }
        }
    }

    /** Xóa trắng form, reset về trạng thái thêm mới */
    private void clearForm() {
        editingContract = null;
        tfId.setText(nextContractId());
        tfStudentId.setText("");
        tfStudentName.setText("");
        tfStartDate.setText("");
        tfEndDate.setText("");
        tfFee.setText("850000");
        tfNote.setText("");
        cbRoom.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
    }

    /** Tự điền tên sinh viên khi nhập mã SV */
    private void autoFillStudentName(String svId) {
        DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equalsIgnoreCase(svId))
            .findFirst()
            .ifPresent(s -> {
                tfStudentName.setText(s.getFullName());
                // Tự chọn phòng nếu có
                if (!s.getRoomId().isEmpty()) {
                    for (int i = 0; i < cbRoom.getItemCount(); i++) {
                        if (s.getRoomId().equals(cbRoom.getItemAt(i))) {
                            cbRoom.setSelectedIndex(i); break;
                        }
                    }
                }
            });
    }

    // ── Tiện ích ─────────────────────────────────────────────────

    /** Sinh mã HĐ tiếp theo */
    private String nextContractId() {
        if (contracts.isEmpty()) return "HĐ0001";
        return Contract.nextId(contracts.get(contracts.size() - 1).getId());
    }

    /** Parse tiền từ chuỗi, trả về 0 nếu không hợp lệ */
    private long parseFee(String text) {
        try { return Long.parseLong(text.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 850_000L; }
    }

    /** Chuyển text tiếng Việt → tên enum */
    private String statusFromText(String text) {
        return switch (text) {
            case "Đang hiệu lực" -> "ACTIVE";
            case "Đã hết hạn"    -> "EXPIRED";
            case "Đã chấm dứt"   -> "TERMINATED";
            default              -> "PENDING";
        };
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, "⚠ " + msg, "Lưu ý", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, "✅ " + msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Cell Renderers ────────────────────────────────────────────

    /** Renderer cho cột Mã HĐ — màu primary, bold */
    private TableCellRenderer idRenderer() {
        return (t, v, sel, focus, row, col) -> {
            JLabel lbl = new JLabel(v.toString());
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.PRIMARY);
            lbl.setBorder(new EmptyBorder(0, 10, 0, 0));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }

    /** Renderer cho cột Trạng thái — badge màu */
    private TableCellRenderer statusRenderer() {
        return (t, v, sel, focus, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Đang hiệu lực" -> UITheme.SUCCESS_BG;
                case "Đã hết hạn"    -> UITheme.DANGER_BG;
                case "Đã chấm dứt"   -> UITheme.BG_SECONDARY;
                default              -> UITheme.WARNING_BG;
            };
            Color fg = switch (s) {
                case "Đang hiệu lực" -> UITheme.SUCCESS_TEXT;
                case "Đã hết hạn"    -> UITheme.DANGER_TEXT;
                case "Đã chấm dứt"   -> UITheme.TEXT_SECONDARY;
                default              -> UITheme.WARNING_TEXT;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }
}
