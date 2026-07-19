package com.sdms.utils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UITheme {
    // Colors
    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color PRIMARY_DARK = new Color(0x1D4ED8);
    public static final Color PRIMARY_LIGHT = new Color(0xEFF6FF);
    public static final Color SIDEBAR_BG = new Color(0x1E293B);
    public static final Color SIDEBAR_DARK = new Color(0x0F172A);
    public static final Color WHITE = Color.WHITE;
    public static final Color BG_LIGHT = new Color(0xF8FAFC);
    public static final Color BG_SECONDARY = new Color(0xF1F5F9);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT_PRIMARY = new Color(0x0F172A);
    public static final Color TEXT_SECONDARY = new Color(0x64748B);
    public static final Color TEXT_MUTED = new Color(0x94A3B8);
    public static final Color SUCCESS = new Color(0x22C55E);
    public static final Color SUCCESS_BG = new Color(0xDCFCE7);
    public static final Color SUCCESS_TEXT = new Color(0x15803D);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color WARNING_BG = new Color(0xFEF9C3);
    public static final Color WARNING_TEXT = new Color(0x854D0E);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color DANGER_BG = new Color(0xFEE2E2);
    public static final Color DANGER_TEXT = new Color(0xB91C1C);
    public static final Color INFO_BG = new Color(0xDBEAFE);
    public static final Color INFO_TEXT = new Color(0x1D4ED8);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color PURPLE_BG = new Color(0xEDE9FE);
    public static final Color YELLOW_BG = new Color(0xFEF9C3);
    public static final Color YELLOW_TEXT = new Color(0x854D0E);

    // Fonts
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    public static final Font FONT_H2 = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONT_H3 = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_TINY = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    public static final int SIDEBAR_WIDTH = 220;
    public static final int HEADER_HEIGHT = 58;
    public static final int INPUT_HEIGHT = 38;
    public static final int BTN_HEIGHT = 36;
    public static final int RADIUS = 10;

    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", PRIMARY_DARK);
        UIManager.put("TableHeader.font", FONT_LABEL);
        UIManager.put("TableHeader.background", BG_SECONDARY);
        UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumb", new Color(0xCBD5E1));
        UIManager.put("ScrollBar.track", BG_LIGHT);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_BOLD);
    }

    // ── Rounded Panel ─────────────────────────────────────────────
    public static JPanel roundedPanel(Color bg, int radius) {
        return new JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
            }
        };
    }

    public static JPanel card() {
        JPanel p = new JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 5 * i));
                    g2.fill(new RoundRectangle2D.Float(i, i, getWidth() - i * 2, getHeight() - i * 2, 12, 12));
                }
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 12, 12));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(0.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 12, 12));
                g2.dispose();
            }
        };
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        return p;
    }

    // ── Buttons ───────────────────────────────────────────────────
    public static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.darker() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(fg);
        b.setFont(FONT_BOLD);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, BTN_HEIGHT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primaryBtn(String text) {
        return btn(text, PRIMARY, WHITE);
    }

    public static JButton dangerBtn(String text) {
        return btn(text, DANGER, WHITE);
    }

    public static JButton warningBtn(String text) {
        return btn(text, WARNING, WHITE);
    }

    public static JButton successBtn(String text) {
        return btn(text, SUCCESS, WHITE);
    }

    public static JButton purpleBtn(String text) {
        return btn(text, PURPLE, WHITE);
    }

    public static JButton outlineBtn(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_SECONDARY : WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
                g2.setColor(getModel().isRollover() ? PRIMARY : BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, RADIUS, RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(TEXT_SECONDARY);
        b.setFont(FONT_BOLD);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, BTN_HEIGHT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Inputs ────────────────────────────────────────────────────
    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getClientProperty("hover") == Boolean.TRUE;
                g2.setColor(isFocusOwner() ? WHITE : hover ? WHITE : BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
                g2.setColor(isFocusOwner() ? PRIMARY : hover ? new Color(0x94A3B8) : BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, RADIUS, RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_PRIMARY);
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(0, 12, 0, 12));
        tf.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        tf.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                tf.putClientProperty("hover", Boolean.TRUE);
                tf.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                tf.putClientProperty("hover", null);
                tf.repaint();
            }
        });
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.repaint();
            }
        });
        return tf;
    }

    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getClientProperty("hover") == Boolean.TRUE;
                g2.setColor(isFocusOwner() ? WHITE : hover ? WHITE : BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
                g2.setColor(isFocusOwner() ? PRIMARY : hover ? new Color(0x94A3B8) : BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, RADIUS, RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };
        pf.setFont(FONT_BODY);
        pf.setForeground(TEXT_PRIMARY);
        pf.setOpaque(false);
        pf.setBorder(new EmptyBorder(0, 12, 0, 12));
        pf.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        pf.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                pf.putClientProperty("hover", Boolean.TRUE);
                pf.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                pf.putClientProperty("hover", null);
                pf.repaint();
            }
        });
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.repaint();
            }
        });
        return pf;
    }

    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_SECONDARY);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(0, 4, 0, 4)));
        cb.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        return cb;
    }

    // ── Badge ─────────────────────────────────────────────────────
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_TINY);
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BORDER);
        return sep;
    }
}
