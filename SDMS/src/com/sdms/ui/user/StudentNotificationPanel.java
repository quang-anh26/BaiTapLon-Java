package com.sdms.ui.user;

import com.sdms.model.Notification;
import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel thông báo hệ thống dành cho sinh viên.
 * Toàn bộ dữ liệu lấy từ database qua DatabaseService.getNotificationsForStudent().
 * Đánh dấu đã đọc ghi ngược vào DB qua DatabaseService.markNotificationRead().
 */
public class StudentNotificationPanel extends JPanel {

    private final User    currentUser;
    private final Student student;

    private List<Notification> notices = new ArrayList<>();
    private JPanel             listPanel;
    private JLabel             lblUnread;
    private String             currentFilter = "Tất cả";

    public StudentNotificationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.student     = findStudent();
        loadFromDatabase();

        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Tìm student từ DB ─────────────────────────────────────────

    private Student findStudent() {
        String sid = currentUser.getStudentId();
        if (sid != null && !sid.isEmpty()) {
            Student s = DatabaseService.getAllStudents().stream()
                .filter(st -> st.getId().equals(sid))
                .findFirst().orElse(null);
            if (s != null) return s;
        }
        return DatabaseService.getAllStudents().stream()
            .filter(st -> st.getId().equalsIgnoreCase(currentUser.getUsername()))
            .findFirst().orElse(null);
    }

    // ── Load thông báo từ DB ──────────────────────────────────────

    private void loadFromDatabase() {
        notices.clear();
        String sid    = student != null ? student.getId()    : currentUser.getStudentId();
        String roomId = student != null ? student.getRoomId() : "";
        if (sid != null && !sid.isEmpty()) {
            notices.addAll(DatabaseService.getNotificationsForStudent(sid, roomId));
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

        JLabel title = new JLabel("🔔  Thông báo từ hệ thống");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Thông báo");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        long unread = notices.stream().filter(n -> !n.isRead()).count();
        lblUnread = UITheme.badge(unread + " chưa đọc",
            unread > 0 ? UITheme.DANGER_BG : UITheme.BG_SECONDARY,
            unread > 0 ? UITheme.DANGER    : UITheme.TEXT_SECONDARY);

        JButton btnMarkAll = UITheme.outlineBtn("✓ Đánh dấu tất cả đã đọc");
        btnMarkAll.addActionListener(e -> markAllRead());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(lblUnread);
        right.add(btnMarkAll);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Nội dung ──────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_LIGHT);

        p.add(buildFilterBar(), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UITheme.BG_LIGHT);
        listPanel.setBorder(new EmptyBorder(12, 20, 20, 20));

        renderList();

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Thanh filter theo loại thông báo ─────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        bar.setBackground(UITheme.WHITE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));

        String[] filters = {"Tất cả", "Chưa đọc", "Hóa đơn", "Hợp đồng", "Vi phạm", "Kiểm tra", "Chung"};
        ButtonGroup bg = new ButtonGroup();

        for (String f : filters) {
            JToggleButton btn = filterBtn(f);
            bg.add(btn);
            if (f.equals("Tất cả")) btn.setSelected(true);
            btn.addActionListener(e -> {
                currentFilter = f;
                renderList();
            });
            bar.add(btn);
        }
        return bar;
    }

    private JToggleButton filterBtn(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isSelected() ? UITheme.PRIMARY : UITheme.WHITE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(isSelected() ? UITheme.WHITE : UITheme.TEXT_SECONDARY);
                g2.setFont(UITheme.FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(UITheme.FONT_SMALL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(text.length() * 10 + 20, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Render danh sách theo filter ─────────────────────────────

    private void renderList() {
        listPanel.removeAll();

        List<Notification> filtered = notices.stream()
            .filter(n -> {
                return switch (currentFilter) {
                    case "Chưa đọc"  -> !n.isRead();
                    case "Tất cả"    -> true;
                    case "Hóa đơn"   -> n.getType() == Notification.Type.INVOICE;
                    case "Hợp đồng"  -> n.getType() == Notification.Type.CONTRACT;
                    case "Vi phạm"   -> n.getType() == Notification.Type.VIOLATION;
                    case "Kiểm tra"  -> n.getType() == Notification.Type.INSPECTION;
                    case "Chung"     -> n.getType() == Notification.Type.GENERAL
                                     || n.getType() == Notification.Type.URGENT;
                    default          -> true;
                };
            })
            .collect(Collectors.toList());

        // Thông báo ghim lên trước
        List<Notification> pinned  = filtered.stream().filter(Notification::isPinned).collect(Collectors.toList());
        List<Notification> regular = filtered.stream().filter(n -> !n.isPinned()).collect(Collectors.toList());

        if (!pinned.isEmpty()) {
            listPanel.add(sectionLabel("📌  Thông báo quan trọng"));
            listPanel.add(Box.createVerticalStrut(6));
            for (Notification n : pinned) {
                listPanel.add(noticeCard(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
            listPanel.add(Box.createVerticalStrut(6));
        }

        if (!regular.isEmpty()) {
            listPanel.add(sectionLabel("🔔  Tất cả thông báo"));
            listPanel.add(Box.createVerticalStrut(6));
            for (Notification n : regular) {
                listPanel.add(noticeCard(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("Không có thông báo nào.", SwingConstants.CENTER);
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(empty);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ── Card một thông báo ────────────────────────────────────────

    private JPanel noticeCard(Notification n) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));
        card.setBackground(n.isRead() ? UITheme.WHITE : new Color(0xEFF6FF));
        card.setBorder(new CompoundBorder(
            new LineBorder(n.isRead() ? UITheme.BORDER : new Color(0xBFDBFE), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // Icon loại thông báo
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(44, 44));

        JLabel lblIcon = new JLabel(n.getTypeIcon(), SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setPreferredSize(new Dimension(44, 44));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(n.isRead() ? UITheme.BG_SECONDARY : new Color(0xDBEAFE));
        lblIcon.setBorder(new EmptyBorder(8, 8, 8, 8));
        iconPanel.add(lblIcon, BorderLayout.CENTER);

        // Nội dung
        JLabel lblTitle = new JLabel(n.getTitle());
        lblTitle.setFont(n.isRead() ? UITheme.FONT_BODY : UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblContent = new JLabel(
            "<html><body style='width:500px;color:#6B7280'>" + n.getContent() + "</body></html>");
        lblContent.setFont(UITheme.FONT_SMALL);

        JLabel lblMeta = new JLabel(
            n.getTypeIcon() + "  " + n.getTypeText() + "  ·  " + n.getRelativeTime());
        lblMeta.setFont(UITheme.FONT_TINY);
        lblMeta.setForeground(UITheme.TEXT_MUTED);

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(lblTitle);
        textPanel.add(lblContent);
        textPanel.add(lblMeta);

        // Nút đánh dấu đã đọc
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(110, 40));

        if (!n.isRead()) {
            JButton btnRead = UITheme.outlineBtn("✓ Đã đọc");
            btnRead.setPreferredSize(new Dimension(100, 32));
            btnRead.addActionListener(e -> {
                String sid = student != null ? student.getId() : "";
                if (!sid.isEmpty()) {
                    DatabaseService.markNotificationRead(n.getId(), sid);
                }
                n.setRead(true);
                renderList();
                updateUnreadBadge();
            });
            btnPanel.add(btnRead, BorderLayout.NORTH);
        } else {
            JLabel lblRead = new JLabel("✓ Đã đọc");
            lblRead.setFont(UITheme.FONT_TINY);
            lblRead.setForeground(UITheme.TEXT_MUTED);
            lblRead.setHorizontalAlignment(SwingConstants.RIGHT);
            btnPanel.add(lblRead, BorderLayout.NORTH);
        }

        card.add(iconPanel,  BorderLayout.WEST);
        card.add(textPanel,  BorderLayout.CENTER);
        card.add(btnPanel,   BorderLayout.EAST);
        return card;
    }

    // ── Đánh dấu tất cả đã đọc — ghi vào DB ─────────────────────

    private void markAllRead() {
        String sid = student != null ? student.getId() : "";
        for (Notification n : notices) {
            if (!n.isRead()) {
                if (!sid.isEmpty()) {
                    DatabaseService.markNotificationRead(n.getId(), sid);
                }
                n.setRead(true);
            }
        }
        renderList();
        updateUnreadBadge();
    }

    private void updateUnreadBadge() {
        long unread = notices.stream().filter(n -> !n.isRead()).count();
        lblUnread.setText(unread + " chưa đọc");
        lblUnread.setBackground(unread > 0 ? UITheme.DANGER_BG : UITheme.BG_SECONDARY);
        lblUnread.setForeground(unread > 0 ? UITheme.DANGER    : UITheme.TEXT_SECONDARY);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_LABEL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return l;
    }
}
