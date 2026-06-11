package com.sdms.ui.admin;

import com.sdms.model.Student;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class StudentPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;

    // Form fields
    private JTextField  tfId, tfName, tfBirth, tfIdCard, tfPhone, tfEmail, tfFaculty, tfClass, tfAddress;
    private JComboBox<String> cbGender, cbUniversity, cbRoom, cbStatus;

    private Student editingStudent = null;

    private static final String[] COLS = {"Mã SV","Họ tên","Phòng","Trường","SĐT","Email","Trạng thái"};
    private static final String[] UNIVERSITIES = {"ĐHBK Hà Nội","ĐHQGHN","ĐH Kinh tế","HV Tài chính","ĐH Bách khoa HCM","Khác"};
    private static final String[] ROOMS  = {"","A301","A204","B204","B102","C112","C305","D201","D405"};
    private static final String[] STATUS = {"Đang ở","Mới đăng ký","Chờ duyệt","Đã rời"};

    public StudentPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTableArea());
        split.setDividerLocation(330);
        split.setDividerSize(2);
        split.setBackground(UITheme.BORDER);
        split.setBorder(null);
        split.setResizeWeight(0.25);
        add(split, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, UITheme.BORDER), new EmptyBorder(12,20,12,20)));

        JLabel title = new JLabel("👥  Quản lý sinh viên");
        title.setFont(UITheme.FONT_H2); title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel breadcrumb = new JLabel("Dashboard / Quản lý sinh viên");
        breadcrumb.setFont(UITheme.FONT_TINY); breadcrumb.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0,2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(breadcrumb, BorderLayout.SOUTH);

        JLabel count = new JLabel("Tổng cộng: " + DatabaseService.getAllStudents().size() + " sinh viên");
        count.setFont(UITheme.FONT_SMALL); count.setForeground(UITheme.TEXT_SECONDARY);

        p.add(left, BorderLayout.WEST);
        p.add(count, BorderLayout.EAST);
        return p;
    }

    // ── FORM ──────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.WHITE);
        wrapper.setBorder(new MatteBorder(0,0,0,1, UITheme.BORDER));

        JPanel form = new JPanel();
        form.setBackground(UITheme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Section title
        JLabel sec = new JLabel("THÊM / CHỈNH SỬA SINH VIÊN");
        sec.setFont(UITheme.FONT_LABEL); sec.setForeground(UITheme.PRIMARY);
        sec.setBorder(new EmptyBorder(0,0,10,0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // Fields in 2-column grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        tfId         = UITheme.textField("SV001250");
        tfName       = UITheme.textField("Họ và tên...");
        tfBirth      = UITheme.textField("dd/mm/yyyy");
        cbGender     = UITheme.comboBox(new String[]{"Nam","Nữ","Khác"});
        tfIdCard     = UITheme.textField("Số CCCD");
        tfPhone      = UITheme.textField("09xxxxxxxx");
        tfEmail      = UITheme.textField("email@...");
        cbUniversity = UITheme.comboBox(UNIVERSITIES);
        tfFaculty    = UITheme.textField("Tên khoa");
        tfClass      = UITheme.textField("Mã lớp");
        cbRoom       = UITheme.comboBox(ROOMS);
        cbStatus     = UITheme.comboBox(STATUS);

        addFormField(grid, "Mã sinh viên *", tfId);
        addFormField(grid, "Ngày sinh",       tfBirth);
        addFormRow2(grid, "Họ và tên *",      tfName);
        addFormField(grid, "Giới tính",       cbGender);
        addFormField(grid, "CCCD",            tfIdCard);
        addFormField(grid, "Điện thoại",      tfPhone);
        addFormField(grid, "Email",           tfEmail);
        addFormField(grid, "Trường học",      cbUniversity);
        addFormField(grid, "Khoa",            tfFaculty);
        addFormField(grid, "Lớp",             tfClass);
        addFormField(grid, "Phòng",           cbRoom);
        addFormField(grid, "Trạng thái",      cbStatus);

        // Address - full width
        JPanel addrPanel = new JPanel(new BorderLayout(0, 4));
        addrPanel.setOpaque(false);
        addrPanel.setAlignmentX(LEFT_ALIGNMENT);
        addrPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        tfAddress = UITheme.textField("Số nhà, đường, tỉnh/TP...");
        addrPanel.add(UITheme.formLabel("Địa chỉ"), BorderLayout.NORTH);
        addrPanel.add(tfAddress, BorderLayout.CENTER);

        // Auto-fill new ID
        tfId.setText(DataStore.nextStudentId());

        // Buttons
        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow1.setOpaque(false); btnRow1.setAlignmentX(LEFT_ALIGNMENT);
        btnRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnAdd    = UITheme.primaryBtn("➕ Thêm");
        JButton btnEdit   = UITheme.warningBtn("✏ Sửa");
        JButton btnDelete = UITheme.dangerBtn("🗑 Xóa");
        JButton btnReset  = UITheme.outlineBtn("↺ Làm mới");

        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false); btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnExcel = UITheme.successBtn("📊 Xuất Excel");
        JButton btnPdf   = UITheme.dangerBtn("📄 Xuất PDF");

        btnRow1.add(btnAdd); btnRow1.add(btnEdit); btnRow1.add(btnDelete); btnRow1.add(btnReset);
        btnRow2.add(btnExcel); btnRow2.add(btnPdf);

        // Actions
        btnAdd.addActionListener(e -> addStudent());
        btnEdit.addActionListener(e -> editStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnReset.addActionListener(e -> clearForm());
        btnExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "✅ Đã xuất Excel thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        btnPdf.addActionListener(e   -> JOptionPane.showMessageDialog(this, "✅ Đã xuất PDF thành công!",   "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        form.add(sec);
        form.add(grid);
        form.add(Box.createVerticalStrut(8));
        form.add(addrPanel);
        form.add(Box.createVerticalStrut(12));
        form.add(btnRow1);
        form.add(Box.createVerticalStrut(6));
        form.add(btnRow2);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void addFormField(JPanel grid, String label, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        wrapper.add(UITheme.formLabel(label), BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        grid.add(wrapper);
    }

    private void addFormRow2(JPanel grid, String label, JComponent field) {
        // spans 2 cols conceptually - we just skip one cell
        addFormField(grid, label, field);
        grid.add(new JPanel() {{ setOpaque(false); }});
    }

    // ── TABLE AREA ────────────────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        tfSearch = UITheme.textField("🔍  Tìm kiếm theo mã, tên, phòng...");
        tfSearch.setPreferredSize(new Dimension(280, 36));
        JComboBox<String> cbStatusFilter = UITheme.comboBox(new String[]{"Tất cả trạng thái","Đang ở","Mới đăng ký","Chờ duyệt","Đã rời"});
        cbStatusFilter.setPreferredSize(new Dimension(150, 36));
        JComboBox<String> cbUniFilter    = UITheme.comboBox(new String[]{"Tất cả trường", "ĐHBK Hà Nội","ĐHQGHN","ĐH Kinh tế","HV Tài chính"});
        cbUniFilter.setPreferredSize(new Dimension(140, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbStatusFilter);
        toolbar.add(cbUniFilter);

        // Build table
        tableModel = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable(DatabaseService.getAllStudents());

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

        // Column widths
        int[] widths = {90, 160, 70, 120, 100, 150, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        // ID color
        table.getColumnModel().getColumn(0).setCellRenderer(primaryColorRenderer());

        // Row click -> fill form
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromRow(row);
            }
        });

        // Search listener
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String q = tfSearch.getText().toLowerCase();
                List<Student> filtered = DatabaseService.getAllStudents().stream()
                    .filter(s -> s.getId().toLowerCase().contains(q)
                              || s.getFullName().toLowerCase().contains(q)
                              || s.getRoomId().toLowerCase().contains(q)
                              || s.getPhone().contains(q))
                    .collect(Collectors.toList());
                refreshTable(filtered);
            }
        });

        cbStatusFilter.addActionListener(e -> {
            String sel = (String)cbStatusFilter.getSelectedItem();
            List<Student> filtered = DatabaseService.getAllStudents().stream()
                .filter(s -> "Tất cả trạng thái".equals(sel) || s.getStatus().equals(sel))
                .collect(Collectors.toList());
            refreshTable(filtered);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);

        // Pagination bar
        JPanel paging = buildPagingBar();

        p.add(toolbar, BorderLayout.NORTH);
        p.add(scroll,  BorderLayout.CENTER);
        p.add(paging,  BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildPagingBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        p.setOpaque(false);
        JLabel info = new JLabel("Hiển thị 1–" + Math.min(10, DatabaseService.getAllStudents().size())
                + " / " + DatabaseService.getAllStudents().size() + " kết quả");
        info.setFont(UITheme.FONT_SMALL); info.setForeground(UITheme.TEXT_SECONDARY);
        p.add(info);
        for (int i = 1; i <= 3; i++) {
            JButton pg = new JButton(String.valueOf(i));
            pg.setFont(UITheme.FONT_SMALL);
            pg.setPreferredSize(new Dimension(30, 28));
            pg.setFocusPainted(false);
            if (i == 1) { pg.setBackground(UITheme.PRIMARY); pg.setForeground(Color.WHITE); }
            else { pg.setBackground(UITheme.WHITE); pg.setForeground(UITheme.TEXT_SECONDARY); }
            pg.setBorder(BorderFactory.createLineBorder(i==1 ? UITheme.PRIMARY : UITheme.BORDER, 1, true));
            p.add(pg);
        }
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────
    private void refreshTable(List<Student> list) {
        tableModel.setRowCount(0);
        for (Student s : list) tableModel.addRow(s.toRow());
    }

    private void fillFormFromRow(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        editingStudent = DatabaseService.getAllStudents().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
        if (editingStudent == null) return;
        tfId.setText(editingStudent.getId());
        tfName.setText(editingStudent.getFullName());
        tfBirth.setText(editingStudent.getBirthDate());
        tfIdCard.setText(editingStudent.getIdCard());
        tfPhone.setText(editingStudent.getPhone());
        tfEmail.setText(editingStudent.getEmail());
        tfFaculty.setText(editingStudent.getFaculty());
        tfClass.setText(editingStudent.getClassName());
        tfAddress.setText(editingStudent.getAddress());
        cbGender.setSelectedItem(editingStudent.getGender());
        cbRoom.setSelectedItem(editingStudent.getRoomId());
        cbStatus.setSelectedItem(editingStudent.getStatus());
        for (int i = 0; i < ((DefaultComboBoxModel<String>)cbUniversity.getModel()).getSize(); i++) {
            if (editingStudent.getUniversity().equals(cbUniversity.getItemAt(i))) { cbUniversity.setSelectedIndex(i); break; }
        }
    }

    private void addStudent() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"⚠ Vui lòng nhập họ và tên!","Lỗi",JOptionPane.WARNING_MESSAGE); return;
        }
        Student s = new Student(
            tfId.getText().trim(), tfName.getText().trim(), tfBirth.getText().trim(),
            (String)cbGender.getSelectedItem(), tfIdCard.getText().trim(),
            tfPhone.getText().trim(), tfEmail.getText().trim(),
            (String)cbUniversity.getSelectedItem(), tfFaculty.getText().trim(),
            tfClass.getText().trim(), tfAddress.getText().trim(),
            (String)cbRoom.getSelectedItem(), (String)cbStatus.getSelectedItem()
        );
        DatabaseService.addStudent(s);
        refreshTable(DatabaseService.getAllStudents());
        clearForm();
        JOptionPane.showMessageDialog(this,"✅ Thêm sinh viên thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
    }

    private void editStudent() {
        if (editingStudent == null) { JOptionPane.showMessageDialog(this,"⚠ Chọn sinh viên cần sửa từ bảng!","Lưu ý",JOptionPane.WARNING_MESSAGE); return; }
        editingStudent.setFullName(tfName.getText().trim());
        editingStudent.setPhone(tfPhone.getText().trim());
        editingStudent.setEmail(tfEmail.getText().trim());
        editingStudent.setFaculty(tfFaculty.getText().trim());
        editingStudent.setClassName(tfClass.getText().trim());
        editingStudent.setAddress(tfAddress.getText().trim());
        editingStudent.setRoomId((String)cbRoom.getSelectedItem());
        editingStudent.setStatus((String)cbStatus.getSelectedItem());
        editingStudent.setUniversity((String)cbUniversity.getSelectedItem());
        refreshTable(DatabaseService.getAllStudents());
        JOptionPane.showMessageDialog(this,"✅ Cập nhật thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        if (editingStudent == null) { JOptionPane.showMessageDialog(this,"⚠ Chọn sinh viên cần xóa!","Lưu ý",JOptionPane.WARNING_MESSAGE); return; }
        int r = JOptionPane.showConfirmDialog(this,"Xóa sinh viên "+editingStudent.getFullName()+"?","Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            	DatabaseService.deleteStudent(editingStudent.getId());
            editingStudent = null;
            refreshTable(DatabaseService.getAllStudents());
            clearForm();
        }
    }

    private void clearForm() {
        editingStudent = null;
        tfId.setText(DataStore.nextStudentId());
        tfName.setText(""); tfBirth.setText(""); tfIdCard.setText("");
        tfPhone.setText(""); tfEmail.setText(""); tfFaculty.setText("");
        tfClass.setText(""); tfAddress.setText("");
        cbGender.setSelectedIndex(0); cbRoom.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0); cbUniversity.setSelectedIndex(0);
    }

    // ── Renderers ─────────────────────────────────────────────────
    private TableCellRenderer statusRenderer() {
        return (t, v, sel, focus, row, col) -> {
            Color bg = switch(v.toString()) {
                case "Đang ở"      -> UITheme.SUCCESS_BG;
                case "Mới đăng ký" -> UITheme.INFO_BG;
                case "Chờ duyệt"   -> UITheme.WARNING_BG;
                default            -> UITheme.DANGER_BG;
            };
            Color fg = switch(v.toString()) {
                case "Đang ở"      -> UITheme.SUCCESS_TEXT;
                case "Mới đăng ký" -> UITheme.INFO_TEXT;
                case "Chờ duyệt"   -> UITheme.WARNING_TEXT;
                default            -> UITheme.DANGER_TEXT;
            };
            JLabel lbl = UITheme.badge(v.toString(), bg, fg);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        };
    }

    private TableCellRenderer primaryColorRenderer() {
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
}
