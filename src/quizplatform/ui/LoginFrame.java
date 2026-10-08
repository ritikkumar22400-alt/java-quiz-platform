package quizplatform.ui;

import quizplatform.model.User;
import quizplatform.service.AppContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {

    private final AppContext ctx;
    private final JTextField emailField = Ui.field(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    public LoginFrame(AppContext ctx) {
        this.ctx = ctx;
        setTitle(ctx.repo.settings.siteName + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        setContentPane(build());
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);

        // brand panel
        JPanel brand = Ui.gradientPanel(Ui.PRIMARY, new Color(0x7C, 0x3A, 0xED));
        brand.setPreferredSize(new Dimension(360, 0));
        brand.setLayout(new GridBagLayout());
        GridBagConstraints bg = new GridBagConstraints();
        bg.gridx = 0;
        bg.insets = new Insets(10, 24, 10, 24);
        bg.anchor = GridBagConstraints.WEST;

        bg.gridy = 0;
        JLabel logo = new JLabel("JQ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 56));
        logo.setForeground(Color.WHITE);
        brand.add(logo, bg);

        bg.gridy = 1;
        JLabel title = new JLabel("JavaQuiz Platform");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(Color.WHITE);
        brand.add(title, bg);

        bg.gridy = 2;
        JLabel tag = new JLabel("<html>Timed Java quizzes,<br>detailed performance reports<br>and full administration.</html>");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tag.setForeground(new Color(0xE4, 0xE0, 0xFF));
        brand.add(tag, bg);

        bg.gridy = 3;
        bg.insets = new Insets(44, 24, 10, 24);
        JLabel hint = new JLabel("<html><b>Demo accounts</b><br>"
                + "admin@quiz.local / admin123<br>"
                + "creator@quiz.local / creator123<br>"
                + "alice@quiz.local / pass123</html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(0xD5, 0xCF, 0xFF));
        brand.add(hint, bg);

        // form
        JPanel formWrap = new JPanel(new GridBagLayout());
        formWrap.setBackground(Ui.BG);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.LINE), new javax.swing.border.EmptyBorder(30, 36, 30, 36)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);

        c.gridy = 0;
        JLabel h = new JLabel("Welcome back");
        h.setFont(Ui.H1);
        h.setForeground(Ui.DARK);
        form.add(h, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        form.add(Ui.muted("Sign in to continue to your dashboard."), c);

        c.gridy = 2;
        c.insets = new Insets(6, 0, 6, 0);
        form.add(Ui.label("Email"), c);
        emailField.setFont(Ui.BODY);

        c.gridy = 3;
        form.add(emailField, c);

        c.gridy = 4;
        c.insets = new Insets(14, 0, 6, 0);
        form.add(Ui.label("Password"), c);

        c.gridy = 5;
        passwordField.setFont(Ui.BODY);
        passwordField.setBorder(emailField.getBorder());
        form.add(passwordField, c);

        c.gridy = 6;
        c.insets = new Insets(22, 0, 0, 0);
        JButton login = Ui.primary("Sign In");
        login.setPreferredSize(new Dimension(0, 42));
        form.add(login, c);

        c.gridy = 7;
        c.insets = new Insets(16, 0, 0, 0);
        form.add(Ui.muted("Accounts are created by the administrator."), c);

        c.gridy = 8;
        JButton exit = Ui.neutral("Exit");
        form.add(exit, c);

        login.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        exit.addActionListener(e -> System.exit(0));

        GridBagConstraints wrapC = new GridBagConstraints();
        wrapC.fill = GridBagConstraints.HORIZONTAL;
        wrapC.weightx = 1;
        formWrap.add(form, wrapC);

        root.add(brand, BorderLayout.WEST);
        root.add(formWrap, BorderLayout.CENTER);
        return root;
    }

    private void doLogin() {
        try {
            User u = ctx.auth.login(emailField.getText(), new String(passwordField.getPassword()));
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(ctx).setVisible(true));
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.error(this, "Login failed: " + ex.getMessage());
        }
    }
}
