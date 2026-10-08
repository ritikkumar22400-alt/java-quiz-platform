package quizplatform.service;

import quizplatform.model.Reminder;
import quizplatform.storage.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReminderService {

    private final Repository repo;

    public ReminderService(Repository repo) {
        this.repo = repo;
    }

    public Reminder add(String userId, String quizId, String quizTitle, String note, long remindAt) {
        if (remindAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Reminder time must be in the future.");
        }
        synchronized (repo) {
            Reminder r = new Reminder();
            r.id = repo.nextId("r");
            r.userId = userId;
            r.quizId = quizId;
            r.quizTitle = quizTitle;
            r.note = note == null ? "" : note.trim();
            r.remindAt = remindAt;
            repo.reminders.add(r);
            repo.saveReminders();
            return r;
        }
    }

    public List<Reminder> forUser(String userId) {
        synchronized (repo) {
            List<Reminder> out = new ArrayList<>();
            for (Reminder r : repo.reminders) if (r.userId.equals(userId)) out.add(r);
            out.sort(Comparator.comparingLong(r -> r.remindAt));
            return out;
        }
    }

    public void delete(String id) {
        synchronized (repo) {
            repo.reminders.removeIf(r -> r.id.equals(id));
            repo.saveReminders();
        }
    }
}
