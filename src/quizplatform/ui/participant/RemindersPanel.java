package quizplatform.ui.participant;

import quizplatform.model.Quiz;
import quizplatform.model.Reminder;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RemindersPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Quiz", "Reminder Time", "Note", "Status");
    private final JTable table = Ui.table(model);
    private List<Reminder> rows = List.of();

    public RemindersPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Quiz Reminders",
                "Set and view reminders so you never miss a quiz.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        form.setOpaque(false);

        JComboBox<String> quizCombo = Ui.combo(new String[]{});
        quizCombo.setPreferredSize(new java.awt.Dimension(240, 32));
        JTextField note = Ui.field(18);
        JTextField date = Ui.field(10);
        JTextField time = Ui.field(6);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
        date.setText(df.format(new Date(System.currentTimeMillis() + 86_400_000L)));
        time.setText("10:00");

        JButton add = Ui.primary("Add Reminder");
        JButton delete = Ui.danger("Delete Selected");
        JButton refreshBtn = Ui.neutral("Refresh");

        form.add(Ui.label("Quiz:"));
        form.add(quizCombo);
        form.add(Ui.label("Date (dd/MM/yyyy):"));
        form.add(date);
        form.add(Ui.label("Time (HH:mm):"));
        form.add(time);
        form.add(Ui.label("Note:"));
        form.add(note);
        form.add(add);
        form.add(delete);
        form.add(refreshBtn);

        add.addActionListener(e -> {
            try {
                String title = (String) quizCombo.getSelectedItem();
                if (title == null || title.equals("(no approved quizzes)")) {
                    Ui.info(this, "No approved quiz available to remind about.");
                    return;
                }
                Quiz quiz = ctx.quizzes.approved().stream()
                        .filter(q -> q.title.equals(title)).findFirst().orElse(null);
                if (quiz == null) {
                    Ui.error(this, "Quiz not found.");
                    return;
                }
                Date when = new SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .parse(date.getText().trim() + " " + time.getText().trim());
                ctx.reminders.add(ctx.auth.currentUser().id, quiz.id, quiz.title, note.getText(), when.getTime());
                Ui.success(this, "Reminder created for \"" + quiz.title + "\" on " + Ui.date(when.getTime()));
                refresh();
            } catch (ParseException ex) {
                Ui.error(this, "Invalid date or time. Use dd/MM/yyyy and HH:mm.");
            } catch (IllegalArgumentException ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0 || r >= rows.size()) {
                Ui.info(this, "Select a reminder first.");
                return;
            }
            ctx.reminders.delete(rows.get(r).id);
            Ui.success(this, "Reminder deleted.");
            refresh();
        });

        refreshBtn.addActionListener(e -> refresh());

        JPanel toolbar = new JPanel(new BorderLayout(0, 8));
        toolbar.setOpaque(false);
        toolbar.add(form, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);

        // stash combo for refresh
        this.quizCombo = quizCombo;
        return body;
    }

    private JComboBox<String> quizCombo;

    public void refresh() {
        if (quizCombo != null) {
            String sel = (String) quizCombo.getSelectedItem();
            quizCombo.removeAllItems();
            List<Quiz> approved = ctx.quizzes.approved();
            if (approved.isEmpty()) quizCombo.addItem("(no approved quizzes)");
            for (Quiz q : approved) quizCombo.addItem(q.title);
            if (sel != null) quizCombo.setSelectedItem(sel);
        }

        rows = ctx.reminders.forUser(ctx.auth.currentUser().id);
        model.setRowCount(0);
        long now = System.currentTimeMillis();
        for (Reminder r : rows) {
            model.addRow(Ui.row(r.quizTitle, Ui.date(r.remindAt),
                    r.note.isEmpty() ? "-" : r.note,
                    r.remindAt >= now ? "Upcoming" : "Past"));
        }
    }
}
