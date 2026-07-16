package com.sdms.ui.admin;

import com.sdms.model.Room;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.stream.Collectors;

public class RoomPanel extends JPanel {

    private JPanel cardGrid;
    private String filterStatus = "Tất cả";

    public RoomPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,UITheme.BORDER), new EmptyBorder(12,20,12,20)));

        JPanel left = new JPanel(new BorderLayout(0,2));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("🚪  Quản lý phòng");
        titleLabel.setFont(UITheme.FONT_H2); titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Sơ đồ trực quan vị trí phòng theo tầng");
        sub.setFont(UITheme.FONT_TINY); sub.setForeground(UITheme.TEXT_MUTED);
        left.add(titleLabel, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnRefresh = UITheme.outlineBtn("🔄 Làm mới");
        btnRefresh.addActionListener(e -> applyFilter());
        JButton btnAdd = UITheme.primaryBtn("➕  Thêm phòng");
        btnAdd.addActionListener(e -> showAddRoomDialog());
        right.add(btnRefresh);
        right.add(btnAdd);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0,12));
        body.setBackground(UITheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        body.add(buildStatRow(),   BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0,8));
        middle.setOpaque(false);
        middle.add(buildLegendAndFilters(), BorderLayout.NORTH);

        cardGrid = new JPanel();
        cardGrid.setOpaque(false);
        cardGrid.setLayout(new BoxLayout(cardGrid, BoxLayout.Y_AXIS));
        applyFilter();   // dùng applyFilter() thay vì getAllRooms() thẳng để nhất quán

        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        middle.add(scroll, BorderLayout.CENTER);
        body.add(middle, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        long total   = DatabaseService.getAllRooms().size();
        long empty   = DatabaseService.getAllRooms().stream().filter(r -> r.getStatus()==Room.Status.AVAILABLE).count();
        long near    = DatabaseService.getAllRooms().stream().filter(r -> r.getStatus()==Room.Status.NEARLY_FULL).count();
        long full    = DatabaseService.getAllRooms().stream().filter(r -> r.getStatus()==Room.Status.FULL).count();

        row.add(miniStat("Tổng số phòng", total, UITheme.PRIMARY));
        row.add(miniStat("Trống phòng",       empty,  UITheme.SUCCESS));
        row.add(miniStat("Gần đầy",       near,   UITheme.WARNING));
        row.add(miniStat("Đã đầy",        full,   UITheme.DANGER));
        return row;
    }

    private JPanel miniStat(String lbl, long val, Color color) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        JLabel num = new JLabel(String.valueOf(val), SwingConstants.CENTER);
        num.setFont(new Font("Segoe UI", Font.BOLD, 28));
        num.setForeground(color);
        JLabel label = new JLabel(lbl, SwingConstants.CENTER);
        label.setFont(UITheme.FONT_SMALL); label.setForeground(UITheme.TEXT_SECONDARY);
        card.add(num, BorderLayout.CENTER);
        card.add(label, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLegendAndFilters() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setOpaque(false);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Trống phòng",     UITheme.SUCCESS));
        legend.add(legendItem("Gần đầy ≥50%",UITheme.WARNING));
        legend.add(legendItem("Đã đầy",      UITheme.DANGER));
        legend.add(legendItem("Bảo trì",     UITheme.TEXT_MUTED));

        // Filters — dùng ButtonGroup để chỉ 1 nút active tại một thời điểm
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        ButtonGroup filterGroup = new ButtonGroup();
        String[] filterOpts = {"Tất cả","Trống phòng","Gần đầy","Đã đầy","Phòng 4 người","Phòng 6 người"};
        for (String opt : filterOpts) {
            JToggleButton btn = filterBtn(opt);
            if (opt.equals(filterStatus)) {
                btn.setSelected(true);
                btn.setForeground(Color.WHITE);
            }
            filterGroup.add(btn);
            btn.addActionListener(e -> {
                filterStatus = opt;
                // Cập nhật màu chữ cho tất cả nút sau khi chọn
                for (Component c : filters.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton tb = (JToggleButton) c;
                        tb.setForeground(tb.isSelected() ? Color.WHITE : UITheme.TEXT_SECONDARY);
                        tb.repaint();
                    }
                }
                applyFilter();
            });
            filters.add(btn);
        }

        p.add(legend,  BorderLayout.NORTH);
        p.add(filters, BorderLayout.CENTER);
        return p;
    }

    private JPanel legendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); g2.fillOval(0,2,10,10); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(10,14); }
        };
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        p.add(dot); p.add(lbl);
        return p;
    }

    private JToggleButton filterBtn(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(UITheme.PRIMARY); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                } else {
                    g2.setColor(UITheme.WHITE); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                    g2.setColor(UITheme.BORDER); g2.setStroke(new BasicStroke(0.8f));
                    g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,20,20));
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(text.equals(filterStatus) ? Color.WHITE : UITheme.TEXT_SECONDARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void applyFilter() {
        List<Room> filtered = DatabaseService.getAllRooms().stream()
            .filter(r -> switch(filterStatus) {
                case "Trống phòng"       -> r.getStatus() == Room.Status.AVAILABLE;
                case "Gần đầy"       -> r.getStatus() == Room.Status.NEARLY_FULL;
                case "Đã đầy"        -> r.getStatus() == Room.Status.FULL;
                case "Phòng 4 người" -> r.getCapacity() == 4;
                case "Phòng 6 người" -> r.getCapacity() == 6;
                default              -> true;
            })
            .collect(Collectors.toList());
        rebuildCards(filtered);
    }

    private void rebuildCards(List<Room> rooms) {
        cardGrid.removeAll();
        // Group by floor
        for (int floor = 1; floor <= 4; floor++) {
            final int f = floor;
            List<Room> floorRooms = rooms.stream().filter(r -> r.getFloor() == f).collect(Collectors.toList());
            if (floorRooms.isEmpty()) continue;

            JPanel section = new JPanel(new BorderLayout(0, 6));
            section.setOpaque(false);
            section.setAlignmentX(LEFT_ALIGNMENT);
            section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

            JLabel floorLabel = new JLabel("🏢  Tầng " + f);
            floorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            floorLabel.setForeground(UITheme.TEXT_SECONDARY);
            floorLabel.setBorder(new EmptyBorder(8, 0, 4, 0));

            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            grid.setOpaque(false);
            for (Room room : floorRooms) {
                grid.add(buildRoomCard(room));
            }

            section.add(floorLabel, BorderLayout.NORTH);
            section.add(grid,       BorderLayout.CENTER);
            cardGrid.add(section);
        }
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JPanel buildRoomCard(Room room) {
        Color[] colors = roomColors(room.getStatus());
        Color cardBg = colors[0], borderC = colors[1], textC = colors[2], dotC = colors[3];
        // Hover tông sáng hơn của borderC
        Color hoverBg = borderC.brighter().brighter();

        boolean[] hovered = {false};

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền: đậm hơn khi hover
                g2.setColor(hovered[0] ? borderC.brighter() : cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Viền: dày hơn khi hover
                g2.setColor(borderC);
                g2.setStroke(new BasicStroke(hovered[0] ? 2.5f : 1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                // Chấm trạng thái
                g2.setColor(dotC);
                g2.fillOval(getWidth()-14, 6, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 10, 10, 18));
        card.setPreferredSize(new Dimension(90, 72));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblId = new JLabel(room.getId());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblId.setForeground(textC);
        lblId.setAlignmentX(LEFT_ALIGNMENT);

        String capText = room.getStatus() == Room.Status.MAINTENANCE
            ? "Bảo trì" : room.getOccupied()+"/"+room.getCapacity()+" người";
        JLabel lblCap = new JLabel(capText);
        lblCap.setFont(UITheme.FONT_TINY);
        lblCap.setForeground(textC);
        lblCap.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblId);
        card.add(Box.createVerticalStrut(3));
        card.add(lblCap);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                showRoomDetail(room, borderC, textC);
            }
        });
        return card;
    }

    private void showRoomDetail(Room room, Color accentColor, Color textColor) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết phòng", true);
        dlg.setSize(360, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setUndecorated(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.WHITE);

        // Header màu theo trạng thái phòng
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 12, 12);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 72));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel("🚪  " + room.getId());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        JLabel statusLbl = new JLabel(room.getStatusText());
        statusLbl.setFont(UITheme.FONT_SMALL);
        statusLbl.setForeground(new Color(255, 255, 255, 200));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        headerText.add(titleLbl);
        headerText.add(statusLbl);

        JButton btnMembers = new JButton("👥 Thành Viên  ");
        btnMembers.setFont(UITheme.FONT_SMALL);
        btnMembers.setForeground(Color.WHITE);
        btnMembers.setBackground(new Color(255, 255, 255, 60));
        btnMembers.setOpaque(false);
        btnMembers.setContentAreaFilled(false);
        btnMembers.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btnMembers.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMembers.addActionListener(e -> showMembersDialog(room, dlg));

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerRight.setOpaque(false);
        headerRight.add(btnMembers);

        header.add(headerText,  BorderLayout.CENTER);
        header.add(headerRight, BorderLayout.EAST);

        // Body info
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UITheme.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        int available = room.getCapacity() - room.getOccupied();
        Object[][] info = {
            {"Tên phòng",   room.getId()},
            {"Loại phòng",  room.getType()},
            {"Tầng",        "Tầng " + room.getFloor()},
            {"Sức chứa",    room.getCapacity() + " người"},
            {"Đang ở",      room.getOccupied() + " người"},
            {"Còn trống",   available + " chỗ trống"},
        };
        for (int i = 0; i < info.length; i++) {
            g.gridx = 0; g.gridy = i; g.gridwidth = 1;
            JLabel k = new JLabel(info[i][0] + ":");
            k.setFont(UITheme.FONT_BOLD);
            k.setForeground(UITheme.TEXT_SECONDARY);
            k.setPreferredSize(new Dimension(100, 22));
            body.add(k, g);

            g.gridx = 1;
            JLabel v = new JLabel(info[i][1].toString());
            v.setFont(UITheme.FONT_BODY);
            v.setForeground(UITheme.TEXT_PRIMARY);
            body.add(v, g);
        }

        // Footer: nút Xóa phòng (trái) + Đóng (phải)
        JButton btnDelete = UITheme.outlineBtn("🗑 Xóa phòng");
        btnDelete.setForeground(UITheme.DANGER);
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dlg,
                "Bạn có chắc chắn muốn xóa phòng " + room.getId() + " không?",
                "Xác nhận xóa phòng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (DatabaseService.deleteRoom(room.getId())) {
                    JOptionPane.showMessageDialog(dlg, "✅ Đã xóa phòng " + room.getId() + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    applyFilter();
                } else {
                    JOptionPane.showMessageDialog(dlg, "❌ Xóa phòng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnClose = UITheme.primaryBtn("Đóng");
        btnClose.addActionListener(e -> dlg.dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        footerLeft.setOpaque(false);
        footerLeft.add(btnDelete);

        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        footerRight.setOpaque(false);
        footerRight.add(btnClose);

        footer.add(footerLeft,  BorderLayout.WEST);
        footer.add(footerRight, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private Color[] roomColors(Room.Status status) {
        return switch(status) {
            case AVAILABLE    -> new Color[]{new Color(0xF0FDF4), new Color(0x22C55E), UITheme.SUCCESS_TEXT, UITheme.SUCCESS};
            case NEARLY_FULL  -> new Color[]{new Color(0xFEFCE8), new Color(0xF59E0B), UITheme.WARNING_TEXT, UITheme.WARNING};
            case FULL         -> new Color[]{new Color(0xFEF2F2), new Color(0xEF4444), UITheme.DANGER_TEXT,  UITheme.DANGER};
            case MAINTENANCE  -> new Color[]{UITheme.BG_SECONDARY,UITheme.BORDER,    UITheme.TEXT_MUTED,    UITheme.TEXT_MUTED};
        };
    }

    /** Dialog thêm phòng mới */
    private void showAddRoomDialog() {
        JDialog dlg = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Thêm phòng mới", true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridLayout(0, 2, 10, 10));
        content.setBorder(new EmptyBorder(20, 20, 10, 20));
        content.setBackground(UITheme.WHITE);

        JTextField tfId       = UITheme.textField("VD: A501");
        JComboBox<String> cbType = UITheme.comboBox(new String[]{"4 người", "6 người"});
        JTextField tfFloor    = UITheme.textField("VD: 5");
        JTextField tfCapacity = UITheme.textField("4");

        content.add(label("Tên phòng *"));  content.add(tfId);
        content.add(label("Loại phòng"));   content.add(cbType);
        content.add(label("Tầng *"));       content.add(tfFloor);
        content.add(label("Sức chứa *"));   content.add(tfCapacity);

        cbType.addActionListener(e ->
            tfCapacity.setText(cbType.getSelectedIndex() == 0 ? "4" : "6"));

        JButton btnSave   = UITheme.primaryBtn("💾 Lưu");
        JButton btnCancel = UITheme.outlineBtn("Hủy");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String id = tfId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "⚠ Vui lòng nhập Tên phòng!", "Lưu ý", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int floor    = Integer.parseInt(tfFloor.getText().trim());
                int capacity = Integer.parseInt(tfCapacity.getText().trim());
                Room r = new Room(id, id, cbType.getSelectedItem().toString(), floor, capacity, 0);
                if (DatabaseService.addRoom(r)) {
                    JOptionPane.showMessageDialog(dlg, "✅ Đã thêm phòng " + id + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    applyFilter();   // reload grid với phòng mới
                } else {
                    JOptionPane.showMessageDialog(dlg, "❌ Lưu thất bại! Mã phòng có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "⚠ Tầng và Sức chứa phải là số nguyên!", "Lưu ý", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.WHITE);
        btnRow.setBorder(new EmptyBorder(0, 0, 12, 12));
        btnRow.add(btnCancel); btnRow.add(btnSave);

        dlg.setLayout(new BorderLayout());
        dlg.add(content,  BorderLayout.CENTER);
        dlg.add(btnRow,   BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    /** Dialog xem thông tin các thành viên đang ở trong phòng */
    private void showMembersDialog(Room room, JDialog parent) {
        JDialog dlg2 = new JDialog(parent, "Thành viên phòng " + room.getId(), true);
        dlg2.setSize(700, 380);
        dlg2.setLocationRelativeTo(parent);

        // Lấy sinh viên đang ở trong phòng này
        java.util.List<com.sdms.model.Student> members = DatabaseService.getAllStudents().stream()
            .filter(s -> room.getId().equals(s.getRoomId()))
            .collect(java.util.stream.Collectors.toList());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("👥  Danh sách thành viên — Phòng " + room.getId()
            + "  (" + members.size() + "/" + room.getCapacity() + " người)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Table
        String[] cols = {"Mã SV", "Họ và tên", "Giới tính", "Ngày sinh", "SĐT", "Email", "Khoa", "Trạng thái"};
        javax.swing.table.DefaultTableModel tModel = new javax.swing.table.DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (members.isEmpty()) {
            // Không có ai
        } else {
            for (com.sdms.model.Student s : members) {
                tModel.addRow(new Object[]{
                    s.getId(), s.getFullName(), s.getGender(), s.getBirthDate(),
                    s.getPhone(), s.getEmail(), s.getFaculty(), s.getStatus()
                });
            }
        }

        JTable table = new JTable(tModel);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UITheme.BORDER);
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getViewport().setBackground(UITheme.WHITE);

        // Thông báo nếu phòng trống
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.WHITE);
        center.setBorder(new EmptyBorder(12, 16, 12, 16));
        if (members.isEmpty()) {
            JLabel empty = new JLabel("Phòng hiện chưa có sinh viên nào đang ở.", SwingConstants.CENTER);
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            center.add(empty, BorderLayout.CENTER);
        } else {
            center.add(scroll, BorderLayout.CENTER);
        }

        // Footer
        JButton btnClose = UITheme.primaryBtn("Đóng");
        btnClose.addActionListener(e -> dlg2.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));
        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg2.setContentPane(root);
        dlg2.setVisible(true);
    }
}
