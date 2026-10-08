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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Shared theme + tiny widget helpers for all screens. */
public final class Ui {

    // ------------------------------------------------------------- palette
    public static final Color BG = new Color(0xEC, 0xEF, 0xF6);          // soft slate content background
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(0x4F, 0x46, 0xE5);     // indigo-600
    public static final Color PRIMARY_DARK = new Color(0x37, 0x2F, 0xC9);
    public static final Color PRIMARY_LIGHT = new Color(0xDD, 0xD6, 0xFE);
    public static final Color DARK = new Color(0x1E, 0x29, 0x3B);
    public static final Color TEXT = new Color(0x33, 0x41, 0x55);
    public static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    public static final Color SUCCESS = new Color(0x05, 0x9D, 0x69);
    public static final Color SUCCESS_LIGHT = new Color(0xD1, 0xFA, 0xE5);
    public static final Color DANGER = new Color(0xE1, 0x1D, 0x48);
    public static final Color DANGER_LIGHT = new Color(0xFF, 0xE4, 0xE6);
    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);
    public static final Color WARNING_LIGHT = new Color(0xFE, 0xF3, 0xC7);
    public static final Color SKY = new Color(0x02, 0x84, 0xC7);
    public static final Color SKY_LIGHT = new Color(0xE0, 0xF2, 0xFE);
    public static final Color LINE = new Color(0xE2, 0xE8, 0xF0);
    public static final Color HEADER_BG = new Color(0xF8, 0xFA, 0xFC);

    // Sidebar (dark indigo)
    public static final Color NAV_BG = new Color(0x18, 0x1E, 0x3A);
    public static final Color NAV_BG_LIGHT = new Color(0x24, 0x2B, 0x52);
    public static final Color NAV_SEL = new Color(0x4F, 0x46, 0xE5);
    public static final Color NAV_TEXT = new Color(0xA5, 0xB0, 0xC7);
    public static final Color NAV_TEXT_ACTIVE = Color.WHITE;

    // Stat-card accents
    public static final Color ACCENT_INDIGO = new Color(0x4F, 0x46, 0xE5);
    public static final Color ACCENT_EMERALD = new Color(0x05, 0x9D, 0x69);
    public static final Color ACCENT_AMBER = new Color(0xB4, 0x53, 0x09);
    public static final Color ACCENT_ROSE = new Color(0xBE, 0x12, 0x3C);
    public static final Color ACCENT_SKY = new Color(0x02, 0x68, 0xA8);

    public static final Color[] ACCENTS = {ACCENT_INDIGO, ACCENT_EMERALD, ACCENT_AMBER, ACCENT_ROSE, ACCENT_SKY};

    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private Ui() {}

    // ------------------------------------------------------------- helpers
    public static Color tint(Color c, float alpha) {
        return new Color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alpha);
    }

    public static Color darker(Color c) {
        return c.darker().darker();
    }

    public static Color lighter(Color c) {
        float f = 1.25f;
        return new Color(Math.min(255, (int) (c.getRed() * f)),
                Math.min(255, (int) (c.getGreen() * f)),
                Math.min(255, (int) (c.getBlue() * f)));
    }

    // ------------------------------------------------------------ text
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

    /** Colored pill badge for statuses. */
    public static JLabel pill(String text, Color fg, Color bg) {
        JLabel l = new JLabel(" " + text + " ");
        l.setFont(BOLD);
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // ------------------------------------------------------------- fields
    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC7, 0xD2, 0xE0)),
                new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(BODY);
        c.setBackground(CARD);
        return c;
    }

    // ------------------------------------------------------------- buttons
    public static JButton button(String text, Color base) {
        return new RoundedButton(text, base);
    }

    public static JButton primary(String text) { return button(text, PRIMARY); }
    public static JButton danger(String text) { return button(text, DANGER); }
    public static JButton success(String text) { return button(text, SUCCESS); }
    public static JButton neutral(String text) { return button(text, new Color(0x64, 0x74, 0x8B)); }

    /** Flat rounded button with gradient + hover feedback. */
    public static class RoundedButton extends JButton {
        private final Color base;

        public RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setFont(BOLD);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color top = getModel().isRollover() ? lighter(base) : base;
            Color bottom = getModel().isPressed() ? darker(base) : darker(top);
            g2.setPaint(new LinearGradientPaint(0, 0, 0, h,
                    new float[]{0f, 1f}, new Color[]{top, bottom}));
            g2.fillRoundRect(0, 0, w, h, 12, 12);
            if (!isEnabled()) {
                g2.setColor(tint(Color.WHITE, 0.45f));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // -------------------------------------------------------------- cards
    /** White card with a soft drop shadow. */
    public static JPanel card(JComponent content) {
        return new ShadowCard(content);
    }

    public static JPanel card(String title, JComponent content) {
        JPanel wrap = new JPanel(new java.awt.BorderLayout(0, 12));
        wrap.setOpaque(false);
        if (title != null && !title.isEmpty()) {
            JPanel titleRow = new JPanel(new java.awt.BorderLayout(8, 0));
            titleRow.setOpaque(false);
            JPanel accent = new JPanel();
            accent.setBackground(PRIMARY);
            accent.setPreferredSize(new Dimension(4, 20));
            titleRow.add(accent, java.awt.BorderLayout.WEST);
            JLabel t = new JLabel(title);
            t.setFont(H2);
            t.setForeground(DARK);
            titleRow.add(t, java.awt.BorderLayout.CENTER);
            wrap.add(titleRow, java.awt.BorderLayout.NORTH);
        }
        wrap.add(card(content), java.awt.BorderLayout.CENTER);
        return wrap;
    }

    /** Card with a drop shadow drawn underneath. */
    public static class ShadowCard extends JPanel {
        public ShadowCard(JComponent content) {
            setLayout(new java.awt.BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(1, 1, 6, 6));
            add(content);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 4, h = getHeight() - 4;
            for (int i = 3; i >= 1; i--) {
                g2.setColor(tint(new Color(0x1E, 0x29, 0x3B), 0.05f + 0.02f * (3 - i)));
                g2.fillRoundRect(i * 2, i * 2 + 3, w, h, 16, 16);
            }
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, w, h, 16, 16);
            g2.setColor(new Color(0xEE, 0xF1, 0xF8));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Compact KPI tile: colored top bar + label + big value. */
    public static JPanel statCard(String label, String value, Color accent) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 4));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 16, 16)));

        JPanel top = new JPanel(new java.awt.BorderLayout());
        top.setOpaque(false);
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(34, 4));
        top.add(bar, java.awt.BorderLayout.WEST);
        p.add(top, java.awt.BorderLayout.NORTH);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 24));
        v.setForeground(accent);
        p.add(v, java.awt.BorderLayout.CENTER);

        JLabel l = new JLabel(label);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        p.add(l, java.awt.BorderLayout.SOUTH);
        return p;
    }

    /** Row of KPI tiles, evenly spread. */
    public static JPanel statRow(String[][] items) {
        JPanel row = new JPanel(new java.awt.GridLayout(1, items.length, 14, 0));
        row.setOpaque(false);
        for (int i = 0; i < items.length; i++) {
            row.add(statCard(items[i][0], items[i][1], ACCENTS[i % ACCENTS.length]));
        }
        return row;
    }

    /** Vertical gradient panel (diagonal). */
    public static JPanel gradientPanel(Color top, Color bottom) {
        return new GradientPanel(top, bottom);
    }

    public static class GradientPanel extends JPanel {
        private final Color top, bottom;

        public GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(),
                    new float[]{0f, 1f}, new Color[]{top, bottom}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    // --------------------------------------------------------------- table
    public static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(BODY);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(PrimaryPicker.LEADING_TINT);
        t.setSelectionForeground(TEXT);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().setColumnSelectionAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(new Color(0xF1, 0xF4, 0xF9));
        h.setForeground(new Color(0x47, 0x55, 0x69));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));

        t.setDefaultRenderer(Object.class, new JsonRenderer());
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
            TableColumn col = t.getColumnModel().getColumn(i);
            col.setCellRenderer(new JsonRenderer());
        }
        return t;
    }

    /** Tint used for table selection — shared with renderer. */
    static final class PrimaryPicker {
        static final Color LEADING_TINT = new Color(0xE0, 0xE7, 0xFF);
    }

    /** Zebra-striped cells with status-aware colored text. */
    static final class JsonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(BODY);
            setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                setBackground(PrimaryPicker.LEADING_TINT);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFD));
            }
            String v = value == null ? "" : String.valueOf(value);
            setForeground(statusColorFor(v, table, row));
            return c;
        }
    }

    /** Maps cell text to a readable color; column-insensitive keyword match. */
    static Color statusColorFor(String v, JTable table, int row) {
        String lower = v.toLowerCase();
        if (lower.contains("failed") || lower.contains("rejected")
                || lower.contains("disabled") || lower.contains("limit reached")
                || lower.contains("past") || lower.contains("unread")
                || lower.contains("rejected")) return DANGER;
        if (lower.contains("passed") || lower.contains("approved")
                || lower.contains("active") || lower.contains("available")
                || lower.contains("success")) return SUCCESS;
        if (lower.contains("pending approval") || lower.contains("upcoming")
                || lower.contains("warning")) return WARNING;
        if (lower.contains("draft") || lower.contains("read")) return MUTED;
        return TEXT;
    }

    public static Object[] row(Object... cells) { return cells; }

    // -------------------------------------------------------------- dialogs
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

    // --------------------------------------------------------------- misc
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

    /** Page scaffold with a richer, gradient-accented header band. */
    public static JPanel page(String title, String subtitle, JComponent body) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 18));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel head = new JPanel(new java.awt.BorderLayout(12, 0));
        head.setOpaque(false);

        JPanel titleBlock = new JPanel(new java.awt.BorderLayout(12, 2));
        titleBlock.setOpaque(false);
        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(6, 42));
        accent.setBackground(PRIMARY);
        titleBlock.add(accent, java.awt.BorderLayout.WEST);

        JPanel texts = new JPanel(new java.awt.BorderLayout(0, 3));
        texts.setOpaque(false);
        texts.add(heading(title), java.awt.BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            texts.add(muted(subtitle), java.awt.BorderLayout.SOUTH);
        }
        titleBlock.add(texts, java.awt.BorderLayout.CENTER);
        head.add(titleBlock, java.awt.BorderLayout.WEST);

        p.add(head, java.awt.BorderLayout.NORTH);
        p.add(body, java.awt.BorderLayout.CENTER);
        return p;
    }
}