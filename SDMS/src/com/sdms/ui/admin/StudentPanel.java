package com.sdms.ui.admin;

import com.sdms.model.Room;
import com.sdms.model.Student;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class StudentPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;

    // Form fields
    private JTextField  tfId, tfName, tfBirth, tfIdCard, tfPhone, tfEmail, tfAddress, tfFaculty, tfClass;
    private JComboBox<String> cbGender, cbRoom, cbStatus;
    private JComboBox<String> cbFacultyFilter; // filter khoa ở khu vực bảng, tự cập nhật theo dữ liệu thật

    private Student editingStudent = null;

    private static final String[] COLS    = {"Mã SV","Họ tên","Phòng","Khoa","SĐT","Email","Trạng thái"};
    private static final String[] STATUS  = {"Đang ở","Mới đăng ký","Chờ duyệt","Đã rời"};

    /** Lấy danh sách mã phòng thật từ database (thay cho danh sách cứng sai trước đây) */
    private static String[] loadRoomIds() {
        List<String> ids = DatabaseService.getAllRooms().stream()
            .map(Room::getId)
            .collect(Collectors.toList());
        ids.add(0, ""); // cho phép để trống
        return ids.toArray(new String[0]);
    }

    /** Lấy danh sách các khoa thật đang có trong dữ liệu sinh viên (để hiển thị filter), kèm "Tất cả khoa" ở đầu */
    private static String[] loadFacultyOptions() {
        List<String> faculties = DatabaseService.getAllStudents().stream()
            .map(Student::getFaculty)
            .filter(f -> f != null && !f.trim().isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        faculties.add(0, "Tất cả khoa");
        return faculties.toArray(new String[0]);
    }

    public StudentPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        // Fixed layout - NO split pane, form width is locked at 480px
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG_LIGHT);

        JPanel formPanel = buildForm();
        formPanel.setPreferredSize(new Dimension(480, Integer.MAX_VALUE));
        formPanel.setMinimumSize(new Dimension(480, 0));
        formPanel.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));

        // Separator line
        JPanel sep = new JPanel();
        sep.setBackground(UITheme.BORDER);
        sep.setPreferredSize(new Dimension(1, 0));

        center.add(formPanel,       BorderLayout.WEST);
        center.add(sep,             BorderLayout.CENTER);
        center.add(buildTableArea(), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
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
        // Outer wrapper: fixed width, full height, white background
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
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Section title
        JLabel sec = new JLabel("THÊM / CHỈNH SỬA SINH VIÊN");
        sec.setFont(UITheme.FONT_LABEL); sec.setForeground(UITheme.PRIMARY);
        sec.setBorder(new EmptyBorder(0,0,10,0));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // Init all fields
        tfId      = UITheme.textField("");
        tfBirth   = UITheme.textField("");
        tfName    = UITheme.textField("");
        cbGender  = UITheme.comboBox(new String[]{"Nam","Nữ","Khác"});
        tfIdCard  = UITheme.textField("");
        tfPhone   = UITheme.textField("");
        tfEmail   = UITheme.textField("");
        tfFaculty = UITheme.textField("");
        tfClass   = UITheme.textField("");
        cbRoom    = UITheme.comboBox(loadRoomIds());
        cbStatus  = UITheme.comboBox(STATUS);
        tfAddress = UITheme.textField("");

        // Auto-fill new ID — lấy mã thật tiếp theo từ database
        tfId.setText(DatabaseService.nextStudentId());

        // ── Row 1: Mã SV  |  Ngày sinh ──────────────────────────
        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false); row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row1.add(makeFieldPanel("MÃ SINH VIÊN *", tfId));
        row1.add(makeFieldPanel("NGÀY SINH",      tfBirth));

        // ── Row 2: Họ và tên (full width) ───────────────────────
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false); row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row2.add(makeFieldPanel("HỌ VÀ TÊN *", tfName), BorderLayout.CENTER);

        // ── Row 3: Giới tính  |  CCCD ───────────────────────────
        JPanel row3 = new JPanel(new GridLayout(1, 2, 8, 0));
        row3.setOpaque(false); row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row3.add(makeFieldPanel("GIỚI TÍNH", cbGender));
        row3.add(makeFieldPanel("CCCD",      tfIdCard));

        // ── Row 4: Điện thoại  |  Email ─────────────────────────
        JPanel row4 = new JPanel(new GridLayout(1, 2, 8, 0));
        row4.setOpaque(false); row4.setAlignmentX(LEFT_ALIGNMENT);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row4.add(makeFieldPanel("ĐIỆN THOẠI", tfPhone));
        row4.add(makeFieldPanel("EMAIL",      tfEmail));

        // ── Row 5: Khoa  |  Lớp ─────────────────────────────────
        JPanel row5 = new JPanel(new GridLayout(1, 2, 8, 0));
        row5.setOpaque(false); row5.setAlignmentX(LEFT_ALIGNMENT);
        row5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row5.add(makeFieldPanel("KHOA", tfFaculty));
        row5.add(makeFieldPanel("LỚP",  tfClass));

        // ── Row 6: Phòng  |  Trạng thái ─────────────────────────
        JPanel row6 = new JPanel(new GridLayout(1, 2, 8, 0));
        row6.setOpaque(false); row6.setAlignmentX(LEFT_ALIGNMENT);
        row6.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row6.add(makeFieldPanel("PHÒNG",      cbRoom));
        row6.add(makeFieldPanel("TRẠNG THÁI", cbStatus));

        // ── Row 7: Địa chỉ (full width) ─────────────────────────
        JPanel row7 = new JPanel(new BorderLayout());
        row7.setOpaque(false); row7.setAlignmentX(LEFT_ALIGNMENT);
        row7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row7.add(makeFieldPanel("ĐỊA CHỈ", tfAddress), BorderLayout.CENTER);

        // Buttons row 1
        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow1.setOpaque(false); btnRow1.setAlignmentX(LEFT_ALIGNMENT);
        btnRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnAdd    = UITheme.primaryBtn("□ Thêm");
        JButton btnEdit   = UITheme.warningBtn("□ Sửa");
        JButton btnDelete = UITheme.dangerBtn("□ Xóa");
        JButton btnReset  = UITheme.outlineBtn("□ Làm mới");

        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow2.setOpaque(false); btnRow2.setAlignmentX(LEFT_ALIGNMENT);
        btnRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton btnExcel = UITheme.successBtn("□ Xuất Excel");

        btnRow1.add(btnAdd); btnRow1.add(btnEdit); btnRow1.add(btnDelete); btnRow1.add(btnReset);
        btnRow2.add(btnExcel);

        // Actions
        btnAdd.addActionListener(e -> addStudent());
        btnEdit.addActionListener(e -> editStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnReset.addActionListener(e -> clearForm());
        btnExcel.addActionListener(e -> exportToCsv());

        form.add(sec);
        form.add(Box.createVerticalStrut(4));
        form.add(row1);
        form.add(Box.createVerticalStrut(8));
        form.add(row2);
        form.add(Box.createVerticalStrut(8));
        form.add(row3);
        form.add(Box.createVerticalStrut(8));
        form.add(row4);
        form.add(Box.createVerticalStrut(8));
        form.add(row5);
        form.add(Box.createVerticalStrut(8));
        form.add(row6);
        form.add(Box.createVerticalStrut(8));
        form.add(row7);
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

    /** Label trên, field dưới trong một panel dọc */
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

    @Deprecated
    private void addFormField(JPanel grid, String label, JComponent field) {
        grid.add(makeFieldPanel(label, field));
    }

    @Deprecated
    private void addFormRow2(JPanel grid, String label, JComponent field) {
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

        tfSearch = UITheme.textField("");
        tfSearch.setPreferredSize(new Dimension(260, 36));
        JComboBox<String> cbStatusFilter = UITheme.comboBox(new String[]{"Tất cả trạng thái","Đang ở","Mới đăng ký","Chờ duyệt","Đã rời"});
        cbStatusFilter.setPreferredSize(new Dimension(160, 36));
        cbFacultyFilter = UITheme.comboBox(loadFacultyOptions());
        cbFacultyFilter.setPreferredSize(new Dimension(160, 36));

        toolbar.add(tfSearch);
        toolbar.add(cbStatusFilter);
        toolbar.add(cbFacultyFilter);

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

        cbFacultyFilter.addActionListener(e -> {
            String sel = (String)cbFacultyFilter.getSelectedItem();
            List<Student> filtered = DatabaseService.getAllStudents().stream()
                .filter(s -> "Tất cả khoa".equals(sel) || s.getFaculty().equals(sel))
                .collect(Collectors.toList());
            refreshTable(filtered);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.setBackground(UITheme.WHITE);
        scroll.getViewport().setBackground(UITheme.WHITE);

        // Pagination bar
        p.add(toolbar, BorderLayout.NORTH);
        p.add(scroll,  BorderLayout.CENTER);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────
    private void refreshTable(List<Student> list) {
        tableModel.setRowCount(0);
        for (Student s : list) tableModel.addRow(s.toRow());
    }

    /** Cập nhật lại danh sách khoa trong combobox filter dựa theo dữ liệu thật trong DB (gọi sau khi thêm/sửa/xóa sinh viên) */
    private void refreshFacultyFilter() {
        if (cbFacultyFilter == null) return;
        String current = (String) cbFacultyFilter.getSelectedItem();
        cbFacultyFilter.removeAllItems();
        for (String f : loadFacultyOptions()) cbFacultyFilter.addItem(f);
        // Giữ lại lựa chọn cũ nếu khoa đó vẫn còn tồn tại, ngược lại quay về "Tất cả khoa"
        if (current != null) {
            for (int i = 0; i < cbFacultyFilter.getItemCount(); i++) {
                if (cbFacultyFilter.getItemAt(i).equals(current)) {
                    cbFacultyFilter.setSelectedIndex(i);
                    return;
                }
            }
        }
        cbFacultyFilter.setSelectedIndex(0);
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
        tfAddress.setText(editingStudent.getAddress());
        cbGender.setSelectedItem(editingStudent.getGender());
        cbRoom.setSelectedItem(editingStudent.getRoomId());
        cbStatus.setSelectedItem(editingStudent.getStatus());
        tfFaculty.setText(editingStudent.getFaculty());
        tfClass.setText(editingStudent.getClassName());
    }

    private void addStudent() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"⚠ Vui lòng nhập họ và tên!","Lỗi",JOptionPane.WARNING_MESSAGE); return;
        }
        if (tfId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"⚠ Vui lòng nhập mã sinh viên!","Lỗi",JOptionPane.WARNING_MESSAGE); return;
        }
        if (tfFaculty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"⚠ Vui lòng nhập khoa!","Lỗi",JOptionPane.WARNING_MESSAGE); return;
        }
        String roomId = (String)cbRoom.getSelectedItem();

        // Kiểm tra phòng đã đầy chưa
        if (roomId != null && !roomId.isEmpty()) {
            Room selectedRoom = DatabaseService.getAllRooms().stream()
                .filter(r -> r.getId().equals(roomId)).findFirst().orElse(null);
            if (selectedRoom != null && selectedRoom.getOccupied() >= selectedRoom.getCapacity()) {
                JOptionPane.showMessageDialog(this,
                    "⚠ Phòng " + roomId + " đã đầy (" + selectedRoom.getOccupied() + "/" + selectedRoom.getCapacity() + " người)!\nVui lòng chọn phòng khác.",
                    "Phòng đã đầy", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Student s = new Student(
            tfId.getText().trim(), tfName.getText().trim(), tfBirth.getText().trim(),
            (String)cbGender.getSelectedItem(), tfIdCard.getText().trim(),
            tfPhone.getText().trim(), tfEmail.getText().trim(),
            "",                                       // university (không có ô nhập trong form)
            tfFaculty.getText().trim(),               // faculty (khoa đúng, nhập tự do)
            tfClass.getText().trim(), tfAddress.getText().trim(),
            roomId, (String)cbStatus.getSelectedItem()
        );
        boolean ok = DatabaseService.addStudent(s);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                "❌ Thêm sinh viên thất bại! Mã sinh viên có thể đã tồn tại hoặc lỗi kết nối database.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Cập nhật số người trong phòng mới
        if (roomId != null && !roomId.isEmpty()) {
            Room selectedRoom = DatabaseService.getAllRooms().stream()
                .filter(r -> r.getId().equals(roomId)).findFirst().orElse(null);
            if (selectedRoom != null)
                DatabaseService.updateRoomOccupied(roomId, selectedRoom.getOccupied() + 1);
        }
        refreshTable(DatabaseService.getAllStudents());
        refreshFacultyFilter();
        clearForm();
        JOptionPane.showMessageDialog(this,"✅ Thêm sinh viên thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
    }

    private void editStudent() {
        if (editingStudent == null) { JOptionPane.showMessageDialog(this,"⚠ Chọn sinh viên cần sửa từ bảng!","Lưu ý",JOptionPane.WARNING_MESSAGE); return; }
        if (tfFaculty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"⚠ Vui lòng nhập khoa!","Lỗi",JOptionPane.WARNING_MESSAGE); return;
        }

        String oldRoomId = editingStudent.getRoomId();
        String newRoomId = (String)cbRoom.getSelectedItem();

        // Kiểm tra phòng mới có đầy không (chỉ khi đổi sang phòng khác)
        if (newRoomId != null && !newRoomId.isEmpty() && !newRoomId.equals(oldRoomId)) {
            Room newRoom = DatabaseService.getAllRooms().stream()
                .filter(r -> r.getId().equals(newRoomId)).findFirst().orElse(null);
            if (newRoom != null && newRoom.getOccupied() >= newRoom.getCapacity()) {
                JOptionPane.showMessageDialog(this,
                    "⚠ Phòng " + newRoomId + " đã đầy (" + newRoom.getOccupied() + "/" + newRoom.getCapacity() + " người)!\nVui lòng chọn phòng khác.",
                    "Phòng đã đầy", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        editingStudent.setFullName(tfName.getText().trim());
        editingStudent.setPhone(tfPhone.getText().trim());
        editingStudent.setEmail(tfEmail.getText().trim());
        editingStudent.setFaculty(tfFaculty.getText().trim());
        editingStudent.setClassName(tfClass.getText().trim());
        editingStudent.setAddress(tfAddress.getText().trim());
        editingStudent.setRoomId(newRoomId);
        editingStudent.setStatus((String)cbStatus.getSelectedItem());
        boolean ok = DatabaseService.updateStudent(editingStudent);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                "❌ Cập nhật thất bại! Kiểm tra kết nối database.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cập nhật số người trong phòng khi đổi phòng
        if (newRoomId != null && !newRoomId.equals(oldRoomId)) {
            // Phòng cũ: giảm 1 người (nếu có)
            if (oldRoomId != null && !oldRoomId.isEmpty()) {
                Room oldRoom = DatabaseService.getAllRooms().stream()
                    .filter(r -> r.getId().equals(oldRoomId)).findFirst().orElse(null);
                if (oldRoom != null && oldRoom.getOccupied() > 0)
                    DatabaseService.updateRoomOccupied(oldRoomId, oldRoom.getOccupied() - 1);
            }
            // Phòng mới: tăng 1 người (nếu có)
            if (!newRoomId.isEmpty()) {
                Room newRoom = DatabaseService.getAllRooms().stream()
                    .filter(r -> r.getId().equals(newRoomId)).findFirst().orElse(null);
                if (newRoom != null)
                    DatabaseService.updateRoomOccupied(newRoomId, newRoom.getOccupied() + 1);
            }
        }

        refreshTable(DatabaseService.getAllStudents());
        refreshFacultyFilter();
        JOptionPane.showMessageDialog(this,"✅ Cập nhật thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        if (editingStudent == null) { JOptionPane.showMessageDialog(this,"⚠ Chọn sinh viên cần xóa!","Lưu ý",JOptionPane.WARNING_MESSAGE); return; }
        int r = JOptionPane.showConfirmDialog(this,"Xóa sinh viên "+editingStudent.getFullName()+"?","Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            String oldRoomId = editingStudent.getRoomId();
            boolean ok = DatabaseService.deleteStudent(editingStudent.getId());
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                    "❌ Xóa thất bại! Sinh viên có thể đang được tham chiếu bởi hợp đồng/hóa đơn khác, hoặc lỗi kết nối database.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Giảm occupied phòng cũ
            if (oldRoomId != null && !oldRoomId.isEmpty()) {
                Room oldRoom = DatabaseService.getAllRooms().stream()
                    .filter(rm -> rm.getId().equals(oldRoomId)).findFirst().orElse(null);
                if (oldRoom != null && oldRoom.getOccupied() > 0)
                    DatabaseService.updateRoomOccupied(oldRoomId, oldRoom.getOccupied() - 1);
            }
            editingStudent = null;
            refreshTable(DatabaseService.getAllStudents());
            refreshFacultyFilter();
            clearForm();
            JOptionPane.showMessageDialog(this,"✅ Đã xóa sinh viên thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        editingStudent = null;
        tfId.setText(DatabaseService.nextStudentId());
        tfName.setText(""); tfBirth.setText(""); tfIdCard.setText("");
        tfPhone.setText(""); tfEmail.setText(""); tfAddress.setText(""); tfFaculty.setText(""); tfClass.setText("");
        cbGender.setSelectedIndex(0); cbRoom.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
    }

    /** Xuất danh sách sinh viên đang hiển thị ra file CSV (mở được bằng Excel/PDF reader sau khi convert) */
    private void exportToCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("DanhSachSinhVien.csv"));
        fc.setDialogTitle("Lưu danh sách sinh viên");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new java.io.File(file.getPath() + ".csv");

        List<Student> data = DatabaseService.getAllStudents();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF'); // BOM để Excel nhận đúng UTF-8
            pw.println("Mã SV,Họ tên,Phòng,Khoa,Lớp,SĐT,Email,Trạng thái");
            for (Student s : data) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    s.getId(), s.getFullName(), s.getRoomId(), s.getFaculty(),
                    s.getClassName(), s.getPhone(), s.getEmail(), s.getStatus());
            }
            JOptionPane.showMessageDialog(this,
                "✅ Đã xuất " + data.size() + " sinh viên ra:\n" + file.getAbsolutePath(),
                "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Xuất thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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