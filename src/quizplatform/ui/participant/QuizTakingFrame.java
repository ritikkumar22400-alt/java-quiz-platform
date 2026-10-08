package quizplatform.ui.participant;

import quizplatform.model.Attempt;
import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** Full-screen timed quiz experience with auto-submit on expiry. */
public class QuizTakingFrame extends JFrame {

    private final AppContext ctx;
    private final Quiz quiz;
    private final Runnable onFinished;
    private final List<Integer> answers = new ArrayList<>();
    private final long startedAt = System.currentTimeMillis();

    private final int totalSec;
    private int remainingSec;
    private int current = 0;
    private boolean finished = false;

    private final JLabel timerLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JPanel questionHost = new JPanel(new BorderLayout());
    private final javax.swing.Timer timer;

    public QuizTakingFrame(AppContext ctx, Quiz quiz, Runnable onFinished) {
        super("Taking Quiz: " + quiz.title);
        this.ctx = ctx;
        this.quiz = quiz;
        this.onFinished = onFinished;
        this.totalSec = quiz.durationMins * 60;
        this.remainingSec = totalSec;
        for (int i = 0; i < quiz.questions.size(); i++) answers.add(-1);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (finished) {
                    dispose();
                    return;
                }
                int r = JOptionPane.showConfirmDialog(QuizTakingFrame.this,
                        "Quit without submitting? This attempt will not be recorded.",
                        "Confirm exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r == JOptionPane.YES_OPTION) {
                    timer.stop();
                    dispose();
                }
            }
        });

        setSize(860, 640);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        setContentPane(build());

        showQuestion(0);
        updateTimerLabel();
        timer = new javax.swing.Timer(1000, e -> tick());
        timer.start();
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Ui.BG);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.LINE),
                new javax.swing.border.EmptyBorder(12, 18, 12, 18)));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        JLabel t = new JLabel(quiz.title);
        t.setFont(Ui.H2);
        t.setForeground(Ui.DARK);
        titles.add(t, BorderLayout.NORTH);
        titles.add(progressLabel, BorderLayout.SOUTH);
        top.add(titles, BorderLayout.WEST);

        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        timerLabel.setForeground(Ui.PRIMARY);
        top.add(timerLabel, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        questionHost.setBackground(Ui.BG);
        JScrollPane scroll = new JScrollPane(questionHost);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.BG);
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.LINE),
                new javax.swing.border.EmptyBorder(10, 18, 10, 18)));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        JButton prev = Ui.neutral("< Previous");
        JButton next = Ui.neutral("Next >");
        nav.add(prev);
        nav.add(next);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton submit = Ui.success("Submit Quiz");
        actions.add(submit);

        bottom.add(nav, BorderLayout.WEST);
        bottom.add(actions, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        prev.addActionListener(e -> showQuestion(current - 1));
        next.addActionListener(e -> showQuestion(current + 1));
        submit.addActionListener(e -> confirmSubmit());
        return root;
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= quiz.questions.size()) return;
        current = index;
        Question q = quiz.questions.get(index);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.LINE),
                new javax.swing.border.EmptyBorder(24, 26, 24, 26)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(6, 4, 6, 4);

        progressLabel.setText("Question " + (index + 1) + " of " + quiz.questions.size()
                + "  |  " + q.points + " point" + (q.points > 1 ? "s" : ""));

        JLabel qLabel = new JLabel("<html><div style='width:600px'>" + (index + 1) + ". "
                + escape(q.text) + "</div></html>");
        qLabel.setFont(Ui.H2);
        qLabel.setForeground(Ui.TEXT);
        c.gridy = 0;
        card.add(qLabel, c);

        ButtonGroup group = new ButtonGroup();
        int chosen = answers.get(index);
        for (int i = 0; i < q.options.size(); i++) {
            JRadioButton rb = new JRadioButton((char) ('A' + i) + ". " + q.options.get(i));
            rb.setFont(Ui.BODY);
            rb.setSelected(i == chosen);
            final int opt = i;
            rb.addActionListener(e -> answers.set(current, opt));
            group.add(rb);
            c.gridy = i + 1;
            c.insets = new Insets(4, 4, 4, 4);
            card.add(rb, c);
        }

        questionHost.removeAll();
        questionHost.add(card, BorderLayout.NORTH);
        questionHost.revalidate();
        questionHost.repaint();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void tick() {
        remainingSec--;
        updateTimerLabel();
        if (remainingSec <= 0) {
            timer.stop();
            finished = true;
            finish(true);
        }
    }

    private void updateTimerLabel() {
        int m = Math.max(0, remainingSec) / 60;
        int s = Math.max(0, remainingSec) % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
        timerLabel.setForeground(remainingSec <= 60 ? Ui.DANGER : Ui.PRIMARY);
    }

    private void confirmSubmit() {
        int unanswered = 0;
        for (int a : answers) if (a < 0) unanswered++;
        String msg = unanswered > 0
                ? "You have " + unanswered + " unanswered question(s). Submit anyway?"
                : "Submit your answers now?";
        int r = JOptionPane.showConfirmDialog(this, msg, "Submit Quiz",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            timer.stop();
            finished = true;
            finish(false);
        }
    }

    private void finish(boolean timedOut) {
        try {
            Attempt a = ctx.attempts.submit(ctx.auth.currentUser(), quiz, answers, startedAt);
            StringBuilder sb = new StringBuilder();
            sb.append("<html><b>").append(timedOut ? "Time is up! Answers submitted automatically." : "Quiz submitted successfully!")
                    .append("</b><br><br>")
                    .append("Score: <b>").append(a.score).append(" / ").append(a.maxScore).append("</b>")
                    .append("  (").append(Ui.pct(a.percentage())).append(")<br>")
                    .append("Result: <b>").append(a.percentage() >= ctx.repo.settings.passPercentage ? "PASSED" : "FAILED").append("</b><br>")
                    .append("Time taken: ").append(a.timeTakenSec).append(" seconds<br>")
                    .append("Feedback: ").append(a.feedback).append("<br><br>");

            sb.append("<table cellpadding='4'>");
            int i = 0;
            for (Question q : quiz.questions) {
                int ans = answers.get(i);
                boolean ok = ans == q.correctIndex;
                sb.append("<tr><td>").append(ok ? "<font color='#16A34A'><b>✔</b></font>" : "<font color='#DC2626'><b>✘</b></font>")
                        .append("</td><td>Q").append(i + 1).append(": ").append(escape(q.text))
                        .append("<br><small>Your answer: ").append(ans < 0 ? "unanswered" : escape(q.options.get(ans)))
                        .append(" | Correct: ").append(escape(q.options.get(q.correctIndex)))
                        .append("</small></td></tr>");
                i++;
            }
            sb.append("</table></html>");

            JLabel result = new JLabel(sb.toString());
            result.setFont(Ui.BODY);
            JScrollPane sp = new JScrollPane(result);
            sp.setPreferredSize(new Dimension(560, 420));
            JOptionPane.showMessageDialog(this, sp, "Quiz Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
        if (onFinished != null) onFinished.run();
    }
}
