package quizplatform.storage;

import quizplatform.model.*;
import quizplatform.util.Hash;
import quizplatform.util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Loads and saves all application data as JSON files under ./data. */
public class Repository {

    private final Path dataDir;
    private final Path usersFile, settingsFile, messagesFile, remindersFile, alertsFile;
    private final Path quizzesDir, attemptsDir;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public List<User> users = new ArrayList<>();
    public List<Quiz> quizzes = new ArrayList<>();
    public List<Attempt> attempts = new ArrayList<>();
    public List<Message> messages = new ArrayList<>();
    public List<Reminder> reminders = new ArrayList<>();
    public List<Alert> alerts = new ArrayList<>();
    public SystemSettings settings = new SystemSettings();

    public Repository(Path dataDir) {
        this.dataDir = dataDir;
        this.usersFile = dataDir.resolve("users.json");
        this.settingsFile = dataDir.resolve("settings.json");
        this.messagesFile = dataDir.resolve("messages.json");
        this.remindersFile = dataDir.resolve("reminders.json");
        this.alertsFile = dataDir.resolve("alerts.json");
        this.quizzesDir = dataDir.resolve("quizzes");
        this.attemptsDir = dataDir.resolve("attempts");
    }

    // ------------------------------------------------------------------ load
    public void load() {
        try {
            Files.createDirectories(quizzesDir);
            Files.createDirectories(attemptsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data directory: " + dataDir, e);
        }

        users = readList(usersFile, User::fromJson);
        messages = readList(messagesFile, Message::fromJson);
        reminders = readList(remindersFile, Reminder::fromJson);
        alerts = readList(alertsFile, Alert::fromJson);

        if (Files.exists(settingsFile)) {
            try {
                settings = SystemSettings.fromJson(Json.parseObject(read(settingsFile)));
            } catch (Exception e) {
                settings = new SystemSettings();
            }
        }

        quizzes = new ArrayList<>();
        readDir(quizzesDir, q -> quizzes.add(Quiz.fromJson(Json.mapOf(q))));

        attempts = new ArrayList<>();
        readDir(attemptsDir, a -> attempts.add(Attempt.fromJson(Json.mapOf(a))));

        if (users.isEmpty()) {
            seed();
        }
    }

    private <T> List<T> readList(Path file, java.util.function.Function<Map<String, Object>, T> mapper) {
        List<T> out = new ArrayList<>();
        if (!Files.exists(file)) return out;
        try {
            Object root = Json.parse(read(file));
            if (root instanceof List<?> list) {
                for (Object o : list) if (o instanceof Map) out.add(mapper.apply(Json.mapOf(o)));
            }
        } catch (Exception e) {
            System.err.println("Warning: could not parse " + file + " (" + e.getMessage() + ")");
        }
        return out;
    }

    private void readDir(Path dir, java.util.function.Consumer<Object> consumer) {
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json")).sorted().forEach(p -> {
                try {
                    consumer.accept(Json.parse(read(p)));
                } catch (Exception e) {
                    System.err.println("Warning: could not parse " + p + " (" + e.getMessage() + ")");
                }
            });
        } catch (IOException e) {
            System.err.println("Warning: could not list " + dir);
        }
    }

    // ------------------------------------------------------------------ save
    private String read(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + p, e);
        }
    }

    private void write(Path p, String content) {
        try {
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
            Files.writeString(tmp, content, StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write " + p, e);
        }
    }

    private void writeList(Path p, List<? extends Object> items,
                           java.util.function.Function<Object, Map<String, Object>> mapper) {
        List<Object> out = new ArrayList<>();
        for (Object o : items) out.add(mapper.apply(o));
        write(p, Json.write(out));
    }

    public void saveUsers() {
        writeList(usersFile, users, u -> ((User) u).toJson());
    }

    public void saveSettings() {
        write(settingsFile, Json.write(settings.toJson()));
    }

    public void saveMessages() {
        writeList(messagesFile, messages, m -> ((Message) m).toJson());
    }

    public void saveReminders() {
        writeList(remindersFile, reminders, r -> ((Reminder) r).toJson());
    }

    public void saveAlerts() {
        writeList(alertsFile, alerts, a -> ((Alert) a).toJson());
    }

    public void saveQuiz(Quiz q) {
        write(quizzesDir.resolve(q.id + ".json"), Json.write(q.toJson()));
        if (!quizzes.contains(q)) quizzes.add(q);
    }

    public void deleteQuiz(String id) {
        quizzes.removeIf(q -> q.id.equals(id));
        try {
            Files.deleteIfExists(quizzesDir.resolve(id + ".json"));
        } catch (IOException ignored) {}
    }

    public void saveAttempt(Attempt a) {
        write(attemptsDir.resolve(a.id + ".json"), Json.write(a.toJson()));
        if (!attempts.contains(a)) attempts.add(a);
    }

    public void deleteAttempt(String id) {
        attempts.removeIf(a -> a.id.equals(id));
        try {
            Files.deleteIfExists(attemptsDir.resolve(id + ".json"));
        } catch (IOException ignored) {}
    }

    // ------------------------------------------------------------------ ids
    public String nextId(String prefix) {
        return prefix + "-" + Long.toHexString(System.currentTimeMillis()) + "-" + idCounter.getAndIncrement();
    }

    // -------------------------------------------------------------- lookups
    public User findUser(String id) {
        return users.stream().filter(u -> u.id.equals(id)).findFirst().orElse(null);
    }

    public User findUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.email.equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    public Quiz findQuiz(String id) {
        return quizzes.stream().filter(q -> q.id.equals(id)).findFirst().orElse(null);
    }

    public Attempt findAttempt(String id) {
        return attempts.stream().filter(a -> a.id.equals(id)).findFirst().orElse(null);
    }

    public List<Attempt> attemptsFor(String userId) {
        List<Attempt> out = new ArrayList<>();
        for (Attempt a : attempts) if (a.userId.equals(userId)) out.add(a);
        out.sort(Comparator.comparingLong(a -> a.submittedAt));
        return out;
    }

    public List<Attempt> attemptsOnQuiz(String quizId) {
        List<Attempt> out = new ArrayList<>();
        for (Attempt a : attempts) if (a.quizId.equals(quizId)) out.add(a);
        out.sort(Comparator.comparingLong(a -> a.submittedAt));
        return out;
    }

    public int attemptCount(String userId, String quizId) {
        int n = 0;
        for (Attempt a : attempts) if (a.userId.equals(userId) && a.quizId.equals(quizId)) n++;
        return n;
    }

    // ------------------------------------------------------------------ seed
    public void seed() {
        User admin = new User("u-admin", "System Admin", "admin@quiz.local",
                Hash.password("admin123"), User.Role.ADMIN);
        User creator = new User("u-creator", "Carla Creator", "creator@quiz.local",
                Hash.password("creator123"), User.Role.CREATOR);
        User alice = new User("u-alice", "Alice Kumar", "alice@quiz.local",
                Hash.password("pass123"), User.Role.PARTICIPANT);
        User bob = new User("u-bob", "Bob Sharma", "bob@quiz.local",
                Hash.password("pass123"), User.Role.PARTICIPANT);
        users.addAll(List.of(admin, creator, alice, bob));
        saveUsers();

        Quiz basics = quiz("Java Basics", "Core syntax, variables, loops and methods.",
                "Fundamentals", 10, creator, Quiz.Status.APPROVED);
        basics.questions.add(q("Which keyword is used to define a class in Java?", List.of("class", "struct", "define", "object"), 0));
        basics.questions.add(q("What is the size of an int in Java?", List.of("2 bytes", "4 bytes", "8 bytes", "Depends on JVM"), 1));
        basics.questions.add(q("Which of these is NOT a Java primitive type?", List.of("boolean", "char", "String", "double"), 2));
        basics.questions.add(q("What does the 'static' keyword mean?", List.of("Instance bound", "Class level", "Private", "Final"), 1));
        basics.questions.add(q("Which loop guarantees at least one execution?", List.of("for", "while", "do-while", "foreach"), 2));
        saveQuiz(basics);

        Quiz oop = quiz("OOP in Java", "Encapsulation, inheritance, polymorphism and interfaces.",
                "OOP", 8, creator, Quiz.Status.APPROVED);
        oop.questions.add(q("Which OOP principle hides internal state?", List.of("Inheritance", "Polymorphism", "Encapsulation", "Abstraction"), 2));
        oop.questions.add(q("A class that cannot be instantiated is called:", List.of("Abstract class", "Final class", "Static class", "Sealed class"), 0));
        oop.questions.add(q("Method overriding occurs in:", List.of("Same class", "Parent-child classes", "Interfaces only", "Packages"), 1));
        oop.questions.add(q("Which access modifier is most restrictive?", List.of("public", "protected", "default", "private"), 3));
        saveQuiz(oop);

        Quiz pending = quiz("Collections & Generics", "Lists, maps, sets and generic types.",
                "Collections", 12, creator, Quiz.Status.PENDING);
        pending.questions.add(q("Which collection does not allow duplicates?", List.of("ArrayList", "LinkedList", "HashSet", "Vector"), 2));
        pending.questions.add(q("Which interface maps keys to values?", List.of("List", "Set", "Map", "Queue"), 2));
        pending.questions.add(q("What does List<? extends Number> denote?", List.of("Wildcard", "Generic method", "Raw type", "Array"), 0));
        saveQuiz(pending);

        long now = System.currentTimeMillis();
        saveAttempt(attempt(basics, alice, new int[]{0, 1, 2, 1, 2}, now - 86_400_000 * 6, "Excellent grasp of the fundamentals."));
        saveAttempt(attempt(basics, bob, new int[]{0, 1, 3, 1, 0}, now - 86_400_000 * 5, "Revise primitive types and loops."));
        saveAttempt(attempt(oop, alice, new int[]{2, 0, 1, 3}, now - 86_400_000 * 3, "Very strong understanding of OOP."));
        saveAttempt(attempt(oop, bob, new int[]{2, 1, 1, 1}, now - 86_400_000 * 2, "Good effort - revisit access modifiers."));

        Message msg = new Message();
        msg.id = nextId("m");
        msg.fromUserId = alice.id;
        msg.fromName = alice.name;
        msg.fromRole = "PARTICIPANT";
        msg.toUserId = creator.id;
        msg.toName = creator.name;
        msg.text = "When will the Collections & Generics quiz be approved?";
        msg.sentAt = now - 3_600_000;
        messages.add(msg);
        saveMessages();

        Reminder rem = new Reminder();
        rem.id = nextId("r");
        rem.userId = alice.id;
        rem.quizId = oop.id;
        rem.quizTitle = oop.title;
        rem.note = "Re-attempt before the deadline";
        rem.remindAt = now + 86_400_000 * 2;
        reminders.add(rem);
        saveReminders();

        alerts.add(new Alert(nextId("a"), "INFO", "Sample data has been seeded for demonstration."));
        saveAlerts();
        saveSettings();
    }

    private Quiz quiz(String title, String desc, String topic, int mins, User creator, Quiz.Status status) {
        Quiz q = new Quiz();
        q.id = nextId("q");
        q.title = title;
        q.description = desc;
        q.topic = topic;
        q.durationMins = mins;
        q.creatorId = creator.id;
        q.creatorName = creator.name;
        q.status = status;
        q.createdAt = System.currentTimeMillis();
        if (status == Quiz.Status.PENDING) q.submittedAt = System.currentTimeMillis();
        return q;
    }

    private Question q(String text, List<String> opts, int correct) {
        return new Question(text, new ArrayList<>(opts), correct, 1);
    }

    private Attempt attempt(Quiz quiz, User user, int[] answers, long submittedAt, String feedback) {
        Attempt a = new Attempt();
        a.id = nextId("at");
        a.quizId = quiz.id;
        a.quizTitle = quiz.title;
        a.userId = user.id;
        a.userName = user.name;
        int score = 0, max = 0;
        for (int i = 0; i < quiz.questions.size(); i++) {
            Question question = quiz.questions.get(i);
            max += question.points;
            a.answers.add(i < answers.length ? answers[i] : -1);
            if (i < answers.length && answers[i] == question.correctIndex) score += question.points;
        }
        a.score = score;
        a.maxScore = max;
        a.startedAt = submittedAt - 420_000;
        a.submittedAt = submittedAt;
        a.timeTakenSec = 420;
        a.graded = true;
        a.feedback = feedback;
        return a;
    }
}
