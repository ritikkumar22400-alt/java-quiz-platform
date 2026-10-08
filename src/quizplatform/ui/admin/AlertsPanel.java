package quizplatform.ui.admin;

import quizplatform.model.Alert;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

public class AlertsPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Time", "Type", "Message", "Status");
    private final JTable table = Ui.table(model);
    private List<Alert> rows = List.of();

    public AlertsPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("System Alerts",
                "Notifications for system issues, updates and pending work.", buildBody()),
                BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton mark = Ui.primary("Mark Selected Read");
        JButton markAll = Ui.neutral("Mark All Read");
        JButton refreshBtn = Ui.neutral("Refresh");
        toolbar.add(mark);
        toolbar.add(markAll);
        toolbar.add(refreshBtn);

        mark.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0 || r >= rows.size()) {
                Ui.info(this, "Select an alert first.");
                return;
            }
            ctx.alerts.markRead(rows.get(r).id);
            refresh();
        });
        markAll.addActionListener(e -> {
            ctx.alerts.markAllRead();
            Ui.success(this, "All alerts marked as read.");
            refresh();
        });
        refreshBtn.addActionListener(e -> refresh());

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && row < rows.size()) {
                    Alert a = rows.get(row);
                    c.setBackground(a.read ? Color.WHITE : new Color(0xFF, 0xF7, 0xED));
                    setForeground(a.read ? Ui.MUTED : Ui.TEXT);
                }
                return c;
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.add(Ui.scroll(table), BorderLayout.CENTER);
        JLabel hint = Ui.muted("Alerts are generated automatically for new users, quiz submissions, approvals, rejections and attempts.");
        wrap.add(hint, BorderLayout.SOUTH);
        body.add(wrap, BorderLayout.CENTER);
        return body;
    }

    public void refresh() {
        rows = ctx.alerts.all();
        model.setRowCount(0);
        for (Alert a : rows) {
            model.addRow(Ui.row(Ui.date(a.createdAt), a.type, a.message, a.read ? "Read" : "Unread"));
        }
    }
}
