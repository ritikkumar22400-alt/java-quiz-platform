package quizplatform.ui.admin;

import quizplatform.model.SystemSettings;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SettingsPanel extends JPanel {

    private final AppContext ctx;
    private final JTextField siteName = Ui.field(30);
    private final JSpinner duration = new JSpinner(new SpinnerNumberModel(10, 1, 300, 1));
    private final JSpinner passPct = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
    private final JSpinner maxAttempts = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
    private final JCheckBox retries = new JCheckBox("Allow quiz retakes");
    private final JCheckBox shuffle = new JCheckBox("Shuffle questions when taking a quiz");
    private final JCheckBox leaderboard = new JCheckBox("Leaderboard enabled");
    private final JCheckBox registration = new JCheckBox("Open self-registration");

    public SettingsPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("System Settings",
                "Manage system-wide configuration. Changes apply immediately.", buildBody()),
                BorderLayout.CENTER);
        load();
    }

    private JPanel buildBody() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        c.weightx = 1;

        for (JSpinner s : new JSpinner[]{duration, passPct, maxAttempts}) s.setFont(Ui.BODY);
        for (JCheckBox cb : new JCheckBox[]{retries, shuffle, leaderboard, registration}) cb.setFont(Ui.BODY);

        addRow(form, c, 0, "Platform name", siteName);
        addRow(form, c, 1, "Default quiz duration (minutes)", duration);
        addRow(form, c, 2, "Pass percentage", passPct);
        addRow(form, c, 3, "Max attempts per quiz", maxAttempts);
        addRow(form, c, 4, "", retries);
        addRow(form, c, 5, "", shuffle);
        addRow(form, c, 6, "", leaderboard);
        addRow(form, c, 7, "", registration);

        JButton save = Ui.primary("Save Settings");
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.insets = new Insets(20, 8, 8, 8);
        JPanel btnWrap = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(save);
        form.add(btnWrap, c);
        save.addActionListener(e -> save());

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(Ui.card("Configuration", form), BorderLayout.NORTH);
        return body;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridwidth = 1;
        c.insets = new Insets(8, 8, 8, 8);
        c.gridx = 0;
        c.gridy = row;
        if (label.isEmpty()) {
            form.add(new javax.swing.JLabel(" "), c);
        } else {
            form.add(Ui.label(label), c);
        }
        c.gridx = 1;
        form.add(field, c);
    }

    private void load() {
        SystemSettings s = ctx.repo.settings;
        siteName.setText(s.siteName);
        duration.setValue(s.defaultDurationMins);
        passPct.setValue(s.passPercentage);
        maxAttempts.setValue(s.maxAttemptsPerQuiz);
        retries.setSelected(s.allowRetries);
        shuffle.setSelected(s.shuffleQuestions);
        leaderboard.setSelected(s.leaderboardEnabled);
        registration.setSelected(s.registrationOpen);
    }

    private void save() {
        try {
            SystemSettings s = ctx.repo.settings;
            String name = siteName.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Platform name cannot be empty.");
            s.siteName = name;
            s.defaultDurationMins = (Integer) duration.getValue();
            s.passPercentage = (Integer) passPct.getValue();
            s.maxAttemptsPerQuiz = (Integer) maxAttempts.getValue();
            s.allowRetries = retries.isSelected();
            s.shuffleQuestions = shuffle.isSelected();
            s.leaderboardEnabled = leaderboard.isSelected();
            s.registrationOpen = registration.isSelected();
            ctx.repo.saveSettings();
            Ui.success(this, "System settings updated successfully.");
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}
