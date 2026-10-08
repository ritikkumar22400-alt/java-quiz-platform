package quizplatform.ui.creator;

import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.service.AppContext;
import quizplatform.service.QuizService;
import quizplatform.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/** Modal dialog used to create or edit a quiz and its questions. */
public class QuizEditorDialog extends JDialog {

    private final AppContext ctx;
    private final Quiz existing;
    private final Runnable onSaved;

    private final JTextField title = Ui.field(28);
    private final JTextField topic = Ui.field(28);
    private final JSpinner duration = new JSpinner(new SpinnerNumberModel(10, 1, 300, 1));
    private final JTextArea description = new JTextArea(2, 28);

    private final List<Question> questions = new ArrayList<>();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> questionList = new JList<>(listModel);

    private final JTextArea qText = new JTextArea(3, 30);
    private final JTextField[] opts = {Ui.field(30), Ui.field(30), Ui.field(30), Ui.field(30)};
    private final JComboBox<String> correct = Ui.combo(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
    private final JSpinner points = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

    private int editIndex = -1;

    public QuizEditorDialog(JFrame owner, AppContext ctx, Quiz existing, Runnable onSaved) {
        super(owner, existing == null ? "Create Quiz" : "Edit Quiz: " + existing.title, true);
        this.ctx = ctx;
        this.existing = existing;
        this.onSaved = onSaved;
        setMinimumSize(new Dimension(880, 680));
        setContentPane(build());
        if (existing != null) loadExisting();
        clearQuestionForm();
        pack();
        setLocationRelativeTo(owner);
    }

    private void loadExisting() {
        title.setText(existing.title);
        topic.setText(existing.topic);
        duration.setValue(existing.durationMins);
        description.setText(existing.description);
        questions.addAll(existing.questions);
        refreshList();
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));
        root.add(topForm(), BorderLayout.NORTH);
        root.add(questionSection(), BorderLayout.CENTER);
        root.add(bottomBar(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel topForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 6, 4, 6);

        c.gridx = 0; c.gridy = 0;
        form.add(Ui.label("Title *"), c);
        c.gridx = 1;
        form.add(title, c);
        c.gridx = 2;
        form.add(Ui.label("Topic"), c);
        c.gridx = 3;
        form.add(topic, c);

        c.gridx = 0; c.gridy = 1;
        form.add(Ui.label("Duration (min) *"), c);
        c.gridx = 1;
        duration.setFont(Ui.BODY);
        form.add(duration, c);
        c.gridx = 2;
        form.add(Ui.label("Questions"), c);
        c.gridx = 3;
        JLabel count = new JLabel("managed on the right");
        count.setFont(Ui.SMALL);
        count.setForeground(Ui.MUTED);
        form.add(count, c);

        c.gridx = 0; c.gridy = 2;
        c.anchor = GridBagConstraints.NORTH;
        form.add(Ui.label("Description"), c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        description.setFont(Ui.BODY);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        form.add(new JScrollPane(description), c);
        c.gridwidth = 1;
        return form;
    }

    private JPanel questionSection() {
        questionList.setFont(Ui.BODY);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.setFixedCellHeight(30);
        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && questionList.getSelectedIndex() >= 0) {
                editIndex = questionList.getSelectedIndex();
                showQuestion(questions.get(editIndex));
            }
        });
        JScrollPane listScroll = new JScrollPane(questionList);
        listScroll.setPreferredSize(new Dimension(280, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder("Questions in this quiz"));

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBorder(BorderFactory.createTitledBorder("Question details"));

        qText.setFont(Ui.BODY);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 6, 3, 6);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        form.add(Ui.label("Question text *"), c);
        c.gridy = 1;
        form.add(new JScrollPane(qText), c);

        c.gridwidth = 1;
        for (int i = 0; i < 4; i++) {
            c.gridx = 0; c.gridy = 2 + i;
            form.add(Ui.label("Option " + (i + 1) + " *"), c);
            c.gridx = 1; c.gridwidth = 2;
            form.add(opts[i], c);
            c.gridwidth = 1;
        }

        c.gridx = 0; c.gridy = 6;
        form.add(Ui.label("Correct answer"), c);
        c.gridx = 1;
        correct.setFont(Ui.BODY);
        form.add(correct, c);
        c.gridx = 2;
        points.setFont(Ui.BODY);
        JPanel pWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pWrap.setOpaque(false);
        pWrap.add(Ui.muted("Points:"));
        pWrap.add(points);
        form.add(pWrap, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton add = Ui.success("Add Question");
        JButton update = Ui.neutral("Update Selected");
        JButton remove = Ui.danger("Remove Selected");
        JButton clear = Ui.neutral("Clear Form");
        buttons.add(add);
        buttons.add(update);
        buttons.add(remove);
        buttons.add(clear);

        add.addActionListener(e -> addQuestion());
        update.addActionListener(e -> updateQuestion());
        remove.addActionListener(e -> removeQuestion());
        clear.addActionListener(e -> {
            editIndex = -1;
            questionList.clearSelection();
            clearQuestionForm();
        });

        right.add(new JScrollPane(form), BorderLayout.CENTER);
        right.add(buttons, BorderLayout.SOUTH);

        JPanel split = new JPanel(new BorderLayout(12, 0));
        split.add(listScroll, BorderLayout.WEST);
        split.add(right, BorderLayout.CENTER);
        return split;
    }

    private JPanel bottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        JLabel status = new JLabel(existing == null
                ? "New quiz - saved as Draft, then submitted for admin approval."
                : "Status: " + QuizService.label(existing.status));
        status.setFont(Ui.SMALL);
        status.setForeground(Ui.MUTED);
        bar.add(status, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton saveDraft = Ui.primary("Save as Draft");
        JButton saveSubmit = Ui.success("Save & Submit for Approval");
        JButton cancel = Ui.neutral("Cancel");
        buttons.add(saveDraft);
        buttons.add(saveSubmit);
        buttons.add(cancel);
        bar.add(buttons, BorderLayout.EAST);

        saveDraft.addActionListener(e -> save(false));
        saveSubmit.addActionListener(e -> save(true));
        cancel.addActionListener(e -> dispose());
        return bar;
    }

    // ---------------------------------------------------------- question ops
    private void addQuestion() {
        Question q = readQuestionForm();
        questions.add(q);
        refreshList();
        editIndex = -1;
        questionList.clearSelection();
        clearQuestionForm();
        questionList.setSelectedIndex(questions.size() - 1);
    }

    private void updateQuestion() {
        if (editIndex < 0 || editIndex >= questions.size()) {
            Ui.info(this, "Select a question in the list first.");
            return;
        }
        questions.set(editIndex, readQuestionForm());
        refreshList();
        questionList.setSelectedIndex(editIndex);
    }

    private void removeQuestion() {
        if (editIndex < 0 || editIndex >= questions.size()) {
            Ui.info(this, "Select a question in the list first.");
            return;
        }
        questions.remove(editIndex);
        editIndex = -1;
        questionList.clearSelection();
        clearQuestionForm();
        refreshList();
    }

    private Question readQuestionForm() {
        String text = qText.getText().trim();
        if (text.length() < 3) throw new IllegalArgumentException("Question text must be at least 3 characters.");
        List<String> options = new ArrayList<>();
        for (JTextField f : opts) {
            String v = f.getText().trim();
            if (v.isEmpty()) throw new IllegalArgumentException("All four options are required.");
            options.add(v);
        }
        return new Question(text, options, correct.getSelectedIndex(), (Integer) points.getValue());
    }

    private void showQuestion(Question q) {
        qText.setText(q.text);
        for (int i = 0; i < 4; i++) {
            opts[i].setText(i < q.options.size() ? q.options.get(i) : "");
        }
        correct.setSelectedIndex(Math.min(3, Math.max(0, q.correctIndex)));
        points.setValue(q.points);
    }

    private void clearQuestionForm() {
        qText.setText("");
        for (JTextField f : opts) f.setText("");
        correct.setSelectedIndex(0);
        points.setValue(1);
    }

    private void refreshList() {
        listModel.clear();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            listModel.addElement((i + 1) + ". " + q.text);
        }
    }

    // ------------------------------------------------------------------ save
    private void save(boolean submit) {
        try {
            if (existing == null) {
                Quiz q = ctx.quizzes.create(ctx.auth.currentUser(), title.getText(),
                        description.getText(), topic.getText(), (Integer) duration.getValue(),
                        new ArrayList<>(questions));
                if (submit) ctx.quizzes.submitForApproval(q);
                Ui.success(this, submit
                        ? "Quiz created and submitted for approval: \"" + q.title + "\""
                        : "Quiz created successfully as a draft: \"" + q.title + "\"");
            } else {
                ctx.quizzes.update(existing, title.getText(), description.getText(),
                        topic.getText(), (Integer) duration.getValue(), new ArrayList<>(questions));
                if (submit && existing.status != quizplatform.model.Quiz.Status.PENDING) {
                    ctx.quizzes.submitForApproval(existing);
                }
                Ui.success(this, submit
                        ? "Quiz updated and submitted for approval: \"" + existing.title + "\""
                        : "Quiz updated successfully: \"" + existing.title + "\"");
            }
            if (onSaved != null) onSaved.run();
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
