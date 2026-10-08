package quizplatform.service;

import quizplatform.model.Alert;
import quizplatform.storage.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlertService {

    private final Repository repo;

    public AlertService(Repository repo) {
        this.repo = repo;
    }

    public void add(String type, String message) {
        synchronized (repo) {
            repo.alerts.add(new Alert(repo.nextId("a"), type, message));
            repo.saveAlerts();
        }
    }

    public List<Alert> all() {
        synchronized (repo) {
            List<Alert> out = new ArrayList<>(repo.alerts);
            out.sort(Comparator.comparingLong((Alert a) -> a.createdAt).reversed());
            if (out.size() > 200) return new ArrayList<>(out.subList(0, 200));
            return out;
        }
    }

    public int unreadCount() {
        synchronized (repo) {
            int n = 0;
            for (Alert a : repo.alerts) if (!a.read) n++;
            return n;
        }
    }

    public void markRead(String id) {
        synchronized (repo) {
            for (Alert a : repo.alerts) if (a.id.equals(id)) a.read = true;
            repo.saveAlerts();
        }
    }

    public void markAllRead() {
        synchronized (repo) {
            for (Alert a : repo.alerts) a.read = true;
            repo.saveAlerts();
        }
    }
}
