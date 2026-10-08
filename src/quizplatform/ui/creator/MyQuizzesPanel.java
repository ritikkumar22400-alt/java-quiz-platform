package quizplatform.ui.creator;

import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.service.QuizService;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.List;

public class MyQuizzesPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Title", "Topic", "Questions", "Duration", "Status", "Review Note", "Created");
    private final JTable table = Ui.table(model);
    private List<Quiz> rows = List.of();

    public MyQuizzesPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz Creation",
                "Create and manage your quiz content. Drafts are reviewed by an administrator before publishing.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton create = Ui.primary("New Quiz");
        JButton edit = Ui.neutral("Edit");
        JButton submit = Ui.success("Submit for Approval");
        JButton delete = Ui.danger("Delete");
        JButton refreshBtn = Ui.neutral("Refresh");
        for (JButton b : new JButton[]{create, edit, submit, delete, refreshBtn}) toolbar.add(b);

        create.addActionListener(e -> openEditor(null));
        edit.addActionListener(e -> {
            Quiz q = selected();
            if (q != null) openEditor(q);
        });
        submit.addActionListener(e -> {
            Quiz q = selected();
            if (q == null) return;
            try {
                ctx.quizzes.submitForApproval(q);
                Ui.success(this, "Quiz submitted for approval: \"" + q.title + "\"\nThe administrator will review it shortly.");
                refresh();
            } catch (IllegalArgumentException ex) {
                Ui.error(this, ex.getMessage());
            }
        });
        delete.addActionListener(e -> {
            Quiz q = selected();
            if (q == null) return;
            if (!Ui.confirm(this, "Delete quiz \"" + q.title + "\"?")) return;
            try {
                ctx.quizzes.delete(q);
                Ui.success(this, "Quiz deleted: " + q.title);
                refresh();
            } catch (IllegalArgumentException ex) {
                Ui.error(this, ex.getMessage());
            }
        });
        refreshBtn.addActionListener(e -> refresh());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Quiz q = selected();
                    if (q != null) openEditor(q);
                }
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);
        return body;
    }

    private void openEditor(Quiz q) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        new QuizEditorDialog(owner, ctx, q, this::refresh).setVisible(true);
        refresh();
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
        rows = ctx.quizzes.byCreator(ctx.auth.currentUser().id);
        model.setRowCount(0);
        for (Quiz q : rows) {
            model.addRow(Ui.row(q.title, q.topic, q.questions.size() + " (" + q.totalPoints() + " pts)",
                    q.durationMins + " min", QuizService.label(q.status),
                    q.reviewNote.isEmpty() ? "-" : q.reviewNote, Ui.dateShort(q.createdAt)));
        }
    }
}
