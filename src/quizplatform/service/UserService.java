package quizplatform.service;

import quizplatform.model.Alert;
import quizplatform.model.User;
import quizplatform.storage.Repository;
import quizplatform.util.Hash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserService {

    private final Repository repo;

    public UserService(Repository repo) {
        this.repo = repo;
    }

    public List<User> list() {
        synchronized (repo) {
            List<User> out = new ArrayList<>(repo.users);
            out.sort(Comparator.comparing(u -> u.name.toLowerCase()));
            return out;
        }
    }

    public User create(String name, String email, String password, User.Role role) {
        validate(name, email, password, role, null);
        synchronized (repo) {
            User u = new User(repo.nextId("u"), name.trim(), email.trim().toLowerCase(),
                    Hash.password(password), role);
            repo.users.add(u);
            repo.saveUsers();
            repo.alerts.add(new Alert(repo.nextId("a"), "SUCCESS",
                    "User created: " + u.name + " (" + roleLabel(u.role) + ")"));
            repo.saveAlerts();
            return u;
        }
    }

    public void update(String id, String name, String email, User.Role role,
                       boolean active, String newPassword) {
        validate(name, email, newPassword == null || newPassword.isEmpty() ? "skip" : newPassword, role, id);
        synchronized (repo) {
            User u = repo.findUser(id);
            if (u == null) throw new IllegalArgumentException("User not found.");
            if (u.role == User.Role.ADMIN && role != User.Role.ADMIN && countAdmins() <= 1) {
                throw new IllegalArgumentException("At least one admin account must remain.");
            }
            if (u.role == User.Role.ADMIN && !active && countAdmins() <= 1) {
                throw new IllegalArgumentException("At least one active admin account must remain.");
            }
            if (!u.email.equalsIgnoreCase(email.trim()) && repo.findUserByEmail(email.trim()) != null) {
                throw new IllegalArgumentException("Email is already in use.");
            }
            u.name = name.trim();
            u.email = email.trim().toLowerCase();
            u.role = role;
            u.active = active;
            if (newPassword != null && !newPassword.isEmpty()) {
                u.passwordHash = Hash.password(newPassword);
            }
            repo.saveUsers();
            repo.alerts.add(new Alert(repo.nextId("a"), "INFO", "User updated: " + u.name));
            repo.saveAlerts();
        }
    }

    public void delete(String id) {
        synchronized (repo) {
            User u = repo.findUser(id);
            if (u == null) throw new IllegalArgumentException("User not found.");
            if (u.role == User.Role.ADMIN && countAdmins() <= 1) {
                throw new IllegalArgumentException("Cannot delete the last admin account.");
            }
            repo.users.remove(u);
            repo.saveUsers();
            repo.alerts.add(new Alert(repo.nextId("a"), "WARNING", "User deleted: " + u.name));
            repo.saveAlerts();
        }
    }

    private int countAdmins() {
        int n = 0;
        for (User u : repo.users) if (u.role == User.Role.ADMIN) n++;
        return n;
    }

    private void validate(String name, String email, String password, User.Role role, String selfId) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Name must be at least 2 characters.");
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password != null && !password.equals("skip") && password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (role == null) throw new IllegalArgumentException("Role is required.");
        synchronized (repo) {
            User existing = repo.findUserByEmail(email.trim());
            if (existing != null && !existing.id.equals(selfId)) {
                throw new IllegalArgumentException("Email is already in use.");
            }
        }
    }

    public static String roleLabel(User.Role role) {
        return switch (role) {
            case ADMIN -> "Admin";
            case CREATOR -> "Quiz Creator";
            case PARTICIPANT -> "Participant";
        };
    }
}
