package quizplatform;

import quizplatform.model.Attempt;
import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.model.Reminder;
import quizplatform.model.User;
import quizplatform.service.AppContext;
import quizplatform.service.LeaderboardService;
import quizplatform.storage.Repository;
import quizplatform.util.Json;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SmokeTest {

    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) { passed++; System.out.println("  OK   " + name); }
        else { failed++; System.out.println("  FAIL " + name); }
    }

    public static void main(String[] args) {
        Repository repo = new Repository(Path.of("data"));
        repo.load();
        AppContext ctx = new AppContext(repo);

        System.out.println("[storage]");
        check("seeded users", repo.users.size() >= 4);
        check("seeded quizzes", repo.quizzes.size() >= 3);
        check("seeded attempts", repo.attempts.size() >= 4);
        check("JSON round-trip", Json.write(Json.parse("{\"a\":[1,\"x\",true,null]}"))
                .contains("\"a\""));

        System.out.println("[auth]");
        User admin = ctx.auth.login("admin@quiz.local", "admin123");
        check("admin login", admin.role == User.Role.ADMIN);
        boolean threw = false;
        try { ctx.auth.login("admin@quiz.local", "wrong"); } catch (IllegalArgumentException e) { threw = true; }
        check("bad password rejected", threw);
        threw = false;
        try { ctx.auth.login("ghost@quiz.local", "x"); } catch (IllegalArgumentException e) { threw = true; }
        check("unknown email rejected", threw);
        ctx.auth.logout();

        System.out.println("[users]");
        User u = ctx.users.create("Test User", "test@quiz.local", "secret1", User.Role.PARTICIPANT);
        check("user created", ctx.repo.findUserByEmail("test@quiz.local") != null);
        threw = false;
        try { ctx.users.create("Dup", "test@quiz.local", "secret1", User.Role.PARTICIPANT); }
        catch (IllegalArgumentException e) { threw = true; }
        check("duplicate email rejected", threw);
        ctx.users.update(u.id, "Test User Jr", "test2@quiz.local", User.Role.CREATOR, true, "");
        check("user updated", ctx.repo.findUser(u.id).name.equals("Test User Jr")
                && ctx.repo.findUser(u.id).role == User.Role.CREATOR);
        ctx.users.delete(u.id);
        check("user deleted", ctx.repo.findUser(u.id) == null);

        System.out.println("[quiz workflow]");
        User creator = ctx.auth.login("creator@quiz.local", "creator123");
        List<Question> qs = new ArrayList<>();
        qs.add(new Question("2+2 = ?", List.of("3", "4", "5", "6"), 1, 2));
        qs.add(new Question("Capital of France?", List.of("Berlin", "Paris", "Rome", "Madrid"), 1, 3));
        Quiz quiz = ctx.quizzes.create(creator, "Smoke Quiz", "desc", "Testing", 5, qs);
        check("quiz created as draft", quiz.status == Quiz.Status.DRAFT);
        threw = false;
        try { ctx.quizzes.submitForApproval(quiz); ctx.quizzes.submitForApproval(quiz); }
        catch (IllegalArgumentException e) { threw = true; }
        check("double submit rejected", threw);
        ctx.auth.logout();

        ctx.auth.login("admin@quiz.local", "admin123");
        ctx.quizzes.approve(quiz);
        check("quiz approved", quiz.status == Quiz.Status.APPROVED);
        Quiz q2 = ctx.quizzes.create(creator, "Rejection Quiz", "", "T", 5, qs);
        ctx.quizzes.submitForApproval(q2);
        ctx.quizzes.reject(q2, "Poor quality");
        check("quiz rejected with reason", q2.status == Quiz.Status.REJECTED
                && q2.reviewNote.contains("Poor"));

        System.out.println("[attempt]");
        User alice = ctx.auth.login("alice@quiz.local", "pass123");
        check("can attempt approved quiz", ctx.attempts.canAttempt(alice, quiz) == null);
        check("cannot attempt rejected quiz", ctx.attempts.canAttempt(alice, q2) != null);
        Attempt a = ctx.attempts.submit(alice, quiz, List.of(1, 1), System.currentTimeMillis() - 60_000);
        check("perfect score", a.score == 5 && a.maxScore == 5);
        Attempt a2 = ctx.attempts.submit(alice, quiz, List.of(0, 0), System.currentTimeMillis() - 30_000);
        check("zero score", a2.score == 0);

        System.out.println("[grading]");
        ctx.attempts.grade(a2, 3, "Partial credit awarded");
        check("graded with feedback", ctx.repo.findAttempt(a2.id).score == 3
                && ctx.repo.findAttempt(a2.id).feedback.contains("Partial"));

        System.out.println("[messages]");
        User creator2 = ctx.repo.findUser("u-creator");
        ctx.messages.send(alice, creator2, "Hello creator!");
        check("message sent", ctx.messages.unreadCount(creator2) >= 1);
        check("conversation found", ctx.messages.conversation(alice, creator2).size() >= 1);
        ctx.messages.markIncomingRead(creator2, alice.id);
        check("marked read", ctx.messages.unreadCount(creator2) == 0);

        System.out.println("[reminders]");
        Reminder r = ctx.reminders.add(alice.id, quiz.id, quiz.title, "note",
                System.currentTimeMillis() + 1000_000);
        check("reminder added", ctx.reminders.forUser(alice.id).size() >= 1);
        threw = false;
        try { ctx.reminders.add(alice.id, quiz.id, quiz.title, "", System.currentTimeMillis() - 1000); }
        catch (IllegalArgumentException e) { threw = true; }
        check("past reminder rejected", threw);
        ctx.reminders.delete(r.id);
        check("reminder deleted", ctx.reminders.forUser(alice.id).stream().noneMatch(x -> x.id.equals(r.id)));

        System.out.println("[reports & leaderboard]");
        check("overview computed", ctx.reports.overview(alice.id).attempts >= 2);
        check("avg per quiz", !ctx.reports.averageScorePerQuiz().isEmpty());
        check("pass/fail split", ctx.reports.passFailSplit().size() == 2);
        check("14-day trend", ctx.reports.attemptsPerDay(14).size() == 14);
        check("result rows", ctx.reports.resultRows(alice.id).size() >= 2);
        List<LeaderboardService.Row> board = ctx.leaderboard.ranked(alice.id);
        check("leaderboard has rows", !board.isEmpty());
        check("current user marked", board.stream().anyMatch(x -> x.isCurrentUser));

        System.out.println("[alerts & settings]");
        check("alerts generated", ctx.alerts.all().size() >= 3);
        ctx.alerts.markAllRead();
        check("alerts read", ctx.alerts.unreadCount() == 0);
        ctx.repo.settings.passPercentage = 75;
        ctx.repo.saveSettings();
        Repository reloaded = new Repository(Path.of("data"));
        reloaded.load();
        check("settings persisted", reloaded.settings.passPercentage == 75);
        reloaded.settings.passPercentage = 60;
        reloaded.saveSettings();

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}