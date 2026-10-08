package quizplatform.ui.admin;

import quizplatform.service.AppContext;
import quizplatform.service.ReportService;
import quizplatform.service.ReportService.Stat;
import quizplatform.ui.Ui;
import quizplatform.ui.components.ChartPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final AppContext ctx;
    private final ChartPanel avgChart = new ChartPanel(ChartPanel.Type.BAR, "Average score per quiz (%)", List.of());
    private final ChartPanel passChart = new ChartPanel(ChartPanel.Type.PIE, "Attempts pass/fail split", List.of());
    private final ChartPanel trendChart = new ChartPanel(ChartPanel.Type.LINE, "Attempts over the last 14 days", List.of());
    private final ChartPanel attemptsChart = new ChartPanel(ChartPanel.Type.BAR, "Attempts per quiz", List.of());
    private final JPanel statRowPanel = new JPanel(new java.awt.BorderLayout());

    public ReportsPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Performance Reports",
                "Graphical representation of quiz performance statistics across the platform.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        statRowPanel.setOpaque(false);
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(statRowPanel, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(2, 2, 14, 14));
        charts.setOpaque(false);
        charts.add(chartCard(avgChart));
        charts.add(chartCard(passChart));
        charts.add(chartCard(trendChart));
        charts.add(chartCard(attemptsChart));

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(charts);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.BG);
        scroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    private JPanel chartCard(ChartPanel chart) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Ui.LINE),
                new javax.swing.border.EmptyBorder(10, 10, 10, 10)));
        p.add(chart, BorderLayout.CENTER);
        p.setPreferredSize(new java.awt.Dimension(520, 320));
        return p;
    }

    public void refresh() {
        ReportService r = ctx.reports;
        avgChart.setData(r.averageScorePerQuiz(), "Average score per quiz (%)");
        passChart.setData(r.passFailSplit(), "Attempts pass/fail split");
        trendChart.setData(r.attemptsPerDay(14), "Attempts over the last 14 days");
        attemptsChart.setData(r.attemptsPerQuiz(), "Attempts per quiz");

        double avg = 0;
        var all = ctx.repo.attempts;
        if (!all.isEmpty()) {
            double sum = 0;
            for (var a : all) sum += a.percentage();
            avg = sum / all.size();
        }
        statRowPanel.removeAll();
        statRowPanel.add(Ui.statRow(new String[][]{
                {"Users", String.valueOf(ctx.repo.users.size())},
                {"Quizzes", String.valueOf(ctx.repo.quizzes.size())},
                {"Attempts", String.valueOf(all.size())},
                {"Average score", Ui.pct(avg)},
                {"Pass threshold", ctx.repo.settings.passPercentage + "%"}
        }), java.awt.BorderLayout.CENTER);
        statRowPanel.revalidate();
        statRowPanel.repaint();
        repaint();
    }
}
