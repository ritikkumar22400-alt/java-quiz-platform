package quizplatform.service;

import quizplatform.model.Alert;
import quizplatform.model.Attempt;
import quizplatform.model.Quiz;
import quizplatform.model.User;
import quizplatform.storage.Repository;

import java.util.List;

public class AttemptService {

    private final Repository repo;

    public AttemptService(Repository repo) {
        this.repo = repo;
    }

    /** Validates eligibility and returns an error message, or null when allowed. */
    public String canAttempt(User user, Quiz quiz) {
        synchronized (repo) {
            if (quiz.status != Quiz.Status.APPROVED) return "This quiz is not available for taking.";
            if (quiz.questions.isEmpty()) return "This quiz has no questions.";
            if (!repo.settings.allowRetries && repo.attemptCount(user.id, quiz.id) > 0) {
                return "Retakes are disabled for this system.";
            }
            int used = repo.attemptCount(user.id, quiz.id);
            if (used >= repo.settings.maxAttemptsPerQuiz) {
                return "Maximum of " + repo.settings.maxAttemptsPerQuiz + " attempts reached.";
            }
            return null;
        }
    }

    public Attempt submit(User user, Quiz quiz, List<Integer> answers, long startedAt) {
        synchronized (repo) {
            String block = canAttempt(user, quiz);
            if (block != null) throw new IllegalArgumentException(block);

            Attempt a = new Attempt();
            a.id = repo.nextId("at");
            a.quizId = quiz.id;
            a.quizTitle = quiz.title;
            a.userId = user.id;
            a.userName = user.name;
            a.startedAt = startedAt;
            a.submittedAt = System.currentTimeMillis();
            a.timeTakenSec = (int) Math.max(0, (a.submittedAt - startedAt) / 1000);

            int score = 0, max = 0;
            for (int i = 0; i < quiz.questions.size(); i++) {
                var question = quiz.questions.get(i);
                int ans = i < answers.size() ? answers.get(i) : -1;
                a.answers.add(ans);
                max += question.points;
                if (ans == question.correctIndex) score += question.points;
            }
            a.score = score;
            a.maxScore = max;
            a.graded = true;
            a.feedback = a.percentage() >= repo.settings.passPercentage
                    ? "Auto-graded: well done!"
                    : "Auto-graded: review the topics you missed.";

            repo.saveAttempt(a);
            repo.alerts.add(new Alert(repo.nextId("a"), "INFO",
                    user.name + " attempted \"" + quiz.title + "\" - " + score + "/" + max));
            repo.saveAlerts();
            return a;
        }
    }

    /** Creator/admin grading: feedback plus optional score adjustment. */
    public void grade(Attempt attempt, int score, String feedback) {
        synchronized (repo) {
            if (score < 0 || score > attempt.maxScore) {
                throw new IllegalArgumentException("Score must be between 0 and " + attempt.maxScore + ".");
            }
            attempt.score = score;
            attempt.feedback = feedback == null ? "" : feedback.trim();
            attempt.graded = true;
            repo.saveAttempt(attempt);
        }
    }
}
