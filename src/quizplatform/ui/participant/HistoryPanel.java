package quizplatform.ui.participant;

import quizplatform.model.Attempt;
import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Quiz", "Score", "Result", "Time Taken", "Submitted", "Feedback");
    private final JTable table = Ui.table(model);
    private List<Attempt> rows = List.of();
    private final JLabel summary = new JLabel();

    public HistoryPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz Participation History",
                "Table of all quizzes you have participated in, with results and feedback.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        summary.setFont(Ui.BODY);
        summary.setForeground(Ui.TEXT);

        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton view = Ui.primary("View Attempt Detail");
        JButton refreshBtn = Ui.neutral("Refresh");
        toolbar.add(view);
        toolbar.add(refreshBtn);
        view.addActionListener(e -> showDetail());
        refreshBtn.addActionListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(Ui.card("Summary", summary), BorderLayout.NORTH);
        center.add(Ui.scroll(table), BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);
        return body;
    }

    private Attempt selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            Ui.info(this, "Select an attempt first.");
            return null;
        }
        return rows.get(r);
    }

    private void showDetail() {
        Attempt a = selected();
        if (a == null) return;
        Quiz quiz = ctx.repo.findQuiz(a.quizId);
        StringBuilder sb = new StringBuilder("<html><b>").append(a.quizTitle).append("</b><br>")
                .append("Score: ").append(a.score).append("/").append(a.maxScore)
                .append(" (").append(Ui.pct(a.percentage())).append(")<br>")
                .append("Time taken: ").append(a.timeTakenSec).append("s<br>")
                .append("Submitted: ").append(Ui.date(a.submittedAt)).append("<br>")
                .append("Feedback: ").append(a.feedback.isEmpty() ? "-" : a.feedback)
                .append("<br><br>");
        if (quiz != null) {
            int i = 0;
            for (Question q : quiz.questions) {
                int ans = i < a.answers.size() ? a.answers.get(i) : -1;
                boolean ok = ans == q.correctIndex;
                sb.append(ok ? "<font color='#16A34A'><b>✔</b></font>" : "<font color='#DC2626'><b>✘</b></font> ")
                        .append("Q").append(i + 1).append(": ").append(escape(q.text)).append("<br>")
                        .append("&nbsp;&nbsp;Your answer: ")
                        .append(ans < 0 ? "unanswered" : escape(q.options.get(ans)))
                        .append(" | Correct: ").append(escape(q.options.get(q.correctIndex)))
                        .append("<br><br>");
                i++;
            }
        }
        sb.append("</html>");
        JLabel content = new JLabel(sb.toString());
        content.setFont(Ui.BODY);
        JScrollPane sp = new JScrollPane(content);
        sp.setPreferredSize(new Dimension(600, 420));
        javax.swing.JOptionPane.showMessageDialog(this, sp, "Attempt Detail",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void refresh() {
        rows = ctx.repo.attemptsFor(ctx.auth.currentUser().id);
        model.setRowCount(0);
        int passed = 0;
        for (Attempt a : rows) {
            boolean pass = a.percentage() >= ctx.repo.settings.passPercentage;
            if (pass) passed++;
            model.addRow(Ui.row(a.quizTitle, a.score + "/" + a.maxScore,
                    pass ? "Passed" : "Failed",
                    a.timeTakenSec + "s", Ui.date(a.submittedAt),
                    a.feedback.isEmpty() ? "-" : a.feedback));
        }
        summary.setText("<html>Quizzes attempted: <b>" + rows.size() + "</b> &nbsp;|&nbsp; Passed: <b>"
                + passed + "</b> &nbsp;|&nbsp; Pass threshold: <b>" + ctx.repo.settings.passPercentage + "%</b></html>");
    }
}
