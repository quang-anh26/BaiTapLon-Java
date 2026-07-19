package com.sdms.ui.admin;

import com.sdms.model.Room;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private Runnable onNavigateToRooms;

    public DashboardPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    /** AdminFrame gọi method này để truyền callback navigate */
    public void setOnNavigateToRooms(Runnable r) { this.onNavigateToRooms = r; }

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Xin chào Quản trị viên! Hệ thống đang hoạt động bình thường.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 18, 0));

        // ── 5 stat cards (dữ liệu từ DB) ────────────────────────
        String curMonth = String.format("%02d/%d",
            LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        long revenue = DatabaseService.monthRevenue(curMonth);
        String revStr = revenue > 0
            ? String.format("%,d đ", revenue)
            : String.format("%,d đ", DatabaseService.monthRevenue());

        JPanel cardsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(LEFT_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cardsRow.add(statCard("👥","Tổng sinh viên",
            String.valueOf(DatabaseService.getAllStudents().size()),
            "Đang quản lý", new Color(0xDBEAFE), UITheme.PRIMARY, UITheme.TEXT_MUTED));
        cardsRow.add(statCard("🚪","Tổng số phòng",
            String.valueOf(DatabaseService.totalRooms()),
            "Phòng còn trống: " + DatabaseService.emptyRooms(),
            new Color(0xDCFCE7), UITheme.SUCCESS, UITheme.TEXT_MUTED));
        cardsRow.add(statCard("🟡","Phòng còn trống",
            String.valueOf(DatabaseService.emptyRooms()),
            "Trên tổng " + DatabaseService.totalRooms() + " phòng",
            new Color(0xFEF9C3), UITheme.WARNING, UITheme.TEXT_MUTED));
        cardsRow.add(statCard("📄","HĐ đang hiệu lực",
            String.valueOf(DatabaseService.countStudentsByStatus("Đang ở")),
            "Sinh viên đang ở", new Color(0xEDE9FE), UITheme.PURPLE, UITheme.TEXT_MUTED));
        cardsRow.add(statCard("💰","Doanh thu tháng " + curMonth,
            revStr.length() > 13 ? revStr.substring(0, 13) + "…" : revStr,
            "Hóa đơn đã thanh toán", new Color(0xFCE7F3), new Color(0xBE185D), UITheme.SUCCESS));

        // ── Row 2: Tình trạng phòng theo tầng + Phòng cần chú ý ─
        JPanel row2 = new JPanel(new GridLayout(1, 2, 14, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        row2.setBorder(new EmptyBorder(14, 0, 14, 0));
        row2.add(buildFloorSummaryCard());
        row2.add(buildRoomStatusCard());

        // ── Row 3: Sinh viên mới nhất + Doanh thu cả năm ─────────
        JPanel row3 = new JPanel(new GridLayout(1, 2, 14, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        row3.add(buildStudentTableCard());
        row3.add(buildRevenueCard());

        root.add(title); root.add(sub);
        root.add(cardsRow); root.add(row2); root.add(row3);
        return root;
    }

    // ── statCard ─────────────────────────────────────────────────
    private JPanel statCard(String icon, String label, String value,
                            String trend, Color iconBg, Color iconFg, Color trendColor) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, 36, 36, 8, 8));
                g2.dispose(); super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
        };
        iconBox.setOpaque(false);
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        iconBox.setLayout(new BorderLayout());
        iconBox.add(ico);
        top.add(iconBox);
        card.add(top, BorderLayout.NORTH);

        JLabel num = new JLabel(value);
        num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        num.setForeground(UITheme.TEXT_PRIMARY);
        card.add(num, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_TINY); lbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel trendLbl = new JLabel(trend);
        trendLbl.setFont(UITheme.FONT_TINY); trendLbl.setForeground(trendColor);
        bottom.add(lbl, BorderLayout.NORTH);
        bottom.add(trendLbl, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ── Tình trạng phòng theo tầng (thay "sinh viên nhập học") ───
    private JPanel buildFloorSummaryCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Tình trạng phòng theo tầng");
        title.setFont(UITheme.FONT_H3); title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        List<Room> rooms = DatabaseService.getAllRooms();

        // Group theo tầng (ký tự đầu của số phòng: P1xx→Tầng 1, P2xx→Tầng 2...)
        int[] floors = {1, 2, 3, 4};
        int[] total  = new int[4];
        int[] used   = new int[4];
        int[] cap    = new int[4];
        for (Room r : rooms) {
            if (r.getId() == null || r.getId().length() < 2) continue;
            char fc = r.getId().charAt(1); // P1xx → '1'
            int idx = fc - '1';
            if (idx >= 0 && idx < 4) {
                total[idx]++;
                used[idx]  += r.getOccupied();
                cap[idx]   += r.getCapacity();
            }
        }

        JPanel grid = new JPanel(new GridLayout(4, 1, 0, 6));
        grid.setOpaque(false);

        Color[] colors = {UITheme.PRIMARY, UITheme.SUCCESS, UITheme.WARNING, UITheme.PURPLE};
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);

            JLabel lbl = new JLabel("Tầng " + floors[i] + "  (" + total[i] + " phòng)");
            lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_PRIMARY);
            lbl.setPreferredSize(new Dimension(130, 18));

            int c = cap[i] > 0 ? cap[i] : 1;
            int u = used[i];
            Color barColor = (double)u/c >= 0.9 ? UITheme.DANGER
                           : (double)u/c >= 0.6 ? UITheme.WARNING : UITheme.SUCCESS;

            JPanel bar = buildProgressBar(u, c, barColor);

            JLabel pct = new JLabel(u + "/" + cap[i] + " người");
            pct.setFont(UITheme.FONT_TINY); pct.setForeground(barColor);
            pct.setPreferredSize(new Dimension(80, 18));

            row.add(lbl, BorderLayout.WEST);
            row.add(bar, BorderLayout.CENTER);
            row.add(pct, BorderLayout.EAST);
            grid.add(row);
        }
        card.add(grid, BorderLayout.CENTER);

        // Chú thích
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(UITheme.SUCCESS, "Còn chỗ"));
        legend.add(legendDot(UITheme.WARNING, "Gần đầy"));
        legend.add(legendDot(UITheme.DANGER,  "Đã đầy"));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JLabel legendDot(Color c, String text) {
        JLabel l = new JLabel("● " + text);
        l.setFont(UITheme.FONT_TINY); l.setForeground(c);
        return l;
    }

    // ── Phòng cần chú ý: load từ DB, có "Xem tất cả →" ──────────
    private JPanel buildRoomStatusCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Phòng cần chú ý");
        title.setFont(UITheme.FONT_H3); title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel more = new JLabel("Xem tất cả →");
        more.setFont(UITheme.FONT_SMALL); more.setForeground(UITheme.PRIMARY);
        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        more.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onNavigateToRooms != null) onNavigateToRooms.run();
            }
        });
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(more,  BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        // Lấy phòng từ DB: ưu tiên đã đầy trước, rồi gần đầy, tối đa 5 phòng
        List<Room> rooms = DatabaseService.getAllRooms().stream()
            .filter(r -> r.getStatus() == Room.Status.FULL
                      || r.getStatus() == Room.Status.NEARLY_FULL
                      || r.getStatus() == Room.Status.AVAILABLE)
            .sorted(Comparator.comparingDouble((Room r) -> -(double)r.getOccupied() / Math.max(r.getCapacity(), 1)))
            .limit(5)
            .collect(Collectors.toList());

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (rooms.isEmpty()) {
            JLabel empty = new JLabel("Không có dữ liệu phòng.");
            empty.setFont(UITheme.FONT_SMALL); empty.setForeground(UITheme.TEXT_MUTED);
            list.add(empty);
        } else {
            for (Room r : rooms) {
                int occ = r.getOccupied(), c = r.getCapacity();
                Color dotColor = r.getStatus() == Room.Status.FULL     ? UITheme.DANGER
                               : r.getStatus() == Room.Status.NEARLY_FULL ? UITheme.WARNING
                               : UITheme.SUCCESS;

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

                JPanel dot = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(dotColor); g2.fillOval(0, 2, 8, 8); g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(8, 12); }
                };

                JLabel name = new JLabel("Phòng " + r.getId());
                name.setFont(UITheme.FONT_BODY); name.setForeground(UITheme.TEXT_PRIMARY);
                name.setPreferredSize(new Dimension(80, 20));

                JPanel bar = buildProgressBar(occ, Math.max(c, 1), dotColor);

                JLabel pct = new JLabel(occ + "/" + c);
                pct.setFont(UITheme.FONT_SMALL); pct.setForeground(dotColor);

                row.add(dot); row.add(name); row.add(bar); row.add(pct);
                list.add(row);
            }
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    // ── Progress bar ─────────────────────────────────────────────
    private JPanel buildProgressBar(int val, int max, Color color) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 3, getWidth(), 6, 6, 6));
                int filled = max > 0 ? (int)((double)val / max * getWidth()) : 0;
                if (filled > 0) { g2.setColor(color); g2.fill(new RoundRectangle2D.Float(0, 3, filled, 6, 6, 6)); }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(80, 12); }
        };
    }

    // ── Sinh viên mới nhất ────────────────────────────────────────
    private JPanel buildStudentTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Sinh viên mới nhất");
        title.setFont(UITheme.FONT_H3); title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel more = new JLabel("Xem tất cả →");
        more.setFont(UITheme.FONT_SMALL); more.setForeground(UITheme.PRIMARY);
        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        more.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(DashboardPanel.this);
                if (win instanceof AdminFrame af) af.navigateTo("Quản lý sinh viên");
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { more.setForeground(UITheme.PRIMARY_DARK); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { more.setForeground(UITheme.PRIMARY); }
        });
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(more,  BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Mã SV", "Họ tên", "Phòng", "Khoa", "Trạng thái"};
        Object[][] rowData = DatabaseService.getAllStudents().stream()
            .limit(5)
            .map(s -> new Object[]{
                s.getId(), s.getFullName(),
                (s.getRoomId() == null || s.getRoomId().isEmpty()) ? "—" : s.getRoomId(),
                (s.getFaculty() == null || s.getFaculty().isEmpty()) ? "—" : s.getFaculty(),
                s.getStatus()
            })
            .toArray(Object[][]::new);

        DefaultTableModel model = new DefaultTableModel(rowData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowVerticalLines(false);
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setFont(UITheme.FONT_SMALL);
        table.setRowHeight(32);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = UITheme.badge(v.toString(),
                    switch(v.toString()) {
                        case "Đang ở"      -> UITheme.SUCCESS_BG;
                        case "Mới đăng ký" -> UITheme.INFO_BG;
                        case "Chờ duyệt"   -> UITheme.WARNING_BG;
                        default            -> UITheme.DANGER_BG;
                    },
                    switch(v.toString()) {
                        case "Đang ở"      -> UITheme.SUCCESS_TEXT;
                        case "Mới đăng ký" -> UITheme.INFO_TEXT;
                        case "Chờ duyệt"   -> UITheme.WARNING_TEXT;
                        default            -> UITheme.DANGER_TEXT;
                    });
                lbl.setOpaque(true);
                lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── Doanh thu cả năm — lấy từ DB ─────────────────────────────
    private JPanel buildRevenueCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 4));

        int year = LocalDate.now().getYear();
        JLabel title = new JLabel("Doanh thu các tháng năm " + year + " (VNĐ)");
        title.setFont(UITheme.FONT_H3); title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        // Load doanh thu 12 tháng từ DB
        long[] rev    = new long[12];
        String[] lbls = new String[12];
        long maxVal   = 1L;
        for (int m = 1; m <= 12; m++) {
            String key = String.format("%02d/%d", m, year);
            rev[m-1]  = DatabaseService.monthRevenue(key);
            lbls[m-1] = "T" + m;
            if (rev[m-1] > maxVal) maxVal = rev[m-1];
        }
        // Nếu toàn 0 (chưa có dữ liệu), hiện placeholder
        final long[] revFinal  = rev;
        final long   maxFinal  = maxVal > 0 ? maxVal : 1L;
        final int    curMonth  = LocalDate.now().getMonthValue();

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight() - 24;
                int gap  = 4;                              // khoảng cách cố định giữa các cột
                int barW = (w - gap * 13) / 12;           // chia đều toàn bộ chiều rộng
                if (barW < 4) barW = 4;
                // startX để các cột căn giữa panel
                int totalW  = 12 * barW + 13 * gap;
                int startX  = (w - totalW) / 2;
                for (int i = 0; i < 12; i++) {
                    int barH = revFinal[i] > 0 ? (int)((double)revFinal[i] / maxFinal * h) : 4;
                    int x = startX + gap + i * (barW + gap);
                    Color c = (i + 1 == curMonth) ? UITheme.PURPLE : new Color(0xC4B5FD);
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(x, h - barH, barW, barH, 3, 3));
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(lbls[i], x + (barW - fm.stringWidth(lbls[i])) / 2, h + 16);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);

        // Tổng năm
        long total = 0; for (long v : rev) total += v;
        JLabel sum = new JLabel("Tổng " + year + ": " + String.format("%,d đ", total));
        sum.setFont(UITheme.FONT_TINY); sum.setForeground(UITheme.TEXT_SECONDARY);
        card.add(sum, BorderLayout.SOUTH);
        return card;
    }
}