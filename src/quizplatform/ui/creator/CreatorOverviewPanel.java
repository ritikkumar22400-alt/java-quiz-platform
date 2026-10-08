package quizplatform.ui.creator;

import quizplatform.model.Attempt;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.service.ReportService.Stat;
import quizplatform.ui.Ui;
import quizplatform.ui.components.ChartPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreatorOverviewPanel extends JPanel {

    private final AppContext ctx;
    private final JPanel statsRow = new JPanel(new java.awt.BorderLayout());
    private final ChartPanel avgChart = new ChartPanel(ChartPanel.Type.BAR, "Average score per quiz (%)", List.of());
    private final ChartPanel attemptsChart = new ChartPanel(ChartPanel.Type.BAR, "Attempts received per quiz", List.of());
    private final ChartPanel passChart = new ChartPanel(ChartPanel.Type.PIE, "Pass/fail across my quizzes", List.of());
    private final ChartPanel trendChart = new ChartPanel(ChartPanel.Type.LINE, "Recent attempts on my quizzes", List.of());

    public CreatorOverviewPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Performance Overview",
                "Summary of quiz performance and participant feedback on your content.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        statsRow.setOpaque(false);

        JPanel charts = new JPanel(new GridLayout(2, 2, 14, 14));
        charts.setOpaque(false);
        charts.add(card(avgChart));
        charts.add(card(attemptsChart));
        charts.add(card(passChart));
        charts.add(card(trendChart));

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(charts);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.BG);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(statsRow, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    private JPanel card(ChartPanel chart) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Ui.LINE),
                new javax.swing.border.EmptyBorder(10, 10, 10, 10)));
        p.add(chart, BorderLayout.CENTER);
        p.setPreferredSize(new java.awt.Dimension(500, 300));
        return p;
    }

    public void refresh() {
        List<Quiz> mine = ctx.quizzes.byCreator(ctx.auth.currentUser().id);
        List<Attempt> myAttempts = new ArrayList<>();
        for (Attempt a : ctx.repo.attempts) {
            for (Quiz q : mine) {
                if (a.quizId.equals(q.id)) {
                    myAttempts.add(a);
                    break;
                }
            }
        }

        Map<String, double[]> acc = new LinkedHashMap<>();
        Map<String, double[]> counts = new LinkedHashMap<>();
        int passed = 0;
        double sum = 0;
        for (Attempt a : myAttempts) {
            double[] d = acc.computeIfAbsent(a.quizTitle, k -> new double[2]);
            d[0] += a.percentage();
            d[1]++;
            counts.computeIfAbsent(a.quizTitle, k -> new double[1])[0]++;
            sum += a.percentage();
            if (a.percentage() >= ctx.repo.settings.passPercentage) passed++;
        }

        List<Stat> avg = new ArrayList<>();
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            avg.add(new Stat(e.getKey(), e.getValue()[1] == 0 ? 0 : e.getValue()[0] / e.getValue()[1]));
        }
        List<Stat> attemptStats = new ArrayList<>();
        for (Map.Entry<String, double[]> e : counts.entrySet()) {
            attemptStats.add(new Stat(e.getKey(), e.getValue()[0]));
        }
        List<Stat> passFail = List.of(new Stat("Passed", passed), new Stat("Failed", myAttempts.size() - passed));

        List<Stat> trend = new ArrayList<>();
        var sorted = new ArrayList<>(myAttempts);
        sorted.sort((x, y) -> Long.compare(x.submittedAt, y.submittedAt));
        int i = 1;
        for (Attempt a : sorted) {
            trend.add(new Stat(i++ + ". " + a.userName, Math.round(a.percentage())));
        }

        avgChart.setData(avg, "Average score per quiz (%)");
        attemptsChart.setData(attemptStats, "Attempts received per quiz");
        passChart.setData(passFail, "Pass/fail across my quizzes");
        trendChart.setData(trend, "Recent attempts on my quizzes");

        double avgScore = myAttempts.isEmpty() ? 0 : sum / myAttempts.size();
        int published = (int) mine.stream().filter(q -> q.status == Quiz.Status.APPROVED).count();
        statsRow.removeAll();
        statsRow.add(Ui.statRow(new String[][]{
                {"Quizzes created", String.valueOf(mine.size())},
                {"Published", String.valueOf(published)},
                {"Attempts received", String.valueOf(myAttempts.size())},
                {"Average score", Ui.pct(avgScore)},
                {"Passed attempts", String.valueOf(passed)}
        }), java.awt.BorderLayout.CENTER);
        statsRow.revalidate();
        statsRow.repaint();
        repaint();
    }
}
