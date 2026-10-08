package quizplatform.ui.participant;

import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.util.List;

public class AvailableQuizzesPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Quiz", "Topic", "Questions", "Duration", "Your Best", "Attempts Used", "Status");
    private final JTable table = Ui.table(model);
    private List<Quiz> rows = List.of();

    public AvailableQuizzesPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Take Quizzes",
                "Select an approved quiz to start. Each quiz is timed and auto-submits when time runs out.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton start = Ui.success("Start Quiz");
        JButton remind = Ui.primary("Remind Me");
        JButton refreshBtn = Ui.neutral("Refresh");
        toolbar.add(start);
        toolbar.add(remind);
        toolbar.add(refreshBtn);

        start.addActionListener(e -> startSelected());
        remind.addActionListener(e -> addReminder());
        refreshBtn.addActionListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);
        return body;
    }

    private Quiz selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            Ui.info(this, "Select a quiz first.");
            return null;
        }
        return rows.get(r);
    }

    private void startSelected() {
        Quiz q = selected();
        if (q == null) return;
        String block = ctx.attempts.canAttempt(ctx.auth.currentUser(), q);
        if (block != null) {
            Ui.error(this, block);
            return;
        }
        if (!Ui.confirm(this, "Start \"" + q.title + "\"?\nDuration: " + q.durationMins
                + " minutes. The timer starts immediately.")) return;

        var owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        Runnable after = this::refresh;
        if (owner instanceof java.awt.Frame f) {
            new QuizTakingFrame(ctx, q, after).setVisible(true);
        } else {
            new QuizTakingFrame(ctx, q, after).setVisible(true);
        }
    }

    private void addReminder() {
        Quiz q = selected();
        if (q == null) return;
        try {
            long at = System.currentTimeMillis() + 86_400_000L;
            ctx.reminders.add(ctx.auth.currentUser().id, q.id, q.title,
                    "Reminder for quiz: " + q.title, at);
            Ui.success(this, "Reminder set for tomorrow:\n" + q.title + "\n"
                    + Ui.date(at));
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    public void refresh() {
        var me = ctx.auth.currentUser();
        rows = ctx.quizzes.approved();
        model.setRowCount(0);
        for (Quiz q : rows) {
            int used = ctx.repo.attemptCount(me.id, q.id);
            int best = bestScore(me.id, q.id);
            model.addRow(Ui.row(q.title, q.topic, q.questions.size() + " (" + q.totalPoints() + " pts)",
                    q.durationMins + " min",
                    best < 0 ? "-" : best + " pts",
                    used + " / " + ctx.repo.settings.maxAttemptsPerQuiz,
                    used >= ctx.repo.settings.maxAttemptsPerQuiz ? "Limit reached" : "Available"));
        }
    }

    private int bestScore(String userId, String quizId) {
        int best = -1;
        for (var a : ctx.repo.attempts) {
            if (a.userId.equals(userId) && a.quizId.equals(quizId)) {
                if (a.score > best) best = a.score;
            }
        }
        return best;
    }
}
