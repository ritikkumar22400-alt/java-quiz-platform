package quizplatform.service;

import quizplatform.model.Message;
import quizplatform.model.User;
import quizplatform.storage.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessageService {

    private final Repository repo;

    public MessageService(Repository repo) {
        this.repo = repo;
    }

    public void send(User from, User to, String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Message cannot be empty.");
        if (to == null) throw new IllegalArgumentException("Choose a recipient.");
        if (to.id.equals(from.id)) throw new IllegalArgumentException("You cannot message yourself.");
        synchronized (repo) {
            Message m = new Message();
            m.id = repo.nextId("m");
            m.fromUserId = from.id;
            m.fromName = from.name;
            m.fromRole = from.role.name();
            m.toUserId = to.id;
            m.toName = to.name;
            m.text = text.trim();
            m.sentAt = System.currentTimeMillis();
            repo.messages.add(m);
            repo.saveMessages();
        }
    }

    /** All messages sent or received by the user, newest first. */
    public List<Message> forUser(User user) {
        synchronized (repo) {
            List<Message> out = new ArrayList<>();
            for (Message m : repo.messages) {
                if (m.fromUserId.equals(user.id) || m.toUserId.equals(user.id)) out.add(m);
            }
            out.sort(Comparator.comparingLong((Message m) -> m.sentAt).reversed());
            return out;
        }
    }

    /** Conversation between two users in chronological order. */
    public List<Message> conversation(User a, User b) {
        synchronized (repo) {
            List<Message> out = new ArrayList<>();
            for (Message m : repo.messages) {
                boolean match = (m.fromUserId.equals(a.id) && m.toUserId.equals(b.id))
                        || (m.fromUserId.equals(b.id) && m.toUserId.equals(a.id));
                if (match) out.add(m);
            }
            out.sort(Comparator.comparingLong(m -> m.sentAt));
            return out;
        }
    }

    public void markIncomingRead(User user, String otherId) {
        synchronized (repo) {
            boolean changed = false;
            for (Message m : repo.messages) {
                if (m.toUserId.equals(user.id) && m.fromUserId.equals(otherId) && !m.read) {
                    m.read = true;
                    changed = true;
                }
            }
            if (changed) repo.saveMessages();
        }
    }

    public int unreadCount(User user) {
        synchronized (repo) {
            int n = 0;
            for (Message m : repo.messages) {
                if (m.toUserId.equals(user.id) && !m.read) n++;
            }
            return n;
        }
    }

    /** Users this person has exchanged messages with. */
    public List<User> contacts(User user, List<User> allUsers) {
        List<String> ids = new ArrayList<>();
        for (Message m : forUser(user)) {
            String other = m.fromUserId.equals(user.id) ? m.toUserId : m.fromUserId;
            if (!ids.contains(other) && !other.equals(user.id)) ids.add(other);
        }
        List<User> out = new ArrayList<>();
        for (String id : ids) {
            for (User u : allUsers) if (u.id.equals(id)) out.add(u);
        }
        out.sort(Comparator.comparing(u -> u.name.toLowerCase()));
        return out;
    }
}
