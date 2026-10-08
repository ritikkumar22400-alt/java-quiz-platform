package quizplatform.ui.admin;

import quizplatform.model.User;
import quizplatform.service.AppContext;
import quizplatform.service.UserService;
import quizplatform.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final AppContext ctx;
    private final javax.swing.table.DefaultTableModel model =
            Ui.tableModel("Name", "Email", "Role", "Status", "Created");
    private final JTable table = Ui.table(model);
    private List<User> rows = List.of();

    public UserManagementPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("User Management", "Create, update and delete user accounts and roles.",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton add = Ui.primary("Add User");
        JButton edit = Ui.neutral("Edit");
        JButton del = Ui.danger("Delete");
        JButton refreshBtn = Ui.neutral("Refresh");
        left.add(add);
        left.add(edit);
        left.add(del);
        left.add(refreshBtn);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(Ui.muted("Double-click a row to edit"), BorderLayout.EAST);

        add.addActionListener(e -> showUserDialog(null));
        edit.addActionListener(e -> {
            User u = selected();
            if (u != null) showUserDialog(u);
        });
        del.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refresh());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    User u = selected();
                    if (u != null) showUserDialog(u);
                }
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(toolbar, BorderLayout.NORTH);
        body.add(Ui.scroll(table), BorderLayout.CENTER);
        return body;
    }

    private User selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) {
            Ui.info(this, "Select a user first.");
            return null;
        }
        return rows.get(r);
    }

    private void deleteSelected() {
        User u = selected();
        if (u == null) return;
        if (!Ui.confirm(this, "Delete user \"" + u.name + "\" (" + u.email + ")?")) return;
        try {
            ctx.users.delete(u.id);
            Ui.success(this, "User deleted successfully: " + u.name);
            refresh();
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void showUserDialog(User existing) {
        JTextField name = Ui.field(20);
        JTextField email = Ui.field(20);
        JTextField password = Ui.field(20);
        JComboBox<String> role = Ui.combo(new String[]{"Participant", "Quiz Creator", "Admin"});
        JCheckBox active = new JCheckBox("Account active", true);
        active.setFont(Ui.BODY);

        if (existing != null) {
            name.setText(existing.name);
            email.setText(existing.email);
            role.setSelectedIndex(existing.role.ordinal());
            active.setSelected(existing.active);
            password.setToolTipText("Leave blank to keep current password");
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(Ui.label("Name"));
        form.add(name);
        form.add(Ui.label("Email"));
        form.add(email);
        form.add(Ui.label(existing == null ? "Password (min 6 chars)"
                : "New password (blank = keep current)"));
        form.add(password);
        form.add(Ui.label("Role"));
        form.add(role);
        form.add(active);

        int res = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add User" : "Edit User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            User.Role r = switch (role.getSelectedIndex()) {
                case 1 -> User.Role.CREATOR;
                case 2 -> User.Role.ADMIN;
                default -> User.Role.PARTICIPANT;
            };
            if (existing == null) {
                User u = ctx.users.create(name.getText(), email.getText(), password.getText(), r);
                Ui.success(this, "User created successfully: " + u.name + " (" + UserService.roleLabel(r) + ")");
            } else {
                ctx.users.update(existing.id, name.getText(), email.getText(), r,
                        active.isSelected(), password.getText());
                Ui.success(this, "User updated successfully: " + name.getText());
            }
            refresh();
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    public void refresh() {
        rows = ctx.users.list();
        model.setRowCount(0);
        for (User u : rows) {
            model.addRow(Ui.row(u.name, u.email, UserService.roleLabel(u.role),
                    u.active ? "Active" : "Disabled", Ui.dateShort(u.createdAt)));
        }
    }
}
