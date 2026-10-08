package quizplatform.ui.participant;

import quizplatform.service.AppContext;
import quizplatform.service.LeaderboardService.Row;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

public class LeaderboardPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Rank", "Participant", "Email", "Quizzes Taken", "Average %", "Best %");
    private final JTable table = Ui.table(model);
    private final JPanel podium = new JPanel(new java.awt.GridLayout(1, 3, 14, 0));
    private final JLabel note = new JLabel();
    private List<Row> rows = List.of();

    public LeaderboardPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Leaderboard",
                "Overall rankings of participants by best quiz performance.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        note.setFont(Ui.SMALL);
        note.setForeground(Ui.MUTED);
        podium.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JButton refreshBtn = Ui.neutral("Refresh");
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(refreshBtn);
        toolbar.add(left, BorderLayout.WEST);
        refreshBtn.addActionListener(e -> refresh());

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                                    boolean focus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && row < rows.size()) {
                    Row r = rows.get(row);
                    if (r.isCurrentUser) {
                        c.setBackground(new Color(0xE0, 0xE7, 0xFF));
                        setForeground(Ui.PRIMARY_DARK);
                        setFont(Ui.BOLD);
                    } else if (row < 3) {
                        c.setBackground(new Color(0xFE, 0xF3, 0xC7));
                        setForeground(Ui.TEXT);
                        setFont(Ui.BODY);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFD));
                        setForeground(Ui.TEXT);
                        setFont(Ui.BODY);
                    }
                }
                return c;
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(podium, BorderLayout.NORTH);
        center.add(Ui.scroll(table), BorderLayout.CENTER);
        center.add(note, BorderLayout.SOUTH);
        body.add(center, BorderLayout.CENTER);
        return body;
    }

    private JPanel podiumCard(String rank, String name, String stat, java.awt.Color top, java.awt.Color bottom) {
        Ui.GradientPanel p = new Ui.GradientPanel(top, bottom);
        p.setLayout(new java.awt.BorderLayout(0, 4));
        p.setBorder(new javax.swing.border.EmptyBorder(16, 18, 16, 18));
        p.setPreferredSize(new java.awt.Dimension(0, 92));

        JPanel center = new JPanel();
        center.setOpaque(false);
        JLabel nameL = new JLabel(name);
        nameL.setFont(Ui.BOLD);
        nameL.setForeground(Color.WHITE);
        nameL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        JLabel statL = new JLabel(stat);
        statL.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        statL.setForeground(new Color(0xFF, 0xED, 0xD2));
        statL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        center.setLayout(new java.awt.BorderLayout(0, 2));
        center.add(nameL, java.awt.BorderLayout.CENTER);
        center.add(statL, java.awt.BorderLayout.SOUTH);

        JLabel chip = Ui.pill(rank, Color.WHITE, new Color(0, 0, 0, 40));
        JPanel west = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        west.setOpaque(false);
        west.add(chip);

        p.add(west, java.awt.BorderLayout.WEST);
        p.add(center, java.awt.BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        if (!ctx.repo.settings.leaderboardEnabled) {
            model.setRowCount(0);
            podium.removeAll();
            podium.repaint();
            note.setText("The leaderboard has been disabled by the administrator.");
            Ui.info(this, "The leaderboard is currently disabled in system settings.");
            return;
        }
        rows = ctx.leaderboard.ranked(ctx.auth.currentUser().id);
        model.setRowCount(0);
        for (Row r : rows) {
            model.addRow(Ui.row(r.rank, r.name + (r.isCurrentUser ? " (you)" : ""), r.email,
                    r.quizzesTaken, Ui.pct(r.avgPct), Ui.pct(r.bestPct)));
        }
        podium.removeAll();
        String[][] medals = {{"1st", "#F59E0B", "#B45309"}, {"2nd", "#94A3B8", "#475569"},
                {"3rd", "#D97706", "#92400E"}};
        for (int i = 0; i < 3 && i < rows.size(); i++) {
            Row r = rows.get(i);
            podium.add(podiumCard(medals[i][0], r.name, "Best " + Ui.pct(r.bestPct) + "  •  " + r.quizzesTaken + " quiz(zes)",
                    new Color(Integer.parseInt(medals[i][1].substring(1), 16)),
                    new Color(Integer.parseInt(medals[i][2].substring(1), 16))));
        }
        podium.revalidate();
        podium.repaint();
        note.setText("Ranked by best score, then average score. Top 3 are shown on the podium; your row is highlighted in blue.");
    }
}
