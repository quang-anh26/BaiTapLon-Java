package com.sdms.ui.admin;


import java.security.MessageDigest;

import com.sdms.utils.DataStore;
import com.sdms.utils.DataStore.PendingAccount;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AccountManagementPanel — Quản lý tài khoản sinh viên.
 * Xem danh sách đơn đăng ký, duyệt hoặc từ chối tài khoản.
 */
public class AccountManagementPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JLabel            lblCount;
    private JLabel            lblPendingBadge;
    private String            filterStatus = "Tất cả";

    private static final String[] COLUMNS = {
        "Mã đơn", "Tên đăng nhập", "Họ và tên", "Giới tính",
        "Ngày sinh", "Điện thoại", "CCCD", "Thời gian đăng ký", "Trạng thái"
    };

    public AccountManagementPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("👤  Quản lý tài khoản sinh viên");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Xem danh sách đơn đăng ký và duyệt tài khoản cho sinh viên");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        long pending = DatabaseService.getAllPendingAccounts().stream()
            .filter(a -> a.getStatus() == PendingAccount.Status.PENDING).count();

        lblPendingBadge = new JLabel(pending > 0 ? "  " + pending + " chờ duyệt  " : "");
        lblPendingBadge.setFont(UITheme.FONT_SMALL);
        lblPendingBadge.setForeground(Color.WHITE);
        lblPendingBadge.setBackground(UITheme.WARNING);
        lblPendingBadge.setOpaque(true);
        lblPendingBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblPendingBadge.setVisible(pending > 0); // ẩn hẳn khi không có đơn chờ duyệt, tránh hiện ô màu trống

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.add(title);
        titleRow.add(lblPendingBadge);

        JPanel left = new JPanel(new BorderLayout(0, 3));
        left.setOpaque(false);
        left.add(titleRow, BorderLayout.NORTH);
        left.add(sub,      BorderLayout.SOUTH);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UITheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));
        body.add(buildStatRow(),   BorderLayout.NORTH);
        body.add(buildTableArea(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));

        List<PendingAccount> all = DatabaseService.getAllPendingAccounts();
        long total    = all.size();
        long pending  = all.stream().filter(a -> a.getStatus() == PendingAccount.Status.PENDING).count();
        long approved = all.stream().filter(a -> a.getStatus() == PendingAccount.Status.APPROVED).count();
        long rejected = all.stream().filter(a -> a.getStatus() == PendingAccount.Status.REJECTED).count();

        row.add(miniStat("Tổng đơn",   total,    UITheme.PRIMARY));
        row.add(miniStat("Chờ duyệt",  pending,  UITheme.WARNING));
        row.add(miniStat("Đã duyệt",   approved, UITheme.SUCCESS));
        row.add(miniStat("Từ chối",    rejected, UITheme.DANGER));
        return row;
    }

    private JPanel miniStat(String lbl, long val, Color color) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        JLabel num = new JLabel(String.valueOf(val), SwingConstants.CENTER);
        num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        num.setForeground(color);
        JLabel label = new JLabel(lbl, SwingConstants.CENTER);
        label.setFont(UITheme.FONT_SMALL);
        label.setForeground(UITheme.TEXT_SECONDARY);
        card.add(num,   BorderLayout.CENTER);
        card.add(label, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTableArea() {
        JPanel area = new JPanel(new BorderLayout(0, 8));
        area.setOpaque(false);
        area.add(buildToolbar(),    BorderLayout.NORTH);
        area.add(buildTablePanel(), BorderLayout.CENTER);
        return area;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        // Search
        tfSearch = UITheme.textField("Tìm theo tên, mã SV, CCCD...");
        tfSearch.setPreferredSize(new Dimension(280, 36));
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshTable(); }
        });

        // Filter buttons panel
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        String[] opts = {"Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối"};
        for (String opt : opts) {
            JButton btn = makeFilterBtn(opt, filters);
            filters.add(btn);
        }

        // Action buttons
        JButton btnApprove = UITheme.successBtn("✅  Duyệt");
        JButton btnReject  = UITheme.dangerBtn("✖  Từ chối");
        JButton btnDetail  = UITheme.outlineBtn("🔍  Chi tiết");
        btnApprove.addActionListener(e -> approveSelected());
        btnReject .addActionListener(e -> rejectSelected());
        btnDetail .addActionListener(e -> showDetail());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(btnDetail);
        actions.add(btnReject);
        actions.add(btnApprove);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(tfSearch);
        left.add(filters);

        bar.add(left,    BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JButton makeFilterBtn(String text, JPanel parent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = text.equals(filterStatus);
                g2.setColor(active ? UITheme.PRIMARY : UITheme.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                if (!active) {
                    g2.setColor(UITheme.BORDER);
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(text.equals(filterStatus) ? Color.WHITE : UITheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            filterStatus = text;
            refreshTable();
            // Cập nhật lại MÀU CHỮ cho tất cả filter btn theo trạng thái active mới, rồi mới repaint
            for (Component c : parent.getComponents()) {
                if (c instanceof JButton) {
                    JButton b = (JButton) c;
                    boolean isActive = b.getText().equals(filterStatus);
                    b.setForeground(isActive ? Color.WHITE : UITheme.TEXT_SECONDARY);
                }
                c.repaint();
            }
        });
        return btn;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel wrap = UITheme.card();
        wrap.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(UITheme.FONT_BODY);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setSelectionForeground(UITheme.PRIMARY_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));

        // Column widths
        int[] widths = {70, 110, 160, 80, 100, 110, 130, 145, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer cho cột Trạng thái
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                String v = val == null ? "" : val.toString();
                Color bg, fg;
                if ("Đã duyệt".equals(v))      { bg = UITheme.SUCCESS_BG; fg = UITheme.SUCCESS_TEXT; }
                else if ("Từ chối".equals(v))   { bg = new Color(0xFEE2E2); fg = UITheme.DANGER_TEXT; }
                else                            { bg = UITheme.WARNING_BG; fg = UITheme.WARNING_TEXT; }
                JLabel lbl = UITheme.badge(v, bg, fg);
                lbl.setOpaque(true);
                lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
                return lbl;
            }
        });

        // Double-click mở chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetail();
            }
        });

        refreshTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.WHITE);

        lblCount = new JLabel();
        lblCount.setFont(UITheme.FONT_TINY);
        lblCount.setForeground(UITheme.TEXT_MUTED);
        lblCount.setBorder(new EmptyBorder(6, 14, 6, 0));

        wrap.add(scroll,   BorderLayout.CENTER);
        wrap.add(lblCount, BorderLayout.SOUTH);
        return wrap;
    }

    // ── Logic ─────────────────────────────────────────────────────
    private void refreshTable() {
        String q = tfSearch == null ? "" : tfSearch.getText().trim().toLowerCase();

        List<PendingAccount> data = DatabaseService.getAllPendingAccounts().stream()
            .filter(a -> {
                switch (filterStatus) {
                    case "Chờ duyệt": return a.getStatus() == PendingAccount.Status.PENDING;
                    case "Đã duyệt":  return a.getStatus() == PendingAccount.Status.APPROVED;
                    case "Từ chối":   return a.getStatus() == PendingAccount.Status.REJECTED;
                    default:          return true;
                }
            })
            .filter(a -> q.isEmpty()
                || a.getFullName().toLowerCase().contains(q)
                || a.getUsername().toLowerCase().contains(q)
                || a.getCccd().contains(q)
                || a.getPhone().contains(q))
            .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (PendingAccount a : data)
            tableModel.addRow(a.toRow());

        if (lblCount != null)
            lblCount.setText("  Hiển thị " + data.size()
                + " / " + DatabaseService.getAllPendingAccounts().size() + " đơn đăng ký");

        // Cập nhật badge chờ duyệt
        if (lblPendingBadge != null) {
            long pending = DatabaseService.getAllPendingAccounts().stream()
                .filter(a -> a.getStatus() == PendingAccount.Status.PENDING).count();
            lblPendingBadge.setText(pending > 0 ? "  " + pending + " chờ duyệt  " : "");
            lblPendingBadge.setVisible(pending > 0);
        }
    }

    private PendingAccount getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String id = (String) tableModel.getValueAt(row, 0);
        return DatabaseService.getAllPendingAccounts().stream()
            .filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }
private void approveSelected() {
    PendingAccount a = getSelected();

    if (a == null) {
        showToast("Vui lòng chọn một đơn đăng ký!", false);
        return;
    }

    if (a.getStatus() != PendingAccount.Status.PENDING) {
        showToast("Đơn này đã được xử lý rồi!", false);
        return;
    }

    // Dialog nhập thông tin còn thiếu
    JTextField tfEmail    = new JTextField(20);
    JTextField tfFaculty  = new JTextField(20);
    JTextField tfClass    = new JTextField(10);
    JTextField tfAddress  = new JTextField(20);

    // Phòng: chọn từ danh sách thật trong DB (kèm số chỗ còn trống) để tránh nhập sai mã phòng
    List<com.sdms.model.Room> allRooms = DatabaseService.getAllRooms();
    List<String> roomOptions = new ArrayList<>();
    roomOptions.add(""); // cho phép để trống — chưa gán phòng
    for (com.sdms.model.Room rm : allRooms) {
        roomOptions.add(rm.getId() + "  (" + rm.getOccupied() + "/" + rm.getCapacity() + ")");
    }
    JComboBox<String> cbRoom = new JComboBox<>(roomOptions.toArray(new String[0]));

    JPanel dlg = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
    dlg.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,8,8,8));
    dlg.add(new JLabel("Họ tên:")); dlg.add(new JLabel(a.getFullName()));
    dlg.add(new JLabel("Ngày sinh:")); dlg.add(new JLabel(a.getDob()));
    dlg.add(new JLabel("Giới tính:")); dlg.add(new JLabel(a.getGender()));
    dlg.add(new JLabel("CCCD:")); dlg.add(new JLabel(a.getCccd()));
    dlg.add(new JLabel("SĐT:")); dlg.add(new JLabel(a.getPhone()));
    dlg.add(new JLabel("Email *:")); dlg.add(tfEmail);
    dlg.add(new JLabel("Khoa *:")); dlg.add(tfFaculty);
    dlg.add(new JLabel("Lớp:")); dlg.add(tfClass);
    dlg.add(new JLabel("Địa chỉ:")); dlg.add(tfAddress);
    dlg.add(new JLabel("Phòng:")); dlg.add(cbRoom);

    // Validate + lấy mã phòng đã chọn (vòng lặp cho tới khi hợp lệ hoặc người dùng bấm Cancel)
    String chosenRoomId = askApprovalDetails(dlg, tfEmail, tfFaculty, cbRoom, allRooms);
    if (chosenRoomId == null) return; // người dùng bấm Cancel hoặc đóng dialog

    // Tạo Student từ thông tin PendingAccount + form
    String studentId = DatabaseService.nextStudentId();
    String faculty   = tfFaculty.getText().trim();
    String email     = tfEmail.getText().trim();
    String clazz     = tfClass.getText().trim();
    String address   = tfAddress.getText().trim();

    com.sdms.model.Student student = new com.sdms.model.Student(
        studentId, a.getFullName(), a.getDob(),
        a.getGender(), a.getCccd(),
        a.getPhone(), email,
        "", faculty,
        clazz, address,
        chosenRoomId, "Đang ở"
    );

    boolean studentAdded = DatabaseService.addStudent(student);
    if (!studentAdded) {
        showToast("Tạo hồ sơ sinh viên thất bại! Kiểm tra kết nối database.", false);
        return;
    }

    // Cập nhật số người trong phòng được gán (nếu có)
    com.sdms.model.Room assignedRoom = null;
    if (!chosenRoomId.isEmpty()) {
        assignedRoom = allRooms.stream()
            .filter(r -> r.getId().equals(chosenRoomId)).findFirst().orElse(null);
        if (assignedRoom != null)
            DatabaseService.updateRoomOccupied(chosenRoomId, assignedRoom.getOccupied() + 1);
    }

    // Tự động tạo Hợp đồng nếu sinh viên đã được gán phòng
    String contractInfo = "";
    if (assignedRoom != null) {
        long monthlyFee = roomMonthlyFee(assignedRoom.getCapacity());
        java.time.LocalDate startDate = java.time.LocalDate.now();
        java.time.LocalDate endDate   = startDate.plusYears(1);

        com.sdms.model.Contract contract = new com.sdms.model.Contract(
            DatabaseService.nextContractId(),
            studentId, a.getFullName(), chosenRoomId,
            startDate, endDate, monthlyFee,
            "Tự động tạo khi duyệt tài khoản",
            com.sdms.model.Contract.Status.ACTIVE
        );
        if (DatabaseService.addContract(contract)) {
            contractInfo = "\n📄 Đã tạo hợp đồng " + contract.getId() + " (hiệu lực đến " + contract.getEndDateStr() + ").";
        } else {
            contractInfo = "\n⚠ Tạo hợp đồng tự động thất bại, vui lòng tạo thủ công ở Quản lý hợp đồng.";
        }
    }

    // Cập nhật trạng thái đơn
    a.setStatus(PendingAccount.Status.APPROVED);
    DatabaseService.updatePendingAccountStatus(a.getId(), "Đã duyệt", "");

    // Tạo tài khoản đăng nhập
    DatabaseService.addUser(a.getUsername(), a.getPassword(), "STUDENT", a.getFullName(), studentId);

    refreshTable();
    showToast("✅ Đã duyệt! Sinh viên " + a.getFullName() + " (mã " + studentId + ") đã được thêm vào hệ thống." + contractInfo, true);
}

/**
 * Lấy tiền phòng/tháng theo sức chứa phòng, dựa trên đơn giá đã cấu hình ở Cài đặt hệ thống
 * (room_fee_4 / room_fee_6). Sức chứa khác sẽ rơi về mặc định room_fee_4 nếu không khớp.
 */
private long roomMonthlyFee(int capacity) {
    String key = (capacity >= 6) ? "room_fee_6" : "room_fee_4";
    String raw = DatabaseService.getSetting(key);
    try {
        return raw == null || raw.trim().isEmpty() ? 0L : Long.parseLong(raw.trim());
    } catch (NumberFormatException e) {
        return 0L;
    }
}

/**
 * Hiện dialog xác nhận, validate Email/Khoa bắt buộc và kiểm tra phòng đã đầy chưa.
 * Lặp lại dialog cho tới khi dữ liệu hợp lệ hoặc người dùng hủy.
 * @return mã phòng đã chọn (chuỗi rỗng nếu không gán phòng), hoặc null nếu người dùng hủy.
 */
private String askApprovalDetails(JPanel dlg, JTextField tfEmail, JTextField tfFaculty,
                                   JComboBox<String> cbRoom, List<com.sdms.model.Room> allRooms) {
    while (true) {
        int confirm = JOptionPane.showConfirmDialog(this, dlg,
            "Duyệt tài khoản — Nhập thông tin còn thiếu",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (confirm != JOptionPane.OK_OPTION) return null;

        if (tfEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Vui lòng nhập Email!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            continue;
        }
        if (tfFaculty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Vui lòng nhập Khoa!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            continue;
        }

        // Tách mã phòng khỏi chuỗi hiển thị dạng "P101  (3/4)"
        String roomSelection = (String) cbRoom.getSelectedItem();
        String parsedRoomId = (roomSelection == null || roomSelection.trim().isEmpty())
            ? "" : roomSelection.trim().split("\\s+")[0];

        // Kiểm tra phòng đã đầy chưa trước khi gán
        if (!parsedRoomId.isEmpty()) {
            com.sdms.model.Room selectedRoom = allRooms.stream()
                .filter(r -> r.getId().equals(parsedRoomId)).findFirst().orElse(null);
            if (selectedRoom != null && selectedRoom.getOccupied() >= selectedRoom.getCapacity()) {
                JOptionPane.showMessageDialog(this,
                    "⚠ Phòng " + parsedRoomId + " đã đầy (" + selectedRoom.getOccupied() + "/" + selectedRoom.getCapacity() + " người)!\nVui lòng chọn phòng khác.",
                    "Phòng đã đầy", JOptionPane.WARNING_MESSAGE);
                continue;
            }
        }
        return parsedRoomId;
    }
}

    private void rejectSelected() {
        PendingAccount a = getSelected();
        if (a == null) { showToast("Vui lòng chọn một đơn đăng ký!", false); return; }
        if (a.getStatus() != PendingAccount.Status.PENDING) {
            showToast("Đơn này đã được xử lý rồi!", false); return;
        }
        String reason = (String) JOptionPane.showInputDialog(this,
            "<html>Lý do từ chối tài khoản <b>" + a.getFullName() + "</b>:</html>",
            "Từ chối đăng ký", JOptionPane.WARNING_MESSAGE,
            null, null, "");
        if (reason == null) return;   // bấm Cancel
        a.setStatus(PendingAccount.Status.REJECTED);
        a.setNote(reason.trim());
        DatabaseService.updatePendingAccountStatus(a.getId(), "Từ chối", reason.trim());
        refreshTable();
        showToast("Đã từ chối tài khoản: " + a.getFullName(), false);
    }

    private void showDetail() {
        PendingAccount a = getSelected();
        if (a == null) { showToast("Vui lòng chọn một đơn để xem chi tiết!", false); return; }

        Color accentColor;
        switch (a.getStatus()) {
            case APPROVED: accentColor = UITheme.SUCCESS; break;
            case REJECTED: accentColor = UITheme.DANGER;  break;
            default:       accentColor = UITheme.WARNING;
        }

        // Tạo danh sách dòng thông tin
        List<String[]> infoList = new ArrayList<>();
        infoList.add(new String[]{"Mã đơn",             a.getId()});
        infoList.add(new String[]{"Tên đăng nhập",      a.getUsername()});
        infoList.add(new String[]{"Họ và tên",          a.getFullName()});
        infoList.add(new String[]{"Giới tính",          a.getGender()});
        infoList.add(new String[]{"Ngày sinh",          a.getDob()});
        infoList.add(new String[]{"Điện thoại",         a.getPhone()});
        infoList.add(new String[]{"CCCD",               a.getCccd()});
        infoList.add(new String[]{"Thời gian đăng ký",  a.getRegisteredAt()});
        infoList.add(new String[]{"Trạng thái",         a.getStatusText()});
        if (a.getNote() != null && !a.getNote().isEmpty())
            infoList.add(new String[]{"Ghi chú",        a.getNote()});

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(6, 10, 6, 10));
        panel.setBackground(UITheme.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 6, 4, 12);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.NONE;

        for (int i = 0; i < infoList.size(); i++) {
            String key = infoList.get(i)[0];
            String val = infoList.get(i)[1];

            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1;
            JLabel kLbl = new JLabel(key + ":");
            kLbl.setFont(UITheme.FONT_BOLD);
            kLbl.setForeground(UITheme.TEXT_SECONDARY);
            kLbl.setPreferredSize(new Dimension(150, 22));
            panel.add(kLbl, gbc);

            gbc.gridx = 1;
            JLabel vLbl = new JLabel(val);
            vLbl.setFont(UITheme.FONT_BODY);
            vLbl.setForeground("Trạng thái".equals(key) ? accentColor : UITheme.TEXT_PRIMARY);
            panel.add(vLbl, gbc);
        }

        JOptionPane.showMessageDialog(this, panel,
            "Chi tiết đơn — " + a.getFullName(),
            JOptionPane.PLAIN_MESSAGE);
    }

    // ── Toast ─────────────────────────────────────────────────────
    private void showToast(String msg, boolean success) {
        JDialog t = new JDialog();
        t.setUndecorated(true);
        t.setAlwaysOnTop(true);
        t.setBackground(new Color(0, 0, 0, 0));

        Color accent = success ? UITheme.SUCCESS : UITheme.DANGER;

        JPanel c = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(12, 16, 12, 16));
        c.setBackground(Color.WHITE);

        JLabel ico = new JLabel(success ? "✅" : "⚠️");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JLabel txt = new JLabel("<html>" + msg + "</html>");
        txt.setFont(UITheme.FONT_BODY);
        txt.setForeground(UITheme.TEXT_PRIMARY);
        c.add(ico, BorderLayout.WEST);
        c.add(txt, BorderLayout.CENTER);

        t.setContentPane(c);
        t.pack();
        t.setSize(Math.max(t.getWidth(), 360), t.getHeight() + 4);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        t.setLocation(sc.width - t.getWidth() - 24, sc.height - t.getHeight() - 52);
        t.setVisible(true);
        new Timer(3500, e -> t.dispose()) {{ setRepeats(false); start(); }};

        
    }
    private String sha256(String text) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(text.getBytes("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }  catch (Exception e) {
        throw new RuntimeException(e);
        }
  }
}