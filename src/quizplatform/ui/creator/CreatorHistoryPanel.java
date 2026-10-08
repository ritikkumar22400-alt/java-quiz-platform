package quizplatform.ui.creator;

import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.service.QuizService;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.util.List;

public class CreatorHistoryPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Title", "Status", "Created", "Submitted", "Reviewed", "Admin / Creator Note");
    private final JTable table = Ui.table(model);
    private final JPanel summaryRow = new JPanel(new java.awt.BorderLayout());
    private List<Quiz> rows = List.of();

    public CreatorHistoryPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz History",
                "Log of all quizzes you have created, submitted and published.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        summaryRow.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JButton refreshBtn = Ui.neutral("Refresh");
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(refreshBtn);
        toolbar.add(left, BorderLayout.WEST);
        refreshBtn.addActionListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(summaryRow, BorderLayout.NORTH);
        center.add(Ui.scroll(table), BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);
        return body;
    }

    public void refresh() {
        rows = ctx.quizzes.byCreator(ctx.auth.currentUser().id);
        int draft = 0, pending = 0, approved = 0, rejected = 0;
        model.setRowCount(0);
        for (Quiz q : rows) {
            switch (q.status) {
                case DRAFT -> draft++;
                case PENDING -> pending++;
                case APPROVED -> approved++;
                case REJECTED -> rejected++;
            }
            String note = q.status == Quiz.Status.REJECTED || q.status == Quiz.Status.APPROVED
                    ? q.reviewNote : "-";
            model.addRow(Ui.row(q.title, QuizService.label(q.status),
                    Ui.dateShort(q.createdAt),
                    q.submittedAt > 0 ? Ui.dateShort(q.submittedAt) : "-",
                    q.reviewedAt > 0 ? Ui.dateShort(q.reviewedAt) : "-",
                    note.isEmpty() ? "-" : note));
        }
        summaryRow.removeAll();
        summaryRow.add(Ui.statRow(new String[][]{
                {"Total created", String.valueOf(rows.size())},
                {"Draft", String.valueOf(draft)},
                {"Pending approval", String.valueOf(pending)},
                {"Published", String.valueOf(approved)},
                {"Rejected", String.valueOf(rejected)}
        }), java.awt.BorderLayout.CENTER);
        summaryRow.revalidate();
        summaryRow.repaint();
    }
}
