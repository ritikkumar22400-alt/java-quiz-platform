package quizplatform.service;

import quizplatform.model.Alert;
import quizplatform.model.Question;
import quizplatform.model.Quiz;
import quizplatform.model.User;
import quizplatform.storage.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuizService {

    private final Repository repo;

    public QuizService(Repository repo) {
        this.repo = repo;
    }

    public List<Quiz> all() {
        synchronized (repo) {
            List<Quiz> out = new ArrayList<>(repo.quizzes);
            out.sort(Comparator.comparingLong((Quiz q) -> q.createdAt).reversed());
            return out;
        }
    }

    public List<Quiz> byCreator(String creatorId) {
        List<Quiz> out = new ArrayList<>();
        for (Quiz q : all()) if (q.creatorId.equals(creatorId)) out.add(q);
        return out;
    }

    public List<Quiz> approved() {
        List<Quiz> out = new ArrayList<>();
        for (Quiz q : all()) if (q.status == Quiz.Status.APPROVED) out.add(q);
        return out;
    }

    public Quiz create(User creator, String title, String description, String topic,
                       int durationMins, List<Question> questions) {
        validate(title, durationMins, questions);
        synchronized (repo) {
            Quiz q = new Quiz();
            q.id = repo.nextId("q");
            q.title = title.trim();
            q.description = description == null ? "" : description.trim();
            q.topic = topic == null || topic.isBlank() ? "General" : topic.trim();
            q.durationMins = durationMins;
            q.creatorId = creator.id;
            q.creatorName = creator.name;
            q.status = Quiz.Status.DRAFT;
            q.createdAt = System.currentTimeMillis();
            q.questions = questions;
            repo.saveQuiz(q);
            repo.alerts.add(new Alert(repo.nextId("a"), "INFO", "Quiz created: " + q.title));
            repo.saveAlerts();
            return q;
        }
    }

    public void update(Quiz quiz, String title, String description, String topic,
                       int durationMins, List<Question> questions) {
        validate(title, durationMins, questions);
        synchronized (repo) {
            if (quiz.status == Quiz.Status.PENDING || quiz.status == Quiz.Status.APPROVED) {
                throw new IllegalArgumentException("Editing is only allowed while the quiz is Draft or Rejected.");
            }
            quiz.title = title.trim();
            quiz.description = description == null ? "" : description.trim();
            quiz.topic = topic == null || topic.isBlank() ? "General" : topic.trim();
            quiz.durationMins = durationMins;
            quiz.questions = questions;
            repo.saveQuiz(quiz);
        }
    }

    public void delete(Quiz quiz) {
        synchronized (repo) {
            if (quiz.status == Quiz.Status.APPROVED && !repo.attemptsOnQuiz(quiz.id).isEmpty()) {
                throw new IllegalArgumentException("Cannot delete a quiz that already has submitted attempts.");
            }
            repo.deleteQuiz(quiz.id);
            repo.alerts.add(new Alert(repo.nextId("a"), "WARNING", "Quiz deleted: " + quiz.title));
            repo.saveAlerts();
        }
    }

    public void submitForApproval(Quiz quiz) {
        synchronized (repo) {
            if (quiz.questions.isEmpty()) throw new IllegalArgumentException("Add at least one question first.");
            if (quiz.status == Quiz.Status.APPROVED || quiz.status == Quiz.Status.PENDING) {
                throw new IllegalArgumentException("Quiz is already " + label(quiz.status) + ".");
            }
            quiz.status = Quiz.Status.PENDING;
            quiz.submittedAt = System.currentTimeMillis();
            repo.saveQuiz(quiz);
            repo.alerts.add(new Alert(repo.nextId("a"), "WARNING",
                    "Quiz awaiting approval: " + quiz.title + " (by " + quiz.creatorName + ")"));
            repo.saveAlerts();
        }
    }

    public void approve(Quiz quiz) {
        synchronized (repo) {
            if (quiz.status != Quiz.Status.PENDING) {
                throw new IllegalArgumentException("Only pending quizzes can be approved.");
            }
            quiz.status = Quiz.Status.APPROVED;
            quiz.reviewedAt = System.currentTimeMillis();
            quiz.reviewNote = "Approved by administrator.";
            repo.saveQuiz(quiz);
            repo.alerts.add(new Alert(repo.nextId("a"), "SUCCESS", "Quiz approved: " + quiz.title));
            repo.saveAlerts();
        }
    }

    public void reject(Quiz quiz, String reason) {
        synchronized (repo) {
            if (quiz.status != Quiz.Status.PENDING) {
                throw new IllegalArgumentException("Only pending quizzes can be rejected.");
            }
            if (reason == null || reason.isBlank()) throw new IllegalArgumentException("A reason is required to reject content.");
            quiz.status = Quiz.Status.REJECTED;
            quiz.reviewedAt = System.currentTimeMillis();
            quiz.reviewNote = reason.trim();
            repo.saveQuiz(quiz);
            repo.alerts.add(new Alert(repo.nextId("a"), "WARNING", "Quiz rejected: " + quiz.title));
            repo.saveAlerts();
        }
    }

    private void validate(String title, int durationMins, List<Question> questions) {
        if (title == null || title.trim().length() < 3) {
            throw new IllegalArgumentException("Title must be at least 3 characters.");
        }
        if (durationMins < 1 || durationMins > 300) {
            throw new IllegalArgumentException("Duration must be between 1 and 300 minutes.");
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Add at least one question.");
        }
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.text == null || q.text.trim().length() < 3) {
                throw new IllegalArgumentException("Question " + (i + 1) + " needs text (min 3 characters).");
            }
            if (q.options.size() < 2) {
                throw new IllegalArgumentException("Question " + (i + 1) + " needs at least 2 options.");
            }
            for (String opt : q.options) {
                if (opt == null || opt.isBlank()) {
                    throw new IllegalArgumentException("Question " + (i + 1) + " has an empty option.");
                }
            }
            if (q.correctIndex < 0 || q.correctIndex >= q.options.size()) {
                throw new IllegalArgumentException("Question " + (i + 1) + " has an invalid correct option.");
            }
            if (q.points < 1) throw new IllegalArgumentException("Question " + (i + 1) + " must be worth at least 1 point.");
        }
    }

    public static String label(Quiz.Status s) {
        return switch (s) {
            case DRAFT -> "Draft";
            case PENDING -> "Pending Approval";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
        };
    }
}
