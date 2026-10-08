package quizplatform.ui.creator;

import quizplatform.model.Attempt;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class CreatorResultsPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Quiz", "Participant", "Score", "Result", "Submitted", "Graded");
    private final JTable table = Ui.table(model);
    private List<Attempt> rows = List.of();

    public CreatorResultsPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz Results",
                "Review submitted quizzes, provide feedback and finalize scoring.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton view = Ui.primary("Review & Grade");
        JButton refreshBtn = Ui.neutral("Refresh");
        toolbar.add(view);
        toolbar.add(refreshBtn);

        view.addActionListener(e -> {
            Attempt a = selected();
            if (a != null) showGradeDialog(a);
        });
        refreshBtn.addActionListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);
        return body;
    }

    private Attempt selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            Ui.info(this, "Select a submitted quiz first.");
            return null;
        }
        return rows.get(r);
    }

    private void showGradeDialog(Attempt a) {
        Quiz quiz = ctx.repo.findQuiz(a.quizId);
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setPreferredSize(new Dimension(720, 520));

        StringBuilder sb = new StringBuilder("<html><b>").append(a.userName).append("</b> &mdash; ")
                .append(a.quizTitle).append("<br>Score: ").append(a.score).append("/").append(a.maxScore)
                .append(" (").append(Ui.pct(a.percentage())).append(")")
                .append(" &nbsp;|&nbsp; Time: ").append(a.timeTakenSec).append("s")
                .append(" &nbsp;|&nbsp; Submitted: ").append(Ui.date(a.submittedAt))
                .append("<br><br>");
        if (quiz != null) {
            int i = 0;
            for (var q : quiz.questions) {
                int ans = i < a.answers.size() ? a.answers.get(i) : -1;
                boolean ok = ans == q.correctIndex;
                sb.append(ok ? "<span style='color:#16A34A'>✔</span>" : "<span style='color:#DC2626'>✘</span> ");
                sb.append("Q").append(i + 1).append(": ").append(q.text).append("<br>");
                sb.append("&nbsp;&nbsp;Answered: ").append(ans < 0 ? "(unanswered)" : q.options.get(ans));
                sb.append(" &nbsp;|&nbsp; Correct: ").append(q.options.get(q.correctIndex)).append("<br><br>");
                i++;
            }
        }
        sb.append("</html>");
        JLabel breakdown = new JLabel(sb.toString());
        breakdown.setFont(Ui.BODY);
        JScrollPane breakdownScroll = new JScrollPane(breakdown);
        breakdownScroll.setBorder(BorderFactoryTitledBorder("Answer breakdown"));

        SpinnerNumberModel scoreModel = new SpinnerNumberModel(a.score, 0, Math.max(0, a.maxScore), 1);
        javax.swing.JSpinner scoreSpinner = new javax.swing.JSpinner(scoreModel);
        scoreSpinner.setFont(Ui.BODY);
        JTextArea feedback = new JTextArea(3, 40);
        feedback.setFont(Ui.BODY);
        feedback.setLineWrap(true);
        feedback.setWrapStyleWord(true);
        feedback.setText(a.feedback);
        JScrollPane feedbackScroll = new JScrollPane(feedback);
        feedbackScroll.setBorder(BorderFactoryTitledBorder("Feedback for the participant"));

        JPanel inputs = new JPanel(new BorderLayout(0, 8));
        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(Ui.label("Final score:"));
        scoreRow.add(scoreSpinner);
        scoreRow.add(Ui.muted("(auto-scored; adjust if needed)"));
        inputs.add(scoreRow, BorderLayout.NORTH);
        inputs.add(feedbackScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.add(breakdownScroll, BorderLayout.CENTER);
        center.add(inputs, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        int res = javax.swing.JOptionPane.showConfirmDialog(this, root, "Review & Grade",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (res != javax.swing.JOptionPane.OK_OPTION) return;
        try {
            ctx.attempts.grade(a, (Integer) scoreSpinner.getValue(), feedback.getText());
            Ui.success(this, "Result graded successfully for " + a.userName + ".");
            refresh();
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static javax.swing.border.Border BorderFactoryTitledBorder(String title) {
        return javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(Ui.LINE), title);
    }

    public void refresh() {
        String me = ctx.auth.currentUser().id;
        List<Quiz> mine = ctx.quizzes.byCreator(me);
        rows = new ArrayList<>();
        for (Attempt a : ctx.repo.attempts) {
            for (Quiz q : mine) {
                if (a.quizId.equals(q.id)) {
                    rows.add(a);
                    break;
                }
            }
        }
        rows.sort((x, y) -> Long.compare(y.submittedAt, x.submittedAt));
        model.setRowCount(0);
        for (Attempt a : rows) {
            model.addRow(Ui.row(a.quizTitle, a.userName, a.score + "/" + a.maxScore,
                    Ui.pct(a.percentage()), Ui.date(a.submittedAt), a.graded ? "Yes" : "No"));
        }
    }
}
