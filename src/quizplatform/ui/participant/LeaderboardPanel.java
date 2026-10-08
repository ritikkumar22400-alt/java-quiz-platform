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
                        c.setBackground(new Color(0xDB, 0xEA, 0xFE));
                        setForeground(Ui.DARK);
                        setFont(Ui.BOLD);
                    } else if (row < 3) {
                        c.setBackground(new Color(0xFE, 0xF3, 0xC7));
                        setForeground(Ui.TEXT);
                        setFont(Ui.BODY);
                    } else {
                        c.setBackground(Color.WHITE);
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
        center.add(Ui.scroll(table), BorderLayout.CENTER);
        center.add(note, BorderLayout.SOUTH);
        body.add(center, BorderLayout.CENTER);
        return body;
    }

    public void refresh() {
        if (!ctx.repo.settings.leaderboardEnabled) {
            model.setRowCount(0);
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
        note.setText("Ranked by best score, then average score. Your row is highlighted in blue.");
    }
}
