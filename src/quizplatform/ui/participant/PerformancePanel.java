package quizplatform.ui.participant;

import quizplatform.service.AppContext;
import quizplatform.service.ReportService.ParticipantOverview;
import quizplatform.service.ReportService.ResultRow;
import quizplatform.service.ReportService.Stat;
import quizplatform.ui.Ui;
import quizplatform.ui.components.ChartPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class PerformancePanel extends JPanel {

    private final AppContext ctx;
    private final JPanel statsRow = new JPanel(new java.awt.BorderLayout());
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Quiz", "Score", "Percentage", "Graded", "Date", "Feedback");
    private final JTable table = Ui.table(model);
    private final ChartPanel trendChart = new ChartPanel(ChartPanel.Type.LINE, "Your score trend (%)", List.of());
    private final ChartPanel perQuizChart = new ChartPanel(ChartPanel.Type.BAR, "Average score per quiz (%)", List.of());

    public PerformancePanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Performance Report",
                "Detailed view of your quiz performance, trends and creator feedback.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        statsRow.setOpaque(false);

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 14));
        charts.setOpaque(false);
        charts.add(card(trendChart));
        charts.add(card(perQuizChart));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(statsRow, BorderLayout.NORTH);
        body.add(charts, BorderLayout.CENTER);
        body.add(Ui.card("Detailed results", Ui.scroll(table)), BorderLayout.SOUTH);
        return body;
    }

    private JPanel card(ChartPanel chart) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Ui.LINE),
                new javax.swing.border.EmptyBorder(10, 10, 10, 10)));
        p.add(chart, BorderLayout.CENTER);
        p.setPreferredSize(new java.awt.Dimension(500, 260));
        return p;
    }

    public void refresh() {
        String me = ctx.auth.currentUser().id;
        ParticipantOverview o = ctx.reports.overview(me);
        List<Stat> trend = ctx.reports.scoreTrend(me);
        List<Stat> perQuiz = averagePerQuiz(me);
        List<ResultRow> rows = ctx.reports.resultRows(me);

        trendChart.setData(trend, "Your score trend (%)");
        perQuizChart.setData(perQuiz, "Average score per quiz (%)");

        statsRow.removeAll();
        statsRow.add(Ui.statRow(new String[][]{
                {"Attempts", String.valueOf(o.attempts)},
                {"Quizzes taken", String.valueOf(o.quizzesTaken)},
                {"Average", Ui.pct(o.avgPct)},
                {"Best score", Ui.pct(o.bestPct)},
                {"Passed attempts", String.valueOf(o.passed)}
        }), java.awt.BorderLayout.CENTER);
        statsRow.revalidate();
        statsRow.repaint();

        model.setRowCount(0);
        for (ResultRow r : rows) {
            model.addRow(Ui.row(r.quizTitle, r.score + "/" + r.max, Ui.pct(r.pct),
                    r.graded ? "Yes" : "No", r.date,
                    r.feedback.isEmpty() ? "-" : r.feedback));
        }
        revalidate();
        repaint();
    }

    private List<Stat> averagePerQuiz(String userId) {
        java.util.Map<String, double[]> acc = new java.util.LinkedHashMap<>();
        for (var a : ctx.repo.attempts) {
            if (!a.userId.equals(userId)) continue;
            double[] d = acc.computeIfAbsent(a.quizTitle, k -> new double[2]);
            d[0] += a.percentage();
            d[1]++;
        }
        List<Stat> out = new java.util.ArrayList<>();
        for (var e : acc.entrySet()) {
            out.add(new Stat(e.getKey(), e.getValue()[0] / e.getValue()[1]));
        }
        return out;
    }
}
