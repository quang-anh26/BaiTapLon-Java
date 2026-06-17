package com.sdms.ui.admin;

import com.sdms.model.Violation;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Panel quản lý vi phạm nội quy ký túc xá.
 * Hỗ trợ: Thêm, Sửa, Xóa, Tìm kiếm, Lọc theo mức độ/trạng thái, Xử lý vi phạm.
 * Dữ liệu được tải trực tiếp từ SQL Server qua DatabaseService.
 */
public class ViolationPanel extends JPanel {

    // ── Dữ liệu ───────────────────────────────────────────────────
    private final List<Violation> violations = new ArrayList<>();
    private Violation editingViolation = null;

    // ── Table ─────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JLabel            lblCount;

    // ── Form fields ───────────────────────────────────────────────
    private JTextField        tfId, tfStudentId, tfStudentName, tfDate,
                              tfFine, tfHandledBy, tfNote;
    private JComboBox<String> cbRoom, cbType, cbSeverity, cbStatus;
    private JTextArea         taDescription;

    // ── Cột bảng ─────────────────────────────────────────────────
    private static final String[] COLS = {
        "Mã VP", "Mã SV", "Tên sinh viên", "Phòng",
        "Ngày", "Loại vi phạm", "Mức độ", "Tiền phạt", "Trạng thái"
    };

    private static final String[] SEVERITIES = {"Nhẹ", "Trung bình", "Nặng", "Rất nặng"};
    private static final String[] STATUSES   = {"Chưa xử lý", "Đã xử lý", "Đang khiếu nại"};

    public ViolationPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        loadDataFromDatabase();          // ← tải từ SQL Server thay vì dữ liệu ảo
        add(buildHeader(), BorderLayout.NORTH);

        // Fixed layout — form width locked, not draggable
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

    // ── Tải dữ liệu từ SQL Server ─────────────────────────────────
    private void loadDataFromDatabase() {
        violations.clear();
        try {
            List<Violation> dbList = DatabaseService.getAllViolations();
            violations.addAll(dbList);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "⚠ Không thể tải danh sách vi phạm từ cơ sở dữ liệu!\n" + e.getMessage(),
                "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("⚠  Quản lý vi phạm nội quy");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Dashboard / Quản lý vi phạm");
        breadcrumb.setFont(UITheme.FONT_TINY);
        breadcrumb.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title,      BorderLayout.NORTH);
        left.add(breadcrumb, BorderLayout.SOUTH);

        lblCount = new JLabel("Tổng cộng: " + violations.size() + " vi phạm");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        p.add(left,     BorderLayout.WEST);
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

        // Tiêu đề form
        JLabel sec = new JLabel("GHI NHẬN VI PHẠM");
        sec.setFont(UITheme.FONT_LABEL);
        sec.setForeground(UITheme.DANGER);
        sec.setBorder(new EmptyBorder(0, 0, 10, 0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // Khởi tạo fields
        tfId          = UITheme.textField("");
        tfStudentId   = UITheme.textField("");
        tfStudentName = UITheme.textField("");
        tfDate        = UITheme.textField("");
        tfFine        = UITheme.textField("");
        tfHandledBy   = UITheme.textField("");
        tfNote        = UITheme.textField("");

        // Comboboxes
        String[] roomIds = DatabaseService.getAllRooms().stream()
            .map(r -> r.getId()).toArray(String[]::new);
        cbRoom     = UITheme.comboBox(roomIds);
        cbType     = UITheme.comboBox(Violation.VIOLATION_TYPES);
        cbSeverity = UITheme.comboBox(SEVERITIES);
        cbStatus   = UITheme.comboBox(STATUSES);

        // Mô tả chi tiết — TextArea
        taDescription = new JTextArea(3, 1);
        taDescription.setFont(UITheme.FONT_BODY);
        taDescription.setForeground(UITheme.TEXT_PRIMARY);
        taDescription.setBackground(UITheme.BG_SECONDARY);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        JScrollPane descScroll = new JScrollPane(taDescription);
        descScroll.setBorder(null);
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        tfStudentId.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                autoFillStudent(tfStudentId.getText().trim());
            }
        });

        cbSeverity.addActionListener(e -> suggestFine());

        tfId.setText(DatabaseService.nextViolationId());  // lấy ID tiếp theo từ DB
        tfDate.setText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // ── Rows ─────────────────────────────────────────────────
        JPanel row1 = makeRow2(makeFieldPanel("MÃ VI PHẠM",     tfId),
                               makeFieldPanel("NGÀY VI PHẠM *", tfDate));
        JPanel row2 = makeRow2(makeFieldPanel("MÃ SINH VIÊN *", tfStudentId),
                               makeFieldPanel("PHÒNG",           cbRoom));
        JPanel row3 = makeRow2(makeFieldPanel("LOẠI VI PHẠM *", cbType),
                               makeFieldPanel("MỨC ĐỘ *",       cbSeverity));
        JPanel row4 = makeRow2(makeFieldPanel("TIỀN PHẠT (Đ)",  tfFine),
                               makeFieldPanel("TRẠNG THÁI",     cbStatus));
        JPanel row5 = makeRow1("NGƯỜI XỬ LÝ", tfHandledBy);
        JPanel row6 = makeRow1("TÊN SINH VIÊN", tfStudentName);

        // Mô tả full width
        JPanel descRow = new JPanel(new BorderLayout(0, 4));
        descRow.setOpaque(false); descRow.setAlignmentX(LEFT_ALIGNMENT);
        descRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JLabel descLbl = new JLabel("MÔ TẢ CHI TIẾT");
        descLbl.setFont(UITheme.FONT_LABEL); descLbl.setForeground(UITheme.TEXT_SECONDARY);
        descRow.add(descLbl, BorderLayout.NORTH);
        descRow.add(descScroll, BorderLayout.CENTER);

        JPanel noteRow = makeRow1("GHI CHÚ", tfNote);

        // Nút thao tác hàng 1
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnAdd    = UITheme.primaryBtn("□ Thêm");
        JButton btnEdit   = UITheme.warningBtn("□ Sửa");
        JButton btnDelete = UITheme.dangerBtn("□ Xóa");
        JButton btnReset  = UITheme.outlineBtn("□ Làm mới");
        btnRow.add(btnAdd); btnRow.add(btnEdit); btnRow.add(btnDelete); btnRow.add(btnReset);

        // Nút xử lý vi phạm
        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false); btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnProcess = UITheme.successBtn("□ Đánh dấu đã xử lý");
        JButton btnExport  = UITheme.purpleBtn("□ Xuất báo cáo");
        btnRow2.add(btnProcess); btnRow2.add(btnExport);

        // Gắn sự kiện
        btnAdd.addActionListener(e     -> addViolation());
        btnEdit.addActionListener(e    -> editViolation());
        btnDelete.addActionListener(e  -> deleteViolation());
        btnReset.addActionListener(e   -> clearForm());
        btnProcess.addActionListener(e -> processViolation());
        btnExport.addActionListener(e  -> JOptionPane.showMessageDialog(this,
            "✅ Đã xuất báo cáo vi phạm thành công!", "Thông báo",
            JOptionPane.INFORMATION_MESSAGE));

        // Ghép form
        form.add(sec);       form.add(Box.createVerticalStrut(4));
        form.add(row1);      form.add(Box.createVerticalStrut(8));
        form.add(row2);      form.add(Box.createVerticalStrut(8));
        form.add(row3);      form.add(Box.createVerticalStrut(8));
        form.add(row4);      form.add(Box.createVerticalStrut(8));
        form.add(row5);      form.add(Box.createVerticalStrut(8));
        form.add(row6);      form.add(Box.createVerticalStrut(8));
        form.add(descRow);   form.add(Box.createVerticalStrut(6));
        form.add(noteRow);   form.add(Box.createVerticalStrut(12));
        form.add(btnRow);    form.add(Box.createVerticalStrut(6));
        form.add(btnRow2);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Khu vực bảng dữ liệu ─────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("");
        tfSearch.setPreferredSize(new Dimension(240, 36));

        JComboBox<String> cbSevFilter = UITheme.comboBox(
            new String[]{"Tất cả mức độ", "Nhẹ", "Trung bình", "Nặng", "Rất nặng"}
        );
        cbSevFilter.setPreferredSize(new Dimension(130, 36));

        JComboBox<String> cbStatusFilter = UITheme.comboBox(
            new String[]{"Tất cả trạng thái", "Chưa xử lý", "Đã xử lý", "Đang khiếu nại"}
        );
        cbStatusFilter.setPreferredSize(new Dimension(150, 36));

        JButton btnRefresh = UITheme.outlineBtn("□ Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbSevFilter);
        toolbar.add(cbStatusFilter);
        toolbar.add(btnRefresh);

        // Khởi tạo bảng
        tableModel = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable(violations);

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
        int[] widths = {75, 80, 160, 65, 90, 140, 90, 90, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderers màu
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(severityRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(fineRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(statusRenderer());

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
                    (String) cbSevFilter.getSelectedItem(),
                    (String) cbStatusFilter.getSelectedItem(),
                    tfSearch.getText()
                );
            }
        });

        cbSevFilter.addActionListener(e -> filterTable(
            (String) cbSevFilter.getSelectedItem(),
            (String) cbStatusFilter.getSelectedItem(),
            tfSearch.getText()
        ));

        cbStatusFilter.addActionListener(e -> filterTable(
            (String) cbSevFilter.getSelectedItem(),
            (String) cbStatusFilter.getSelectedItem(),
            tfSearch.getText()
        ));

        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            cbSevFilter.setSelectedIndex(0);
            cbStatusFilter.setSelectedIndex(0);
            loadDataFromDatabase();     // ← tải lại từ DB
            refreshTable(violations);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);

        // Thanh thống kê
        JPanel statsBar = buildStatsBar();

        p.add(toolbar,  BorderLayout.NORTH);
        p.add(scroll,   BorderLayout.CENTER);
        p.add(statsBar, BorderLayout.SOUTH);
        return p;
    }

    /** Thanh thống kê nhanh */
    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        p.setOpaque(false);

        long pending   = violations.stream().filter(v -> v.getStatus() == Violation.Status.PENDING).count();
        long processed = violations.stream().filter(v -> v.getStatus() == Violation.Status.PROCESSED).count();
        long appealing = violations.stream().filter(v -> v.getStatus() == Violation.Status.APPEALING).count();
        long totalFine = violations.stream().mapToLong(Violation::getFine).sum();
        long highSev   = violations.stream()
            .filter(v -> v.getSeverity() == Violation.Severity.HIGH
                      || v.getSeverity() == Violation.Severity.CRITICAL).count();

        p.add(UITheme.badge("Tổng: " + violations.size(),          UITheme.INFO_BG,      UITheme.INFO_TEXT));
        p.add(UITheme.badge("Chưa xử lý: " + pending,              UITheme.DANGER_BG,    UITheme.DANGER_TEXT));
        p.add(UITheme.badge("Đã xử lý: " + processed,              UITheme.SUCCESS_BG,   UITheme.SUCCESS_TEXT));
        p.add(UITheme.badge("Khiếu nại: " + appealing,             UITheme.WARNING_BG,   UITheme.WARNING_TEXT));
        p.add(UITheme.badge("Vi phạm nặng: " + highSev,            UITheme.DANGER_BG,    UITheme.DANGER_TEXT));
        p.add(UITheme.badge("Tổng phạt: " + String.format("%,d đ", totalFine),
            UITheme.PURPLE_BG, UITheme.PURPLE));
        return p;
    }

    // ── Logic CRUD ────────────────────────────────────────────────

    /** Thêm vi phạm mới vào DB */
    private void addViolation() {
        if (tfStudentId.getText().trim().isEmpty()) {
            showWarn("Vui lòng nhập mã sinh viên!"); return;
        }
        if (tfDate.getText().trim().isEmpty()) {
            showWarn("Vui lòng nhập ngày vi phạm!"); return;
        }
        LocalDate date = Violation.parseDate(tfDate.getText().trim());
        if (date == null) {
            showWarn("Định dạng ngày không hợp lệ! Dùng dd/MM/yyyy"); return;
        }

        Violation v = new Violation(
            tfId.getText().trim(),
            tfStudentId.getText().trim(),
            tfStudentName.getText().trim(),
            (String) cbRoom.getSelectedItem(),
            date,
            (String) cbType.getSelectedItem(),
            taDescription.getText().trim(),
            severityFromText((String) cbSeverity.getSelectedItem()),
            parseFine(tfFine.getText()),
            tfHandledBy.getText().trim(),
            statusFromText((String) cbStatus.getSelectedItem()),
            tfNote.getText().trim()
        );

        boolean ok = DatabaseService.addViolation(v);
        if (ok) {
            loadDataFromDatabase();
            refreshTable(violations);
            clearForm();
            showSuccess("Ghi nhận vi phạm thành công!");
        } else {
            showWarn("Không thể lưu vi phạm vào cơ sở dữ liệu!");
        }
    }

    /** Cập nhật vi phạm đang chọn vào DB */
    private void editViolation() {
        if (editingViolation == null) {
            showWarn("Chọn vi phạm cần sửa từ bảng!"); return;
        }
        LocalDate date = Violation.parseDate(tfDate.getText().trim());
        if (date == null) { showWarn("Định dạng ngày không hợp lệ!"); return; }

        editingViolation.setStudentId(tfStudentId.getText().trim());
        editingViolation.setStudentName(tfStudentName.getText().trim());
        editingViolation.setRoomId((String) cbRoom.getSelectedItem());
        editingViolation.setDate(date);
        editingViolation.setType((String) cbType.getSelectedItem());
        editingViolation.setDescription(taDescription.getText().trim());
        editingViolation.setSeverity(severityFromText((String) cbSeverity.getSelectedItem()));
        editingViolation.setFine(parseFine(tfFine.getText()));
        editingViolation.setHandledBy(tfHandledBy.getText().trim());
        editingViolation.setStatus(statusFromText((String) cbStatus.getSelectedItem()));
        editingViolation.setNote(tfNote.getText().trim());

        boolean ok = DatabaseService.updateViolation(editingViolation);
        if (ok) {
            loadDataFromDatabase();
            refreshTable(violations);
            showSuccess("Cập nhật vi phạm thành công!");
        } else {
            showWarn("Không thể cập nhật vi phạm trong cơ sở dữ liệu!");
        }
    }

    /** Xóa vi phạm đang chọn khỏi DB */
    private void deleteViolation() {
        if (editingViolation == null) {
            showWarn("Chọn vi phạm cần xóa!"); return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "Xóa vi phạm " + editingViolation.getId()
            + " của " + editingViolation.getStudentName() + "?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            boolean ok = DatabaseService.deleteViolation(editingViolation.getId());
            if (ok) {
                editingViolation = null;
                loadDataFromDatabase();
                refreshTable(violations);
                clearForm();
            } else {
                showWarn("Không thể xóa vi phạm trong cơ sở dữ liệu!");
            }
        }
    }

    /** Đánh dấu vi phạm đã được xử lý và lưu vào DB */
    private void processViolation() {
        if (editingViolation == null) {
            showWarn("Chọn vi phạm cần đánh dấu xử lý!"); return;
        }
        if (editingViolation.getStatus() == Violation.Status.PROCESSED) {
            showWarn("Vi phạm này đã được xử lý rồi!"); return;
        }
        String handler = tfHandledBy.getText().trim().isEmpty()
            ? "Quản trị viên" : tfHandledBy.getText().trim();

        int r = JOptionPane.showConfirmDialog(this,
            "<html>Đánh dấu vi phạm <b>" + editingViolation.getId()
            + "</b> là đã xử lý?<br>Người xử lý: <b>" + handler + "</b></html>",
            "Xác nhận xử lý", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            editingViolation.setStatus(Violation.Status.PROCESSED);
            editingViolation.setHandledBy(handler);
            boolean ok = DatabaseService.updateViolation(editingViolation);
            if (ok) {
                loadDataFromDatabase();
                refreshTable(violations);
                showSuccess("Đã cập nhật trạng thái xử lý!");
            } else {
                showWarn("Không thể cập nhật trạng thái trong cơ sở dữ liệu!");
            }
        }
    }

    /** Lọc bảng (lọc trên danh sách đã tải từ DB) */
    private void filterTable(String sevFilter, String statusFilter, String keyword) {
        String q = keyword.toLowerCase().trim();
        List<Violation> filtered = violations.stream()
            .filter(v -> {
                boolean matchSev    = "Tất cả mức độ".equals(sevFilter)
                    || v.getSeverityText().equals(sevFilter);
                boolean matchStatus = "Tất cả trạng thái".equals(statusFilter)
                    || v.getStatusText().equals(statusFilter);
                boolean matchSearch = q.isEmpty()
                    || v.getId().toLowerCase().contains(q)
                    || v.getStudentId().toLowerCase().contains(q)
                    || v.getStudentName().toLowerCase().contains(q)
                    || v.getType().toLowerCase().contains(q)
                    || v.getRoomId().toLowerCase().contains(q);
                return matchSev && matchStatus && matchSearch;
            })
            .collect(Collectors.toList());
        refreshTable(filtered);
    }

    /** Làm mới bảng */
    private void refreshTable(List<Violation> list) {
        tableModel.setRowCount(0);
        for (Violation v : list) tableModel.addRow(v.toRow());
        lblCount.setText("Tổng cộng: " + violations.size() + " vi phạm");
    }

    /** Click hàng → đổ vào form */
    private void fillFormFromRow(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        editingViolation = violations.stream()
            .filter(v -> v.getId().equals(id))
            .findFirst().orElse(null);
        if (editingViolation == null) return;

        tfId.setText(editingViolation.getId());
        tfStudentId.setText(editingViolation.getStudentId());
        tfStudentName.setText(editingViolation.getStudentName());
        tfDate.setText(editingViolation.getDateStr());
        tfFine.setText(String.valueOf(editingViolation.getFine()));
        tfHandledBy.setText(editingViolation.getHandledBy());
        tfNote.setText(editingViolation.getNote());
        taDescription.setText(editingViolation.getDescription());

        cbSeverity.setSelectedItem(editingViolation.getSeverityText());
        cbStatus.setSelectedItem(editingViolation.getStatusText());
        cbType.setSelectedItem(editingViolation.getType());

        for (int i = 0; i < cbRoom.getItemCount(); i++) {
            if (editingViolation.getRoomId().equals(cbRoom.getItemAt(i))) {
                cbRoom.setSelectedIndex(i); break;
            }
        }
    }

    /** Xóa trắng form */
    private void clearForm() {
        editingViolation = null;
        tfId.setText(DatabaseService.nextViolationId());  // lấy ID tiếp theo từ DB
        tfStudentId.setText(""); tfStudentName.setText("");
        tfDate.setText(LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        tfFine.setText("0"); tfHandledBy.setText(""); tfNote.setText("");
        taDescription.setText("");
        cbRoom.setSelectedIndex(0);
        cbType.setSelectedIndex(0);
        cbSeverity.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
    }

    /** Tự động điền tên SV khi nhập mã — tra cứu từ DB */
    private void autoFillStudent(String svId) {
        DatabaseService.getAllStudents().stream()
            .filter(s -> s.getId().equalsIgnoreCase(svId))
            .findFirst()
            .ifPresent(s -> {
                tfStudentName.setText(s.getFullName());
                if (!s.getRoomId().isEmpty()) {
                    for (int i = 0; i < cbRoom.getItemCount(); i++) {
                        if (s.getRoomId().equals(cbRoom.getItemAt(i))) {
                            cbRoom.setSelectedIndex(i); break;
                        }
                    }
                }
            });
    }

    /** Gợi ý tiền phạt theo mức độ vi phạm */
    private void suggestFine() {
        String sev = (String) cbSeverity.getSelectedItem();
        String suggested = switch (sev) {
            case "Nhẹ"       -> "0";
            case "Trung bình"-> "200000";
            case "Nặng"      -> "500000";
            case "Rất nặng"  -> "1000000";
            default          -> "0";
        };
        tfFine.setText(suggested);
    }

    // ── Tiện ích ─────────────────────────────────────────────────

    private long parseFine(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private Violation.Severity severityFromText(String text) {
        return switch (text) {
            case "Trung bình" -> Violation.Severity.MEDIUM;
            case "Nặng"       -> Violation.Severity.HIGH;
            case "Rất nặng"   -> Violation.Severity.CRITICAL;
            default           -> Violation.Severity.LOW;
        };
    }

    private Violation.Status statusFromText(String text) {
        return switch (text) {
            case "Đã xử lý"        -> Violation.Status.PROCESSED;
            case "Đang khiếu nại"  -> Violation.Status.APPEALING;
            default                -> Violation.Status.PENDING;
        };
    }

    private JPanel makeFieldPanel(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER);
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
            l.setForeground(UITheme.DANGER);
            l.setBorder(new EmptyBorder(0, 10, 0, 0));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        };
    }

    private TableCellRenderer severityRenderer() {
        return (t, v, sel, focus, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Nhẹ"       -> UITheme.SUCCESS_BG;
                case "Trung bình"-> UITheme.WARNING_BG;
                case "Nặng"      -> UITheme.DANGER_BG;
                case "Rất nặng"  -> new Color(0xFCE7F3);
                default          -> UITheme.BG_SECONDARY;
            };
            Color fg = switch (s) {
                case "Nhẹ"       -> UITheme.SUCCESS_TEXT;
                case "Trung bình"-> UITheme.WARNING_TEXT;
                case "Nặng"      -> UITheme.DANGER_TEXT;
                case "Rất nặng"  -> new Color(0x9D174D);
                default          -> UITheme.TEXT_SECONDARY;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }

    private TableCellRenderer fineRenderer() {
        return (t, v, sel, focus, row, col) -> {
            String s = v.toString();
            JLabel l = new JLabel(s, SwingConstants.RIGHT);
            l.setFont("Không".equals(s) ? UITheme.FONT_SMALL : UITheme.FONT_BOLD);
            l.setForeground("Không".equals(s) ? UITheme.TEXT_MUTED : UITheme.DANGER);
            l.setBorder(new EmptyBorder(0, 0, 0, 8));
            l.setOpaque(true);
            l.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return l;
        };
    }

    private TableCellRenderer statusRenderer() {
        return (t, v, sel, focus, row, col) -> {
            String s = v.toString();
            Color bg = switch (s) {
                case "Đã xử lý"       -> UITheme.SUCCESS_BG;
                case "Chưa xử lý"     -> UITheme.DANGER_BG;
                case "Đang khiếu nại" -> UITheme.WARNING_BG;
                default               -> UITheme.BG_SECONDARY;
            };
            Color fg = switch (s) {
                case "Đã xử lý"       -> UITheme.SUCCESS_TEXT;
                case "Chưa xử lý"     -> UITheme.DANGER_TEXT;
                case "Đang khiếu nại" -> UITheme.WARNING_TEXT;
                default               -> UITheme.TEXT_SECONDARY;
            };
            JLabel lbl = UITheme.badge(s, bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }
}