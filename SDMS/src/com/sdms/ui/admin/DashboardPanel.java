package com.sdms.ui.admin;

import com.sdms.model.Student;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.setBackground(UITheme.BG_LIGHT);
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Page title
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Xin chào Quản trị viên! Hệ thống đang hoạt động bình thường.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 18, 0));

        // Stat cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(LEFT_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cardsRow.add(statCard("👥", "Tổng sinh viên",     String.valueOf(DatabaseService.getAllStudents().size()),    "↑ +10 tháng này",  new Color(0xDBEAFE), UITheme.PRIMARY,  UITheme.SUCCESS));
        cardsRow.add(statCard("🚪", "Tổng số phòng",       String.valueOf(DatabaseService.totalRooms()),       "4 tầng · 1 tòa",    new Color(0xDCFCE7), UITheme.SUCCESS, UITheme.TEXT_MUTED));
        cardsRow.add(statCard("🟡", "Phòng còn trống",     String.valueOf(DatabaseService.emptyRooms()),       "↓ -2 tuần này",     new Color(0xFEF9C3), UITheme.WARNING, UITheme.DANGER));
        cardsRow.add(statCard("📄", "HĐ đang hoạt động",  String.valueOf(DatabaseService.countStudentsByStatus("Đang ở")),   "↑ 95% tỷ lệ",       new Color(0xEDE9FE), UITheme.PURPLE,  UITheme.SUCCESS));
        cardsRow.add(statCard("💰", "Doanh thu tháng",    String.format("%,d đ",DatabaseService.monthRevenue("06/2026")),"↑ +8% tháng trước",new Color(0xFCE7F3), new Color(0xBE185D), UITheme.SUCCESS));

        // Row 2: bar chart + room status
        JPanel row2 = new JPanel(new GridLayout(1, 2, 14, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        row2.setBorder(new EmptyBorder(14, 0, 14, 0));
        row2.add(buildBarChartCard());
        row2.add(buildRoomStatusCard());

        // Row 3: student table + revenue chart
        JPanel row3 = new JPanel(new GridLayout(1, 2, 14, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        row3.add(buildStudentTableCard());
        row3.add(buildRevenueCard());

        root.add(title); root.add(sub);
        root.add(cardsRow); root.add(row2); root.add(row3);
        return root;
    }

    private JPanel statCard(String icon, String label, String value, String trend, Color iconBg, Color iconFg, Color trendColor) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fill(new RoundRectangle2D.Float(0,0,36,36,8,8));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(36,36); }
        };
        iconBox.setOpaque(false);
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI",Font.PLAIN,16));
        iconBox.setLayout(new BorderLayout());
        iconBox.add(ico);
        top.add(iconBox);
        card.add(top, BorderLayout.NORTH);

        JLabel num = new JLabel(value.length()>9 ? value.substring(0,9)+"…" : value);
        num.setFont(new Font("Segoe UI", Font.BOLD, 22));
        num.setForeground(UITheme.TEXT_PRIMARY);
        card.add(num, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_TINY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel trendLbl = new JLabel(trend);
        trendLbl.setFont(UITheme.FONT_TINY);
        trendLbl.setForeground(trendColor);
        bottom.add(lbl, BorderLayout.NORTH);
        bottom.add(trendLbl, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBarChartCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Sinh viên nhập học theo tháng");
        title.setFont(UITheme.FONT_H3);
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        int[] data = {85,92,78,110,145,132,168,190,205,188,220,248};
        String[] labels = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        int max = 250;

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight()-20;
                int barW = w / (data.length * 2);
                int gap  = barW / 2;
                for (int i = 0; i < data.length; i++) {
                    int barH = (int)((double)data[i]/max * h);
                    int x = gap + i * (barW + gap);
                    Color c = (i == data.length-1) ? UITheme.PRIMARY : new Color(0xBFDBFE);
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(x, h-barH, barW, barH, 4, 4));
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i]))/2, h+14);
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 140); }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomStatusCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Phòng sắp đầy");
        title.setFont(UITheme.FONT_H3);
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        Object[][] items = {
            {"Phòng A301", 8, 8, UITheme.DANGER},
            {"Phòng B204", 7, 8, UITheme.WARNING},
            {"Phòng C112", 6, 8, UITheme.WARNING},
            {"Phòng D405", 5, 8, UITheme.SUCCESS},
        };
        for (Object[] it : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor((Color)it[3]); g2.fillOval(0,0,8,8); g2.dispose();
                }
                @Override public Dimension getPreferredSize(){ return new Dimension(8,8); }
            };
            JLabel name = new JLabel((String)it[0]);
            name.setFont(UITheme.FONT_BODY); name.setForeground(UITheme.TEXT_PRIMARY);
            name.setPreferredSize(new Dimension(90,20));

            JPanel bar = buildProgressBar((int)it[1], (int)it[2], (Color)it[3]);

            JLabel pct = new JLabel(it[1]+"/"+it[2]);
            pct.setFont(UITheme.FONT_SMALL); pct.setForeground((Color)it[3]);

            row.add(dot); row.add(name); row.add(bar); row.add(pct);
            list.add(row);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildProgressBar(int val, int max, Color color) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0,3,getWidth(),6,6,6));
                int filled = (int)((double)val/max * getWidth());
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0,3,filled,6,6,6));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(80,12); }
        };
    }

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
        titleRow.add(title, BorderLayout.WEST); titleRow.add(more, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        String[] cols = {"Mã SV","Họ tên","Phòng","Trường","Trạng thái"};
        Object[][] rowData = DatabaseService.getAllStudents().stream()
            .limit(5)
            .map(s -> new Object[]{s.getId(), s.getFullName(),
                s.getRoomId().isEmpty()?"—":s.getRoomId(), s.getUniversity(), s.getStatus()})
            .toArray(Object[][]::new);

        DefaultTableModel model = new DefaultTableModel(rowData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);

        // Status column renderer
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
                lbl.setOpaque(true); lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
                return lbl;
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(UITheme.FONT_SMALL);
        table.setRowHeight(34);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRevenueCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        JLabel title = new JLabel("Doanh thu 6 tháng (VNĐ)");
        title.setFont(UITheme.FONT_H3); title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        long[] rev = {240_000_000L, 265_000_000L, 258_000_000L, 280_000_000L, 274_000_000L, 284_000_000L};
        String[] labels = {"T7","T8","T9","T10","T11","T12"};
        long maxVal = 300_000_000L;

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight()-22;
                int barW = (w - 40) / (rev.length * 2);
                for (int i = 0; i < rev.length; i++) {
                    int barH = (int)((double)rev[i]/maxVal * h);
                    int x = 20 + i * (barW * 2) + barW/2;
                    Color c = (i == rev.length-1) ? UITheme.PURPLE : new Color(0xC4B5FD);
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(x, h-barH, barW, barH, 4, 4));
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(labels[i], x + (barW-fm.stringWidth(labels[i]))/2, h+15);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }
}
