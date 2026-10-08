package quizplatform.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Shared theme + tiny widget helpers for all screens. */
public final class Ui {

    public static final Color BG = new Color(0xF4, 0xF6, 0xFB);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    public static final Color PRIMARY_DARK = new Color(0x1E, 0x40, 0xAF);
    public static final Color DARK = new Color(0x11, 0x18, 0x27);
    public static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    public static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);
    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);
    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);
    public static final Color LINE = new Color(0xE5, 0xE7, 0xEB);
    public static final Color NAV_BG = new Color(0x11, 0x18, 0x27);
    public static final Color NAV_SEL = new Color(0x25, 0x63, 0xEB);

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private Ui() {}

    // --------------------------------------------------------------- widgets
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(H1);
        l.setForeground(DARK);
        return l;
    }

    public static JLabel subheading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(BODY);
        c.setBackground(CARD);
        return c;
    }

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primary(String text) { return button(text, PRIMARY); }
    public static JButton danger(String text) { return button(text, DANGER); }
    public static JButton success(String text) { return button(text, SUCCESS); }
    public static JButton neutral(String text) { return button(text, new Color(0x4B, 0x55, 0x63)); }

    /** White rounded-ish card container with padding. */
    public static JPanel card(JComponent content) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 16, 16, 16)));
        p.add(content);
        return p;
    }

    public static JPanel card(String title, JComponent content) {
        JPanel wrap = new JPanel(new java.awt.BorderLayout(0, 10));
        wrap.setOpaque(false);
        if (title != null && !title.isEmpty()) {
            JLabel t = new JLabel(title);
            t.setFont(H2);
            t.setForeground(DARK);
            wrap.add(t, java.awt.BorderLayout.NORTH);
        }
        wrap.add(card(content), java.awt.BorderLayout.CENTER);
        return wrap;
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    // ----------------------------------------------------------------- table
    public static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(BODY);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        t.setSelectionForeground(TEXT);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().setColumnSelectionAllowed(false);
        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(new Color(0xF3, 0xF4, 0xF6));
        h.setForeground(TEXT);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        t.setDefaultRenderer(Object.class, center);
        return t;
    }

    public static Object[] row(Object... cells) { return cells; }

    // --------------------------------------------------------------- dialogs
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void success(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String prompt(Component parent, String title, String initial) {
        Object v = JOptionPane.showInputDialog(parent, title, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initial);
        return v == null ? null : String.valueOf(v);
    }

    // ----------------------------------------------------------------- misc
    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    private static final SimpleDateFormat FMT_SHORT = new SimpleDateFormat("dd MMM yyyy");

    public static String date(long millis) {
        return millis <= 0 ? "-" : FMT.format(new Date(millis));
    }

    public static String dateShort(long millis) {
        return millis <= 0 ? "-" : FMT_SHORT.format(new Date(millis));
    }

    public static String pct(double v) {
        return String.format("%.1f%%", v);
    }

    public static Color statusColor(String status) {
        return switch (status) {
            case "Approved", "SUCCESS" -> SUCCESS;
            case "Rejected", "WARNING" -> DANGER;
            case "Pending Approval" -> WARNING;
            default -> MUTED;
        };
    }

    public static JPanel page(String title, String subtitle, JComponent body) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(22, 26, 22, 26));
        JPanel head = new JPanel(new java.awt.BorderLayout());
        head.setOpaque(false);
        head.add(heading(title), java.awt.BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            head.add(new JLabel(" "), java.awt.BorderLayout.CENTER);
            head.add(muted(subtitle), java.awt.BorderLayout.SOUTH);
        }
        p.add(head, java.awt.BorderLayout.NORTH);
        p.add(body, java.awt.BorderLayout.CENTER);
        return p;
    }
}
