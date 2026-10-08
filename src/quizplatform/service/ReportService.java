package quizplatform.service;

import quizplatform.model.Attempt;
import quizplatform.model.Quiz;
import quizplatform.storage.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregated statistics used by dashboards and charts. */
public class ReportService {

    private final Repository repo;

    public ReportService(Repository repo) {
        this.repo = repo;
    }

    public static class Stat {
        public final String label;
        public final double value;

        public Stat(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class ParticipantOverview {
        public int attempts;
        public int quizzesTaken;
        public int passed;
        public double avgPct;
        public double bestPct;
    }

    /** Average score (%) per quiz across all attempts. */
    public List<Stat> averageScorePerQuiz() {
        synchronized (repo) {
            Map<String, double[]> acc = new LinkedHashMap<>(); // sumPct, count
            for (Attempt a : repo.attempts) {
                double[] d = acc.computeIfAbsent(a.quizTitle, k -> new double[2]);
                d[0] += a.percentage();
                d[1]++;
            }
            List<Stat> out = new ArrayList<>();
            for (Map.Entry<String, double[]> e : acc.entrySet()) {
                double[] d = e.getValue();
                out.add(new Stat(truncate(e.getKey()), d[1] == 0 ? 0 : d[0] / d[1]));
            }
            return out;
        }
    }

    /** Attempts count per quiz. */
    public List<Stat> attemptsPerQuiz() {
        synchronized (repo) {
            Map<String, double[]> acc = new LinkedHashMap<>();
            for (Attempt a : repo.attempts) {
                acc.computeIfAbsent(a.quizTitle, k -> new double[1])[0]++;
            }
            List<Stat> out = new ArrayList<>();
            for (Map.Entry<String, double[]> e : acc.entrySet()) {
                out.add(new Stat(truncate(e.getKey()), e.getValue()[0]));
            }
            return out;
        }
    }

    /** Pass/fail split using the configured pass percentage (pie chart). */
    public List<Stat> passFailSplit() {
        synchronized (repo) {
            int pass = 0, fail = 0;
            int threshold = repo.settings.passPercentage;
            for (Attempt a : repo.attempts) {
                if (a.percentage() >= threshold) pass++;
                else fail++;
            }
            List<Stat> out = new ArrayList<>();
            out.add(new Stat("Passed", pass));
            out.add(new Stat("Failed", fail));
            return out;
        }
    }

    /** Attempts per day for the last `days` days (line chart). */
    public List<Stat> attemptsPerDay(int days) {
        synchronized (repo) {
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM");
            long now = System.currentTimeMillis();
            long dayMs = 86_400_000L;
            LinkedHashMap<String, double[]> buckets = new LinkedHashMap<>();
            List<String> order = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                String label = fmt.format(new Date(now - i * dayMs));
                buckets.put(label, new double[1]);
                order.add(label);
            }
            // labels may collide across months in rare cases; keep simple by re-bucketing on date string
            for (Attempt a : repo.attempts) {
                String label = fmt.format(new Date(a.submittedAt));
                if (a.submittedAt >= now - days * dayMs && buckets.containsKey(label)) {
                    buckets.get(label)[0]++;
                }
            }
            List<Stat> out = new ArrayList<>();
            for (String label : order) out.add(new Stat(label, buckets.get(label)[0]));
            return out;
        }
    }

    public ParticipantOverview overview(String userId) {
        synchronized (repo) {
            ParticipantOverview o = new ParticipantOverview();
            Map<String, Boolean> quizzes = new LinkedHashMap<>();
            double pctSum = 0;
            for (Attempt a : repo.attempts) {
                if (!a.userId.equals(userId)) continue;
                o.attempts++;
                quizzes.put(a.quizId, true);
                pctSum += a.percentage();
                if (a.percentage() > o.bestPct) o.bestPct = a.percentage();
                if (a.percentage() >= repo.settings.passPercentage) o.passed++;
            }
            o.quizzesTaken = quizzes.size();
            o.avgPct = o.attempts == 0 ? 0 : pctSum / o.attempts;
            return o;
        }
    }

    /** Score trend (percentage) across the user's attempts, chronological. */
    public List<Stat> scoreTrend(String userId) {
        synchronized (repo) {
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM");
            List<Attempt> mine = new ArrayList<>();
            for (Attempt a : repo.attempts) if (a.userId.equals(userId)) mine.add(a);
            mine.sort((x, y) -> Long.compare(x.submittedAt, y.submittedAt));
            List<Stat> out = new ArrayList<>();
            int i = 1;
            for (Attempt a : mine) {
                out.add(new Stat(i++ + ". " + truncate(a.quizTitle) + " (" + fmt.format(new Date(a.submittedAt)) + ")",
                        Math.round(a.percentage())));
            }
            return out;
        }
    }

    /** Leaderboard-style row used by reports. */
    public static class ResultRow {
        public final String quizTitle;
        public final String date;
        public final int score;
        public final int max;
        public final double pct;
        public final boolean graded;
        public final String feedback;
        public final String attemptId;

        public ResultRow(String attemptId, String quizTitle, String date, int score, int max,
                         double pct, boolean graded, String feedback) {
            this.attemptId = attemptId;
            this.quizTitle = quizTitle;
            this.date = date;
            this.score = score;
            this.max = max;
            this.pct = pct;
            this.graded = graded;
            this.feedback = feedback;
        }
    }

    public List<ResultRow> resultRows(String userId) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        List<ResultRow> out = new ArrayList<>();
        for (Attempt a : repo.attemptsFor(userId)) {
            out.add(new ResultRow(a.id, a.quizTitle, fmt.format(new Date(a.submittedAt)),
                    a.score, a.maxScore, a.percentage(), a.graded, a.feedback));
        }
        return out;
    }

    private static String truncate(String s) {
        return s.length() <= 24 ? s : s.substring(0, 22) + "..";
    }
}
