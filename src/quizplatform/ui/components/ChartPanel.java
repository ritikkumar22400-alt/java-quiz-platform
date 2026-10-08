package quizplatform.ui.components;

import quizplatform.service.ReportService.Stat;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.List;

/** Dependency-free charts (bar / pie / line) drawn with Graphics2D. */
public class ChartPanel extends JPanel {

    public enum Type { BAR, PIE, LINE }

    private static final Color[] PALETTE = {
            new Color(0x25, 0x63, 0xEB), new Color(0x16, 0xA3, 0x4A),
            new Color(0xD9, 0x77, 0x06), new Color(0xDC, 0x26, 0x26),
            new Color(0x7C, 0x3A, 0xED), new Color(0x08, 0x91, 0xB2),
            new Color(0xDB, 0x27, 0x77), new Color(0x4B, 0x55, 0x63)
    };

    private final Type type;
    private List<Stat> data;
    private String title;

    public ChartPanel(Type type, String title, List<Stat> data) {
        this.type = type;
        this.title = title;
        this.data = data;
        setBackground(Color.WHITE);
        setPreferredSize(new java.awt.Dimension(560, 300));
    }

    public void setData(List<Stat> data, String title) {
        this.data = data;
        this.title = title;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(0x1F, 0x29, 0x37));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString(title == null ? "" : title, 12, 22);

        if (data == null || data.isEmpty()) {
            g2.setColor(new Color(0x9C, 0xA3, 0xAF));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("No data available yet.", 12, 48);
            g2.dispose();
            return;
        }

        switch (type) {
            case BAR -> drawBar(g2, w, h);
            case PIE -> drawPie(g2, w, h);
            case LINE -> drawLine(g2, w, h);
        }
        g2.dispose();
    }

    // ------------------------------------------------------------------ bar
    private void drawBar(Graphics2D g2, int w, int h) {
        int left = 50, right = 18, top = 44, bottom = 58;
        int pw = w - left - right, ph = h - top - bottom;
        if (pw <= 20 || ph <= 20) return;

        double max = 1;
        for (Stat s : data) max = Math.max(max, s.value);

        g2.setColor(new Color(0xE5, 0xE7, 0xEB));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            int y = top + ph - (int) (ph * i / 4.0);
            g2.drawLine(left, y, left + pw, y);
            g2.setColor(new Color(0x9C, 0xA3, 0xAF));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.valueOf((int) Math.round(max * i / 4.0)), 8, y + 4);
            g2.setColor(new Color(0xE5, 0xE7, 0xEB));
        }

        int n = data.size();
        int slot = Math.max(24, pw / Math.max(1, n));
        int barW = Math.min(56, slot - 12);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < n; i++) {
            Stat s = data.get(i);
            int bh = (int) (ph * (s.value / max));
            int x = left + i * slot + (slot - barW) / 2;
            int y = top + ph - bh;
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillRoundRect(x, y, barW, Math.max(bh, 2), 6, 6);

            g2.setColor(new Color(0x37, 0x41, 0x51));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String vs = (max <= 100 && s.value == Math.round(s.value))
                    ? String.valueOf((int) s.value) : String.format("%.1f", s.value);
            g2.drawString(vs, x + (barW - fm.stringWidth(vs)) / 2, y - 5);

            g2.setColor(new Color(0x6B, 0x72, 0x80));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            drawRotatedLabel(g2, s.label, x + barW / 2, top + ph + 12);
        }
    }

    private void drawRotatedLabel(Graphics2D g2, String text, int x, int y) {
        String t = text.length() > 16 ? text.substring(0, 15) + ".." : text;
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(t);
        if (tw <= 0) return;
        var old = g2.getTransform();
        double angle = Math.toRadians(35);
        g2.rotate(angle, x, y);
        if (x - tw > 4) {
            g2.drawString(t, x - tw, y + 10);
        } else {
            g2.drawString(t, x, y + 10);
        }
        g2.setTransform(old);
    }

    // ------------------------------------------------------------------ pie
    private void drawPie(Graphics2D g2, int w, int h) {
        int top = 44;
        double total = 0;
        for (Stat s : data) total += Math.max(0, s.value);
        int size = Math.min(h - top - 30, w / 2 - 40);
        if (size < 60) size = Math.max(60, h - top - 30);
        int cx = 40 + size / 2, cy = top + size / 2;

        double start = 90;
        FontMetrics fm = g2.getFontMetrics();
        if (total <= 0) {
            g2.setColor(new Color(0xE5, 0xE7, 0xEB));
            g2.fillOval(cx - size / 2, cy - size / 2, size, size);
        } else {
            for (int i = 0; i < data.size(); i++) {
                Stat s = data.get(i);
                double sweep = 360.0 * Math.max(0, s.value) / total;
                g2.setColor(PALETTE[i % PALETTE.length]);
                Arc2D arc = new Arc2D.Double(cx - size / 2, cy - size / 2, size, size,
                        -start, -sweep, Arc2D.PIE);
                g2.fill(arc);
                start += sweep;
            }
        }

        int lx = cx + size / 2 + 30;
        int ly = cy - data.size() * 14;
        for (int i = 0; i < data.size(); i++) {
            Stat s = data.get(i);
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillRoundRect(lx, ly - 9, 14, 14, 4, 4);
            g2.setColor(new Color(0x37, 0x41, 0x51));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            String txt = s.label + " (" + (int) s.value + ")";
            if (lx + 24 + fm.stringWidth(txt) < w - 8) {
                g2.drawString(txt, lx + 22, ly + 3);
            }
            ly += 28;
        }
    }

    // ----------------------------------------------------------------- line
    private void drawLine(Graphics2D g2, int w, int h) {
        int left = 50, right = 24, top = 44, bottom = 58;
        int pw = w - left - right, ph = h - top - bottom;
        if (pw <= 20 || ph <= 20) return;

        double max = 1;
        for (Stat s : data) max = Math.max(max, s.value);

        g2.setColor(new Color(0xE5, 0xE7, 0xEB));
        for (int i = 0; i <= 4; i++) {
            int y = top + ph - (int) (ph * i / 4.0);
            g2.drawLine(left, y, left + pw, y);
            g2.setColor(new Color(0x9C, 0xA3, 0xAF));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.valueOf((int) Math.round(max * i / 4.0)), 8, y + 4);
            g2.setColor(new Color(0xE5, 0xE7, 0xEB));
        }

        int n = data.size();
        double stepX = n == 1 ? 0 : (double) pw / (n - 1);
        Path2D path = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double x = left + (n == 1 ? pw / 2.0 : i * stepX);
            double y = top + ph - (ph * (data.get(i).value / max));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        g2.setColor(new Color(0x25, 0x63, 0xEB));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i < n; i++) {
            double x = left + (n == 1 ? pw / 2.0 : i * stepX);
            double y = top + ph - (ph * (data.get(i).value / max));
            g2.setColor(Color.WHITE);
            g2.fillOval((int) x - 5, (int) y - 5, 10, 10);
            g2.setColor(new Color(0x25, 0x63, 0xEB));
            g2.fillOval((int) x - 3, (int) y - 3, 6, 6);
            g2.setColor(new Color(0x37, 0x41, 0x51));
            String lbl = data.get(i).label;
            String vs = String.valueOf((int) Math.round(data.get(i).value));
            g2.drawString(vs, (int) x - g2.getFontMetrics().stringWidth(vs) / 2, (int) y - 9);
            g2.setColor(new Color(0x6B, 0x72, 0x80));
            drawRotatedLabel(g2, lbl, (int) x, top + ph + 12);
        }
    }
}
