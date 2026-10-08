package quizplatform.ui.admin;

import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.service.QuizService;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.util.List;

public class QuizApprovalPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Title", "Creator", "Topic", "Questions", "Duration", "Status", "Submitted");
    private final JTable table = Ui.table(model);
    private final JComboBox<String> filter = Ui.combo(new String[]{"All", "Pending Approval", "Approved", "Rejected", "Draft"});
    private List<Quiz> rows = List.of();

    public QuizApprovalPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz Content Management",
                "Approve or reject quiz content submitted by quiz creators.", buildBody()),
                BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton approve = Ui.success("Approve");
        JButton reject = Ui.danger("Reject");
        JButton view = Ui.neutral("View Details");
        JButton refreshBtn = Ui.neutral("Refresh");
        left.add(approve);
        left.add(reject);
        left.add(view);
        left.add(refreshBtn);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(Ui.label("Filter:"));
        right.add(filter);
        toolbar.add(right, BorderLayout.EAST);
        filter.addActionListener(e -> refresh());

        approve.addActionListener(e -> {
            Quiz q = selected();
            if (q == null) return;
            try {
                ctx.quizzes.approve(q);
                Ui.success(this, "Content approved: \"" + q.title + "\" is now available to participants.");
                refresh();
            } catch (IllegalArgumentException ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        reject.addActionListener(e -> {
            Quiz q = selected();
            if (q == null) return;
            String reason = Ui.prompt(this, "Reason for rejection", "");
            if (reason == null) return;
            try {
                ctx.quizzes.reject(q, reason);
                Ui.success(this, "Content rejected: \"" + q.title + "\"\nThe creator will see your reason.");
                refresh();
            } catch (IllegalArgumentException ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        view.addActionListener(e -> {
            Quiz q = selected();
            if (q != null) showDetails(q);
        });

        refreshBtn.addActionListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);
        return body;
    }

    private void showDetails(Quiz q) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(q.title).append("</b><br>")
                .append("By: ").append(q.creatorName).append(" | Topic: ").append(q.topic)
                .append(" | Duration: ").append(q.durationMins).append(" min<br>")
                .append("Status: ").append(QuizService.label(q.status));
        if (!q.reviewNote.isEmpty()) sb.append(" | Note: ").append(q.reviewNote);
        sb.append("<br><br>");
        int i = 1;
        for (Question question : q.questions) {
            sb.append(i++).append(". ").append(question.text).append("<br>&nbsp;&nbsp;Options: ");
            for (int o = 0; o < question.options.size(); o++) {
                sb.append(o == question.correctIndex ? "<b>[" : "[");
                sb.append(question.options.get(o));
                sb.append(o == question.correctIndex ? "] (correct)</b>" : "]");
                sb.append(o < question.options.size() - 1 ? ", " : "<br>");
            }
            sb.append("<br>");
        }
        sb.append("</html>");
        JLabel content = new JLabel(sb.toString());
        content.setFont(Ui.BODY);
        javax.swing.JOptionPane.showMessageDialog(this, content, "Quiz Details",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private Quiz selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            Ui.info(this, "Select a quiz first.");
            return null;
        }
        return rows.get(r);
    }

    public void refresh() {
        String sel = (String) filter.getSelectedItem();
        rows = ctx.quizzes.all().stream()
                .filter(q -> sel == null || sel.equals("All")
                        || QuizService.label(q.status).equals(sel))
                .toList();
        model.setRowCount(0);
        for (Quiz q : rows) {
            model.addRow(Ui.row(q.title, q.creatorName, q.topic,
                    q.questions.size() + " (" + q.totalPoints() + " pts)",
                    q.durationMins + " min",
                    QuizService.label(q.status),
                    q.submittedAt > 0 ? Ui.date(q.submittedAt) : "-"));
        }
    }
}
