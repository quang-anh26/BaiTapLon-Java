package com.sdms.ui.user;

import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Panel xem và chỉnh sửa thông tin cá nhân của sinh viên.
 * Gồm: Avatar + thông tin cơ bản | Form chỉnh sửa bên phải.
 */
public class StudentProfilePanel extends JPanel {

    private final User    currentUser;
    private final Student student;

    // ── Form fields (có thể sửa) ──────────────────────────────────
    private JTextField tfPhone, tfEmail, tfAddress;

    // ── Fields chỉ đọc ───────────────────────────────────────────
    private JLabel lblId, lblName, lblBirth, lblGender,
                   lblIdCard, lblUniversity, lblFaculty,
                   lblClass, lblRoom, lblStatus;

    // ── Chế độ chỉnh sửa ─────────────────────────────────────────
    private boolean editMode = false;
    private JButton btnEdit;

    public StudentProfilePanel(User currentUser) {
        this.currentUser = currentUser;
        this.student     = findStudent();

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

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("👤  Thông tin cá nhân");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Thông tin cá nhân");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        btnEdit = UITheme.primaryBtn("✏  Chỉnh sửa");
        btnEdit.addActionListener(e -> toggleEdit());

        p.add(left,    BorderLayout.WEST);
        p.add(btnEdit, BorderLayout.EAST);
        return p;
    }

    // ── Nội dung chính ────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setBackground(UITheme.BG_LIGHT);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        content.add(buildAvatarCard(),  BorderLayout.WEST);
        content.add(buildDetailCard(),  BorderLayout.CENTER);
        return content;
    }

    // ── Card avatar bên trái ──────────────────────────────────────
    private JPanel buildAvatarCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(24, 20, 24, 20)
        ));
        card.setPreferredSize(new Dimension(220, 0));

        // Avatar circle vẽ tay
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY,
                    getWidth(), getHeight(), UITheme.PURPLE
                );
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();
                String init = initials(student != null ? student.getFullName()
                    : currentUser.getFullName());
                g2.drawString(init,
                    (getWidth()  - fm.stringWidth(init)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(90, 90); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        avatar.setOpaque(false);
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        // Tên sinh viên
        String name = student != null ? student.getFullName() : currentUser.getFullName();
        JLabel lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        lblName.setBorder(new EmptyBorder(10, 0, 2, 0));

        // Mã SV
        String sid = student != null ? student.getId() : "—";
        JLabel lblId = new JLabel(sid, SwingConstants.CENTER);
        lblId.setFont(UITheme.FONT_SMALL);
        lblId.setForeground(UITheme.PRIMARY);
        lblId.setAlignmentX(CENTER_ALIGNMENT);

        // Badge trạng thái
        String status = student != null ? student.getStatus() : "Đang lưu trú";
        JLabel lblStatus = UITheme.badge("● " + status, UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        lblStatus.setBorder(new EmptyBorder(6, 0, 0, 0));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Thông tin nhanh bên dưới
        JPanel quickInfo = new JPanel(new GridLayout(0, 1, 0, 8));
        quickInfo.setOpaque(false);
        quickInfo.setBorder(new EmptyBorder(10, 0, 0, 0));

        String roomId = student != null && !student.getRoomId().isEmpty()
            ? student.getRoomId() : "Chưa có phòng";
        addQuickInfo(quickInfo, "🛏", "Phòng",    roomId);
        addQuickInfo(quickInfo, "🎓", "Khoa",
            student != null ? student.getFaculty() : "—");
        addQuickInfo(quickInfo, "📚", "Lớp",
            student != null ? student.getClassName() : "—");
        addQuickInfo(quickInfo, "📞", "SĐT",
            student != null ? student.getPhone() : "—");

        card.add(avatar);
        card.add(lblName);
        card.add(lblId);
        card.add(lblStatus);
        card.add(Box.createVerticalStrut(12));
        card.add(sep);
        card.add(quickInfo);
        return card;
    }

    /** Thêm một dòng thông tin nhanh */
    private void addQuickInfo(JPanel p, String icon, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);

        JLabel lblIcon = new JLabel(icon + " " + label + ":");
        lblIcon.setFont(UITheme.FONT_TINY);
        lblIcon.setForeground(UITheme.TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(UITheme.FONT_SMALL);
        lblVal.setForeground(UITheme.TEXT_PRIMARY);
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblVal,  BorderLayout.EAST);
        p.add(row);
    }

    // ── Card chi tiết bên phải ────────────────────────────────────
    private JPanel buildDetailCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(20, 22, 20, 22)
        ));

        // ── Section 1: Thông tin cơ bản (chỉ đọc) ────────────────
        card.add(sectionHeader("📋  Thông tin cơ bản"));
        card.add(Box.createVerticalStrut(10));

        JPanel basicGrid = new JPanel(new GridLayout(0, 2, 14, 10));
        basicGrid.setOpaque(false);
        basicGrid.setAlignmentX(LEFT_ALIGNMENT);
        basicGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        lblId         = readonlyLabel(student != null ? student.getId()         : "—");
        lblName       = readonlyLabel(student != null ? student.getFullName()   : "—");
        lblBirth      = readonlyLabel(student != null ? student.getBirthDate()  : "—");
        lblGender     = readonlyLabel(student != null ? student.getGender()     : "—");
        lblIdCard     = readonlyLabel(student != null ? student.getIdCard()     : "—");
        lblUniversity = readonlyLabel(student != null ? student.getUniversity() : "—");
        lblFaculty    = readonlyLabel(student != null ? student.getFaculty()    : "—");
        lblClass      = readonlyLabel(student != null ? student.getClassName()  : "—");
        lblRoom       = readonlyLabel(student != null ? student.getRoomId()     : "—");
        lblStatus     = readonlyLabel(student != null ? student.getStatus()     : "—");

        addReadonlyField(basicGrid, "Mã sinh viên",  lblId);
        addReadonlyField(basicGrid, "Họ và tên",     lblName);
        addReadonlyField(basicGrid, "Ngày sinh",     lblBirth);
        addReadonlyField(basicGrid, "Giới tính",     lblGender);
        addReadonlyField(basicGrid, "CCCD/CMND",     lblIdCard);
        addReadonlyField(basicGrid, "Phòng đang ở",  lblRoom);
        addReadonlyField(basicGrid, "Trường/CSGD",   lblUniversity);
        addReadonlyField(basicGrid, "Khoa/Viện",     lblFaculty);
        addReadonlyField(basicGrid, "Lớp",           lblClass);
        addReadonlyField(basicGrid, "Trạng thái",    lblStatus);

        card.add(basicGrid);
        card.add(Box.createVerticalStrut(16));

        // ── Section 2: Thông tin liên hệ (có thể sửa) ────────────
        card.add(sectionHeader("📞  Thông tin liên hệ"));
        card.add(Box.createVerticalStrut(10));

        tfPhone   = UITheme.textField(student != null ? student.getPhone()   : "");
        tfEmail   = UITheme.textField(student != null ? student.getEmail()   : "");
        tfAddress = UITheme.textField(student != null ? student.getAddress() : "");

        // Khởi đầu ở chế độ chỉ đọc
        setFieldsEditable(false);

        JPanel contactGrid = new JPanel(new GridLayout(0, 2, 14, 10));
        contactGrid.setOpaque(false);
        contactGrid.setAlignmentX(LEFT_ALIGNMENT);
        contactGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        addEditableField(contactGrid, "Số điện thoại", tfPhone);
        addEditableField(contactGrid, "Email",         tfEmail);
        addEditableField(contactGrid, "Địa chỉ thường trú", tfAddress);

        card.add(contactGrid);
        card.add(Box.createVerticalStrut(14));

        // Nút lưu (ẩn khi chưa edit)
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnSave   = UITheme.successBtn("💾 Lưu thay đổi");
        JButton btnCancel = UITheme.outlineBtn("✕ Hủy");
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
        btnRow.add(btnSave);
        btnRow.add(btnCancel);

        btnSave.addActionListener(e -> {
            saveChanges();
            btnSave.setVisible(false);
            btnCancel.setVisible(false);
        });
        btnCancel.addActionListener(e -> {
            cancelEdit();
            btnSave.setVisible(false);
            btnCancel.setVisible(false);
        });

        // Lưu reference để toggle visibility
        this.saveBtnRef    = btnSave;
        this.cancelBtnRef  = btnCancel;

        card.add(btnRow);
        return card;
    }

    // ── Lưu reference nút để toggle ──────────────────────────────
    private JButton saveBtnRef, cancelBtnRef;

    /** Bật/tắt chế độ chỉnh sửa */
    private void toggleEdit() {
        editMode = !editMode;
        setFieldsEditable(editMode);
        btnEdit.setText(editMode ? "↺ Đang chỉnh sửa..." : "✏  Chỉnh sửa");
        btnEdit.setBackground(editMode ? UITheme.WARNING_TEXT : UITheme.PRIMARY);
        if (saveBtnRef   != null) saveBtnRef.setVisible(editMode);
        if (cancelBtnRef != null) cancelBtnRef.setVisible(editMode);
        revalidate(); repaint();
    }

    /** Bật/tắt editable cho các field liên hệ */
    private void setFieldsEditable(boolean editable) {
        Color bg = editable ? UITheme.WHITE : UITheme.BG_SECONDARY;
        for (JTextField tf : new JTextField[]{tfPhone, tfEmail, tfAddress}) {
            if (tf != null) {
                tf.setEditable(editable);
                tf.setBackground(bg);
            }
        }
    }

    /** Lưu thay đổi vào DataStore */
    private void saveChanges() {
        if (student == null) return;
        student.setPhone(tfPhone.getText().trim());
        student.setEmail(tfEmail.getText().trim());
        student.setAddress(tfAddress.getText().trim());
        editMode = false;
        setFieldsEditable(false);
        btnEdit.setText("✏  Chỉnh sửa");
        btnEdit.setBackground(UITheme.PRIMARY);
        JOptionPane.showMessageDialog(this,
            "✅ Đã lưu thông tin liên hệ thành công!", "Thành công",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /** Hủy chỉnh sửa, khôi phục giá trị cũ */
    private void cancelEdit() {
        if (student != null) {
            tfPhone.setText(student.getPhone());
            tfEmail.setText(student.getEmail());
            tfAddress.setText(student.getAddress());
        }
        editMode = false;
        setFieldsEditable(false);
        btnEdit.setText("✏  Chỉnh sửa");
        btnEdit.setBackground(UITheme.PRIMARY);
    }

    // ── Builder helpers ───────────────────────────────────────────

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.PRIMARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(0, 0, 6, 0)
        ));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JLabel readonlyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setBackground(UITheme.BG_SECONDARY);
        l.setOpaque(true);
        l.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return l;
    }

    private void addReadonlyField(JPanel grid, String label, JComponent field) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.add(UITheme.formLabel(label), BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        grid.add(cell);
    }

    private void addEditableField(JPanel grid, String label, JTextField field) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.add(UITheme.formLabel(label), BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        grid.add(cell);
    }

    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length-1].substring(0, 1)).toUpperCase();
    }
}
