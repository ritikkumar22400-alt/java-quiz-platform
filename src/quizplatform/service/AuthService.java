package quizplatform.service;

import quizplatform.model.User;
import quizplatform.storage.Repository;
import quizplatform.util.Hash;

public class AuthService {

    private final Repository repo;
    private User currentUser;

    public AuthService(Repository repo) {
        this.repo = repo;
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required.");
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password is required.");
        synchronized (repo) {
            User u = repo.findUserByEmail(email.trim());
            if (u == null || !Hash.matches(password, u.passwordHash)) {
                throw new IllegalArgumentException("Invalid email or password.");
            }
            if (!u.active) throw new IllegalArgumentException("This account has been deactivated. Contact an administrator.");
            currentUser = u;
            return u;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User currentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.role == User.Role.ADMIN;
    }

    public boolean isCreator() {
        return currentUser != null && currentUser.role == User.Role.CREATOR;
    }

    public boolean isParticipant() {
        return currentUser != null && currentUser.role == User.Role.PARTICIPANT;
    }
}
