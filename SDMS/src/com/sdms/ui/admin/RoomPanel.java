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
        JLabel title = new JLabel("🚪  Quản lý phòng — Tòa A");
        title.setFont(UITheme.FONT_H2); title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Sơ đồ trực quan vị trí phòng theo tầng");
        sub.setFont(UITheme.FONT_TINY); sub.setForeground(UITheme.TEXT_MUTED);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JComboBox<String> cbFloor = UITheme.comboBox(new String[]{"Tòa A","Tòa B","Tòa C","Tòa D"});
        cbFloor.setPreferredSize(new Dimension(110, 34));
        JButton btnAdd = UITheme.primaryBtn("➕  Thêm phòng");
        right.add(cbFloor); right.add(btnAdd);

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
        rebuildCards(	DatabaseService.getAllRooms());

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
        row.add(miniStat("Còn chỗ",       empty,  UITheme.SUCCESS));
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
        legend.add(legendItem("Còn chỗ",     UITheme.SUCCESS));
        legend.add(legendItem("Gần đầy ≥50%",UITheme.WARNING));
        legend.add(legendItem("Đã đầy",      UITheme.DANGER));
        legend.add(legendItem("Bảo trì",     UITheme.TEXT_MUTED));

        // Filters — dùng ButtonGroup để chỉ 1 nút active tại một thời điểm
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        ButtonGroup filterGroup = new ButtonGroup();
        String[] filterOpts = {"Tất cả","Còn chỗ","Gần đầy","Đã đầy","Phòng 4 người","Phòng 8 người"};
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
        List<Room> filtered = DatabaseService.getAllRooms().stream().filter(r -> switch(filterStatus) {
            case "Còn chỗ"      -> r.getStatus() == Room.Status.AVAILABLE;
            case "Gần đầy"      -> r.getStatus() == Room.Status.NEARLY_FULL;
            case "Đã đầy"       -> r.getStatus() == Room.Status.FULL;
            case "Phòng 4 người"-> r.getCapacity() == 4;
            case "Phòng 8 người"-> r.getCapacity() == 8;
            default             -> true;
        }).collect(Collectors.toList());
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
        dlg.setSize(360, 340);
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

        JLabel titleLbl = new JLabel("🚪  " + room.getName());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        JLabel statusLbl = new JLabel(room.getStatusText());
        statusLbl.setFont(UITheme.FONT_SMALL);
        statusLbl.setForeground(new Color(255, 255, 255, 200));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        headerText.add(titleLbl);
        headerText.add(statusLbl);
        header.add(headerText, BorderLayout.CENTER);

        // Body info
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UITheme.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        int available = room.getCapacity() - room.getOccupied();
        Object[][] info = {
            {"Mã phòng",    room.getId()},
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

        // Close button
        JButton btnClose = UITheme.primaryBtn("Đóng");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));
        footer.add(btnClose);

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
}
