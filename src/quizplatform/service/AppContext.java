package quizplatform.service;

import quizplatform.storage.Repository;

import java.util.Comparator;
import java.util.List;

public class AppContext {

    public final Repository repo;
    public final AuthService auth;
    public final UserService users;
    public final QuizService quizzes;
    public final AttemptService attempts;
    public final MessageService messages;
    public final ReminderService reminders;
    public final AlertService alerts;
    public final ReportService reports;
    public final LeaderboardService leaderboard;

    public AppContext(Repository repo) {
        this.repo = repo;
        this.auth = new AuthService(repo);
        this.users = new UserService(repo);
        this.quizzes = new QuizService(repo);
        this.attempts = new AttemptService(repo);
        this.messages = new MessageService(repo);
        this.reminders = new ReminderService(repo);
        this.alerts = new AlertService(repo);
        this.reports = new ReportService(repo);
        this.leaderboard = new LeaderboardService(repo);
    }
}
