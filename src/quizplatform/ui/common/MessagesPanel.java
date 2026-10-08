package quizplatform.ui.common;

import quizplatform.model.Message;
import quizplatform.model.User;
import quizplatform.service.AppContext;
import quizplatform.ui.Ui;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Interaction panel shared by Quiz Creators, Participants and Admins. */
public class MessagesPanel extends JPanel {

    private final AppContext ctx;
    private final DefaultListModel<String> contactModel = new DefaultListModel<>();
    private final JList<String> contactList = new JList<>(contactModel);
    private final JComboBox<String> newChatCombo = new JComboBox<>();
    private final JTextArea conversation = new JTextArea();
    private final JTextField input = Ui.field(30);

    private List<User> contactUsers = new ArrayList<>();
    private List<User> selectable = new ArrayList<>();
    private User active;

    public MessagesPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 14));
        setBackground(Ui.BG);
        add(Ui.page("Participant Interactions",
                "Communicate with quiz participants and creators.", buildBody()), BorderLayout.CENTER);
        reloadContacts();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);

        // left
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setPreferredSize(new Dimension(260, 0));
        left.setOpaque(false);
        JLabel head = Ui.label("Contacts");
        left.add(head, BorderLayout.NORTH);

        JPanel leftInner = new JPanel(new BorderLayout(0, 8));
        leftInner.setOpaque(false);
        leftInner.setBackground(Color.WHITE);
        leftInner.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        contactList.setFont(Ui.BODY);
        contactList.setBackground(Color.WHITE);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(34);
        contactList.setSelectionBackground(new Color(0xE0, 0xE7, 0xFF));
        contactList.setSelectionForeground(Ui.PRIMARY_DARK);
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && contactList.getSelectedIndex() >= 0) {
                active = contactUsers.get(contactList.getSelectedIndex());
                loadConversation();
            }
        });
        JScrollPane clScroll = new JScrollPane(contactList);
        clScroll.setBorder(null);
        clScroll.getViewport().setBackground(Color.WHITE);
        leftInner.add(clScroll, BorderLayout.CENTER);

        JPanel start = new JPanel(new BorderLayout(0, 6));
        start.setOpaque(false);
        start.add(Ui.muted("Start a new conversation:"), BorderLayout.NORTH);
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        newChatCombo.setFont(Ui.SMALL);
        row.add(newChatCombo, BorderLayout.CENTER);
        JButton startBtn = Ui.primary("Chat");
        row.add(startBtn, BorderLayout.EAST);
        start.add(row, BorderLayout.CENTER);
        leftInner.add(start, BorderLayout.SOUTH);
        left.add(leftInner, BorderLayout.CENTER);

        startBtn.addActionListener(e -> {
            int i = newChatCombo.getSelectedIndex();
            if (i >= 0 && i < selectable.size()) {
                active = selectable.get(i);
                if (!contactUsers.contains(active)) {
                    contactUsers.add(0, active);
                    contactModel.add(0, displayName(active));
                }
                contactList.setSelectedIndex(0);
                loadConversation();
            }
        });

        // right
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);

        JPanel rightInner = new JPanel(new BorderLayout(0, 8));
        rightInner.setOpaque(false);
        rightInner.setBackground(Color.WHITE);
        rightInner.setBorder(new javax.swing.border.EmptyBorder(12, 12, 12, 12));
        JLabel activeLabel = Ui.subheading("Select a contact to view the conversation.");
        rightInner.add(activeLabel, BorderLayout.NORTH);

        conversation.setEditable(false);
        conversation.setFont(Ui.BODY);
        conversation.setLineWrap(true);
        conversation.setWrapStyleWord(true);
        conversation.setMargin(new java.awt.Insets(12, 12, 12, 12));
        JScrollPane convScroll = new JScrollPane(conversation);
        convScroll.setBorder(null);
        rightInner.add(convScroll, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        input.addActionListener(e -> send());
        inputRow.add(input, BorderLayout.CENTER);
        JButton send = Ui.primary("Send");
        JButton refresh = Ui.neutral("Refresh");
        inputRow.add(send, BorderLayout.EAST);
        inputRow.add(refresh, BorderLayout.EAST);
        rightInner.add(inputRow, BorderLayout.SOUTH);
        send.addActionListener(e -> send());
        refresh.addActionListener(e -> {
            reloadContacts();
            if (active != null) loadConversation();
        });
        right.add(rightInner, BorderLayout.CENTER);

        body.add(Ui.card(left), BorderLayout.WEST);
        body.add(Ui.card(right), BorderLayout.CENTER);
        return body;
    }

    private void send() {
        try {
            if (active == null) throw new IllegalArgumentException("Select a contact first.");
            ctx.messages.send(ctx.auth.currentUser(), active, input.getText());
            input.setText("");
            loadConversation();
        } catch (IllegalArgumentException ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void loadConversation() {
        if (active == null) return;
        User me = ctx.auth.currentUser();
        ctx.messages.markIncomingRead(me, active.id);
        List<Message> msgs = ctx.messages.conversation(me, active);
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, HH:mm");
        StringBuilder sb = new StringBuilder();
        for (Message m : msgs) {
            boolean mine = m.fromUserId.equals(me.id);
            sb.append(mine ? "You" : m.fromName).append("  (").append(fmt.format(new Date(m.sentAt))).append(")\n");
            sb.append(m.text).append("\n\n");
        }
        conversation.setText(sb.length() == 0 ? "No messages yet. Say hello!" : sb.toString());
        conversation.setCaretPosition(conversation.getDocument().getLength());
    }

    private void reloadContacts() {
        User me = ctx.auth.currentUser();
        List<User> all = ctx.users.list();
        selectable = new ArrayList<>();
        for (User u : all) {
            if (u.id.equals(me.id) || !u.active) continue;
            boolean allowed = switch (me.role) {
                case PARTICIPANT -> u.role == User.Role.CREATOR || u.role == User.Role.ADMIN;
                case CREATOR -> u.role == User.Role.PARTICIPANT;
                case ADMIN -> true;
            };
            if (allowed) selectable.add(u);
        }
        contactUsers = ctx.messages.contacts(me, selectable);

        contactModel.clear();
        for (User u : contactUsers) contactModel.addElement(displayName(u));

        newChatCombo.removeAllItems();
        for (User u : selectable) {
            if (!contactUsers.contains(u)) newChatCombo.addItem(displayName(u));
        }
    }

    private String displayName(User u) {
        return u.name + "  (" + u.role.name().toLowerCase() + ")";
    }
}
