package com.sdms.ui.user;

import com.sdms.model.Room;
import com.sdms.model.Student;
import com.sdms.model.User;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel xem thông tin phòng mà sinh viên đang ở.
 * Hiển thị: Chi tiết phòng | Danh sách bạn cùng phòng | Nội quy phòng.
 */
public class StudentRoomPanel extends JPanel {

    private final User    currentUser;
    private final Student student;
    private final Room    room;

    public StudentRoomPanel(User currentUser) {
        this.currentUser = currentUser;
        this.student     = findStudent();
        this.room        = findRoom();

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

    private Room findRoom() {
        if (student == null || student.getRoomId().isEmpty()) return null;
        return DatabaseService.getAllRooms().stream()
            .filter(r -> r.getId().equals(student.getRoomId()))
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

        JLabel title = new JLabel("🛏  Thông tin phòng ở");
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Trang chủ / Thông tin phòng");
        sub.setFont(UITheme.FONT_TINY);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub,   BorderLayout.SOUTH);

        // Badge tên phòng
        String roomLabel = room != null ? "Phòng " + room.getId() : "Chưa có phòng";
        JLabel lblRoom = UITheme.badge(roomLabel, UITheme.PRIMARY_LIGHT, UITheme.PRIMARY);

        p.add(left,     BorderLayout.WEST);
        p.add(lblRoom,  BorderLayout.EAST);
        return p;
    }

    // ── Nội dung chính ────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(UITheme.BG_LIGHT);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        if (room == null) {
            content.add(buildNoRoomCard());
        } else {
            content.add(buildRoomInfoRow());
            content.add(Box.createVerticalStrut(16));
            content.add(buildRoommatesCard());
            content.add(Box.createVerticalStrut(16));
            content.add(buildRulesCard());
        }
        return content;
    }

    // ── Trường hợp chưa có phòng ─────────────────────────────────
    private JPanel buildNoRoomCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel icon = new JLabel("🏠", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel msg = new JLabel("Bạn chưa được phân phòng. Vui lòng liên hệ Ban quản lý.",
            SwingConstants.CENTER);
        msg.setFont(UITheme.FONT_BODY);
        msg.setForeground(UITheme.TEXT_SECONDARY);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 10));
        center.setOpaque(false);
        center.add(icon);
        center.add(msg);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    // ── Hàng thông tin phòng ─────────────────────────────────────
    private JPanel buildRoomInfoRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card 1: Thông tin phòng
        row.add(infoCard("🛏 Phòng",
            room.getId(),
            room.getType(),
            UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));

        // Card 2: Sức chứa
        int cap  = room.getCapacity();
        int curr = room.getOccupied();
        row.add(infoCard("👥 Sức chứa",
            curr + " / " + cap + " người",
            cap - curr > 0 ? "Còn " + (cap-curr) + " chỗ trống" : "Đã đầy",
            curr == cap ? UITheme.DANGER : UITheme.SUCCESS_TEXT,
            curr == cap ? UITheme.DANGER_BG : UITheme.SUCCESS_BG));

        // Card 3: Tầng
        int floor = room.getFloor();
        row.add(infoCard("🏢 Vị trí",
            "Tầng " + floor,
            "Tòa " + room.getId().charAt(0),
            UITheme.INFO_TEXT, UITheme.INFO_BG));

        return row;
    }

    /** Card thông tin nhỏ */
    private JPanel infoCard(String title, String value, String sub,
                             Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_H2);
        lblValue.setForeground(accent);

        JLabel lblSub = UITheme.badge(sub, bg, accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub,   BorderLayout.SOUTH);
        return card;
    }

    // ── Card danh sách bạn cùng phòng ────────────────────────────
    private JPanel buildRoommatesCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        JLabel title = new JLabel("👥  Danh sách bạn cùng phòng");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Tìm tất cả SV cùng phòng
        List<Student> roommates = 	DatabaseService.getAllStudents().stream()
            .filter(s -> room.getId().equals(s.getRoomId()))
            .collect(Collectors.toList());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        if (roommates.isEmpty()) {
            JLabel empty = new JLabel("Không có dữ liệu bạn cùng phòng.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (int i = 0; i < roommates.size(); i++) {
                Student s = roommates.get(i);
                boolean isMe = s.getId().equals(
                    student != null ? student.getId() : "");
                listPanel.add(roommateRow(s, isMe, i + 1));
                if (i < roommates.size() - 1)
                    listPanel.add(Box.createVerticalStrut(8));
            }
        }

        card.add(title,     BorderLayout.NORTH);
        card.add(listPanel, BorderLayout.CENTER);
        return card;
    }

    /** Một hàng bạn cùng phòng */
    private JPanel roommateRow(Student s, boolean isMe, int index) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setBackground(isMe ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
        row.setBorder(new CompoundBorder(
            new LineBorder(isMe ? UITheme.PRIMARY : UITheme.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        // Mini avatar
        JPanel avt = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMe ? UITheme.PRIMARY : UITheme.BG_SECONDARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(isMe ? Color.WHITE : UITheme.TEXT_SECONDARY);
                g2.setFont(UITheme.FONT_BOLD);
                String init = initials(s.getFullName());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init,
                    (getWidth()-fm.stringWidth(init))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(38, 38); }
        };
        avt.setOpaque(false);

        // Tên + khoa
        JLabel lblName = new JLabel(s.getFullName() + (isMe ? "  (Bạn)" : ""));
        lblName.setFont(isMe ? UITheme.FONT_BOLD : UITheme.FONT_BODY);
        lblName.setForeground(isMe ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);

        JLabel lblInfo = new JLabel(s.getFaculty() + " — " + s.getClassName());
        lblInfo.setFont(UITheme.FONT_TINY);
        lblInfo.setForeground(UITheme.TEXT_MUTED);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);
        center.add(lblName);
        center.add(lblInfo);

        // Mã SV bên phải
        JLabel lblId = new JLabel(s.getId());
        lblId.setFont(UITheme.FONT_TINY);
        lblId.setForeground(UITheme.TEXT_MUTED);
        lblId.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(avt,    BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(lblId,  BorderLayout.EAST);
        return row;
    }

    // ── Card nội quy phòng ────────────────────────────────────────
    private JPanel buildRulesCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        JLabel title = new JLabel("📋  Nội quy phòng & Ký túc xá");
        title.setFont(UITheme.FONT_LABEL);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Danh sách nội quy
        String[][] rules = {
            {"🕐", "Giờ giới nghiêm", "Sinh viên phải có mặt tại KTX trước 23:00 hàng ngày."},
            {"🚭", "Cấm hút thuốc",   "Tuyệt đối không hút thuốc trong phòng và khuôn viên KTX."},
            {"🔇", "Giữ trật tự",     "Không gây tiếng ồn sau 22:00 và trước 06:00."},
            {"🧹", "Vệ sinh",         "Tự dọn dẹp phòng và tham gia vệ sinh chung theo lịch phân công."},
            {"👥", "Khách thăm",      "Khách thăm phải đăng ký tại BQL và ra về trước 20:00."},
            {"⚡", "Điện",            "Không sử dụng thiết bị điện công suất lớn (bếp điện, lò vi sóng...)."},
            {"🔒", "Tài sản",         "Tự bảo quản tài sản cá nhân, khóa cửa khi ra ngoài."},
        };

        JPanel rulesPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        rulesPanel.setOpaque(false);

        for (String[] rule : rules) {
            rulesPanel.add(ruleRow(rule[0], rule[1], rule[2]));
        }

        // Ghi chú vi phạm
        JLabel warn = new JLabel(
            "⚠  Vi phạm nội quy sẽ bị xử lý theo quy định của Ban quản lý ký túc xá.");
        warn.setFont(UITheme.FONT_TINY);
        warn.setForeground(UITheme.DANGER);
        warn.setBorder(new CompoundBorder(
            new LineBorder(UITheme.DANGER_BG, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        warn.setOpaque(true);
        warn.setBackground(UITheme.DANGER_BG);

        card.add(title,      BorderLayout.NORTH);
        card.add(rulesPanel, BorderLayout.CENTER);
        card.add(warn,       BorderLayout.SOUTH);
        return card;
    }

    /** Một hàng nội quy */
    private JPanel ruleRow(String icon, String name, String desc) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setPreferredSize(new Dimension(28, 28));

        JLabel lblName = new JLabel("<html><b>" + name + ":</b> " + desc + "</html>");
        lblName.setFont(UITheme.FONT_BODY);
        lblName.setForeground(UITheme.TEXT_PRIMARY);

        p.add(lblIcon, BorderLayout.WEST);
        p.add(lblName, BorderLayout.CENTER);
        return p;
    }

    // ── Tiện ích ─────────────────────────────────────────────────
    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length-1].substring(0, 1)).toUpperCase();
    }
}
