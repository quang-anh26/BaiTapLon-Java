package com.sdms.ui.admin;

import com.sdms.model.Invoice;
import com.sdms.utils.DataStore;
import com.sdms.utils.DatabaseService;
import com.sdms.utils.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class PaymentPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    private static final String[] COLS = {"Mã HĐ","Sinh viên","Phòng","Tháng",
                                          "Tiền phòng","Tiền điện","Tiền nước","Tổng tiền","Trạng thái"};

    public PaymentPanel() {
        setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,UITheme.BORDER), new EmptyBorder(12,20,12,20)));
        JLabel title = new JLabel("💳  Quản lý thanh toán");
        title.setFont(UITheme.FONT_H2); title.setForeground(UITheme.TEXT_PRIMARY);
        p.add(title, BorderLayout.WEST);
        return p;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0,10));
        body.setBackground(UITheme.BG_LIGHT);
        body.setBorder(new EmptyBorder(14,16,14,16));

        // Summary cards
        JPanel cards = new JPanel(new GridLayout(1,3,12,0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        long totalRev = DatabaseService.getAllInvoices().stream().filter(Invoice::isPaid).mapToLong(Invoice::getTotal).sum();
        long pending  = DatabaseService.getAllInvoices().stream().filter(i -> !i.isPaid()).mapToLong(Invoice::getTotal).sum();
        long count    = DatabaseService.getAllInvoices().stream().filter(i -> !i.isPaid()).count();

        cards.add(summaryCard("💰", "Đã thu tháng này",    String.format("%,d đ", totalRev), UITheme.SUCCESS));
        cards.add(summaryCard("⏳", "Chưa thanh toán",     String.format("%,d đ", pending),  UITheme.WARNING));
        cards.add(summaryCard("📋", "Hóa đơn chờ xử lý",   count + " hóa đơn",              UITheme.DANGER));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);
        JTextField tfSearch = UITheme.textField("🔍  Tìm theo tên sinh viên, phòng...");
        tfSearch.setPreferredSize(new Dimension(260,36));
        JComboBox<String> cbMonth = UITheme.comboBox(new String[]{"06/2026","05/2026","04/2026","03/2026"});
        cbMonth.setPreferredSize(new Dimension(110,36));
        JComboBox<String> cbStatus = UITheme.comboBox(new String[]{"Tất cả","Đã thanh toán","Chưa thanh toán"});
        cbStatus.setPreferredSize(new Dimension(160,36));
        JButton btnExcel = UITheme.successBtn("📊 Xuất Excel");
        toolbar.add(tfSearch); toolbar.add(cbMonth); toolbar.add(cbStatus); toolbar.add(btnExcel);

        // Table
        model = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshTable();

        table = new JTable(model);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UITheme.BORDER);
        table.setBackground(UITheme.WHITE);
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setRowHeight(38);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setBackground(UITheme.BG_SECONDARY);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);

        // Status renderer
        table.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, focus, row, col) -> {
            boolean paid = "Đã thanh toán".equals(v.toString());
            JLabel lbl = UITheme.badge(v.toString(), paid ? UITheme.SUCCESS_BG : UITheme.WARNING_BG,
                                       paid ? UITheme.SUCCESS_TEXT : UITheme.WARNING_TEXT);
            lbl.setOpaque(true); lbl.setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.WHITE);
            return lbl;
        });

        // Action on row double-click -> mark paid
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    String id = (String) model.getValueAt(row, 0);
                    Invoice inv = DatabaseService.getAllInvoices().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
                    if (inv != null && !inv.isPaid()) {
                        int r = JOptionPane.showConfirmDialog(PaymentPanel.this,
                            "<html>Xác nhận thanh toán hóa đơn <b>"+id+"</b>?<br>Tổng tiền: <b>"+String.format("%,d đ", inv.getTotal())+"</b></html>",
                            "Thanh toán", JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) {
                            inv.setPaid(true);
                            refreshTable();
                            JOptionPane.showMessageDialog(PaymentPanel.this, "✅ Thanh toán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(UITheme.WHITE);

        JLabel hint = new JLabel("💡 Double-click vào hóa đơn chưa thanh toán để xác nhận thanh toán");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(UITheme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout(0,10));
        top.setOpaque(false);
        top.add(cards, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        body.add(top, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        body.add(hint, BorderLayout.SOUTH);
        return body;
    }

    private JPanel summaryCard(String icon, String label, String value, Color color) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(10,0));
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI",Font.PLAIN,28));
        JPanel info = new JPanel(new GridLayout(2,1));
        info.setOpaque(false);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI",Font.BOLD,18)); val.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        info.add(val); info.add(lbl);
        card.add(ico, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Invoice inv : DatabaseService.getAllInvoices()) model.addRow(inv.toRow());
    }
}
