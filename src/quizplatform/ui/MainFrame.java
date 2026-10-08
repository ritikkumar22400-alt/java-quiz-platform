package quizplatform.ui;

import quizplatform.model.User;
import quizplatform.service.AppContext;
import quizplatform.service.UserService;
import quizplatform.ui.admin.AlertsPanel;
import quizplatform.ui.admin.QuizApprovalPanel;
import quizplatform.ui.admin.ReportsPanel;
import quizplatform.ui.admin.SettingsPanel;
import quizplatform.ui.admin.UserManagementPanel;
import quizplatform.ui.common.MessagesPanel;
import quizplatform.ui.creator.CreatorHistoryPanel;
import quizplatform.ui.creator.CreatorOverviewPanel;
import quizplatform.ui.creator.CreatorResultsPanel;
import quizplatform.ui.creator.MyQuizzesPanel;
import quizplatform.ui.participant.AvailableQuizzesPanel;
import quizplatform.ui.participant.HistoryPanel;
import quizplatform.ui.participant.LeaderboardPanel;
import quizplatform.ui.participant.PerformancePanel;
import quizplatform.ui.participant.RemindersPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final List<NavButton> navButtons = new ArrayList<>();
    private final JLabel badgesLabel = new JLabel();

    public MainFrame(AppContext ctx) {
        this.ctx = ctx;
        User u = ctx.auth.currentUser();
        setTitle(ctx.repo.settings.siteName + " - " + UserService.roleLabel(u.role) + " Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        setContentPane(build());
        showPage(navButtons.get(0).pageKey);
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);
        root.add(header(), BorderLayout.NORTH);
        root.add(sidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        addPages();
        return root;
    }

    // ---------------------------------------------------------------- header
    private JPanel header() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.LINE),
                new javax.swing.border.EmptyBorder(12, 24, 12, 20)));

        JPanel brand = new JPanel(new java.awt.FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        // small gradient logo block
        JPanel logo = Ui.gradientPanel(Ui.PRIMARY, new Color(0x7C, 0x3A, 0xED));
        logo.setPreferredSize(new Dimension(30, 30));
        JLabel lg = new JLabel("JQ");
        lg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lg.setForeground(Color.WHITE);
        logo.setLayout(new java.awt.GridBagLayout());
        logo.add(lg);
        brand.add(logo);

        JLabel site = new JLabel(ctx.repo.settings.siteName);
        site.setFont(Ui.H2);
        site.setForeground(Ui.PRIMARY_DARK);
        brand.add(site);
        h.add(brand, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        User u = ctx.auth.currentUser();
        JLabel role = Ui.pill(UserService.roleLabel(u.role),
                Ui.PRIMARY_DARK, new Color(0xE0, 0xE7, 0xFF));
        right.add(role);

        JLabel who = new JLabel(u.name);
        who.setFont(Ui.BOLD);
        who.setForeground(Ui.DARK);
        right.add(who);
        right.add(badgesLabel);

        JButton logout = Ui.danger("Logout");
        logout.addActionListener(e -> {
            ctx.auth.logout();
            dispose();
            new LoginFrame(ctx).setVisible(true);
        });
        right.add(logout);
        h.add(right, BorderLayout.EAST);

        wrap.add(h, BorderLayout.CENTER);
        Ui.GradientPanel strip = new Ui.GradientPanel(Ui.PRIMARY, new Color(0x7C, 0x3A, 0xED));
        strip.setPreferredSize(new Dimension(0, 3));
        wrap.add(strip, BorderLayout.SOUTH);
        refreshBadges();
        return wrap;
    }

    private void refreshBadges() {
        User u = ctx.auth.currentUser();
        int unreadMsg = ctx.messages.unreadCount(u);
        int unreadAlert = ctx.alerts.unreadCount();
        StringBuilder sb = new StringBuilder("<html>");
        if (unreadMsg > 0) sb.append("<span style='color:#4F46E5'><b>").append(unreadMsg).append(" new</b></span> &nbsp;");
        if (u.role == User.Role.ADMIN && unreadAlert > 0) {
            sb.append("<span style='color:#D97706'><b>").append(unreadAlert).append(" alert(s)</b></span>");
        }
        sb.append("</html>");
        badgesLabel.setText(sb.toString());
        badgesLabel.setFont(Ui.SMALL);
    }

    // --------------------------------------------------------------- sidebar
    private JPanel sidebar() {
        JPanel s = Ui.gradientPanel(new Color(0x1A, 0x21, 0x44), new Color(0x12, 0x17, 0x31));
        s.setPreferredSize(new Dimension(232, 0));
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(new javax.swing.border.EmptyBorder(20, 14, 16, 14));

        JLabel section = new JLabel("  MENU");
        section.setFont(new Font("Segoe UI", Font.BOLD, 11));
        section.setForeground(new Color(0x6B, 0x75, 0x93));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(204, 28));
        s.add(section);
        s.add(Box.createVerticalStrut(10));

        User.Role role = ctx.auth.currentUser().role;
        String[][] items = switch (role) {
            case ADMIN -> new String[][]{
                    {"User Management", "admin-users"},
                    {"Quiz Content", "admin-quizzes"},
                    {"System Settings", "admin-settings"},
                    {"Performance Reports", "admin-reports"},
                    {"System Alerts", "admin-alerts"}
            };
            case CREATOR -> new String[][]{
                    {"Quiz Creation", "cr-quizzes"},
                    {"Quiz Results", "cr-results"},
                    {"Participant Interactions", "cr-messages"},
                    {"Quiz History", "cr-history"},
                    {"Performance Overview", "cr-overview"}
            };
            case PARTICIPANT -> new String[][]{
                    {"Take Quizzes", "p-quizzes"},
                    {"Participation History", "p-history"},
                    {"Performance Report", "p-performance"},
                    {"Interactions", "p-messages"},
                    {"Quiz Reminders", "p-reminders"},
                    {"Leaderboard", "p-leaderboard"}
            };
        };

        for (String[] item : items) {
            NavButton b = new NavButton(item[0], item[1]);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(204, 46));
            b.addActionListener(e -> showPage(item[1]));
            navButtons.add(b);
            s.add(b);
            s.add(Box.createVerticalStrut(6));
        }

        s.add(Box.createVerticalGlue());
        JLabel foot = new JLabel("<html><div style='text-align:center;font-size:10px'>Java-Based<br><b>Online Quiz Platform</b></div></html>");
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        foot.setForeground(new Color(0x6B, 0x75, 0x93));
        foot.setAlignmentX(Component.CENTER_ALIGNMENT);
        s.add(foot);
        return s;
    }

    private void showPage(String key) {
        for (NavButton b : navButtons) b.setActive(b.pageKey.equals(key));
        cards.show(content, key);
        refreshBadges();
        revalidate();
        repaint();
    }

    public void refreshCurrent() {
        refreshBadges();
    }

    // ----------------------------------------------------------------- pages
    private void addPages() {
        User.Role role = ctx.auth.currentUser().role;
        switch (role) {
            case ADMIN -> {
                addPage("admin-users", new UserManagementPanel(ctx));
                addPage("admin-quizzes", new QuizApprovalPanel(ctx));
                addPage("admin-settings", new SettingsPanel(ctx));
                addPage("admin-reports", new ReportsPanel(ctx));
                addPage("admin-alerts", new AlertsPanel(ctx));
            }
            case CREATOR -> {
                addPage("cr-quizzes", new MyQuizzesPanel(ctx));
                addPage("cr-results", new CreatorResultsPanel(ctx));
                addPage("cr-messages", new MessagesPanel(ctx));
                addPage("cr-history", new CreatorHistoryPanel(ctx));
                addPage("cr-overview", new CreatorOverviewPanel(ctx));
            }
            case PARTICIPANT -> {
                addPage("p-quizzes", new AvailableQuizzesPanel(ctx));
                addPage("p-history", new HistoryPanel(ctx));
                addPage("p-performance", new PerformancePanel(ctx));
                addPage("p-messages", new MessagesPanel(ctx));
                addPage("p-reminders", new RemindersPanel(ctx));
                addPage("p-leaderboard", new LeaderboardPanel(ctx));
            }
        }
    }

    private void addPage(String key, JPanel page) {
        content.add(page, key);
    }

    // ------------------------------------------------------------- nav button
    private static class NavButton extends JButton {
        final String pageKey;
        boolean active;

        NavButton(String text, String pageKey) {
            super(text);
            this.pageKey = pageKey;
            setFont(Ui.BODY);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(Ui.NAV_TEXT);
            setBorder(new javax.swing.border.EmptyBorder(12, 18, 12, 18));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

        void setActive(boolean v) {
            active = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (active) {
                g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                        new float[]{0f, 1f}, new Color[]{Ui.NAV_SEL, new Color(0x7C, 0x3A, 0xED)}));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                setFont(Ui.BOLD);
                setForeground(Ui.NAV_TEXT_ACTIVE);
            } else if (getModel().isRollover()) {
                g2.setColor(Ui.NAV_BG_LIGHT);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                setFont(Ui.BODY);
                setForeground(new Color(0xE0, 0xE7, 0xFF));
            } else {
                setFont(Ui.BODY);
                setForeground(Ui.NAV_TEXT);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}