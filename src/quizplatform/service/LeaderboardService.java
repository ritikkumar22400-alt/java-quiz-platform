package quizplatform.service;

import quizplatform.model.Attempt;
import quizplatform.model.User;
import quizplatform.storage.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardService {

    private final Repository repo;

    public LeaderboardService(Repository repo) {
        this.repo = repo;
    }

    public static class Row {
        public final int rank;
        public final String name;
        public final String email;
        public final int quizzesTaken;
        public final double avgPct;
        public final double bestPct;
        public final boolean isCurrentUser;

        public Row(int rank, String name, String email, int quizzesTaken,
                   double avgPct, double bestPct, boolean isCurrentUser) {
            this.rank = rank;
            this.name = name;
            this.email = email;
            this.quizzesTaken = quizzesTaken;
            this.avgPct = avgPct;
            this.bestPct = bestPct;
            this.isCurrentUser = isCurrentUser;
        }
    }

    public List<Row> ranked(String currentUserId) {
        synchronized (repo) {
            Map<String, double[]> acc = new LinkedHashMap<>(); // attempts, pctSum, best, quizzes
            Map<String, String> names = new LinkedHashMap<>();
            Map<String, String> emails = new LinkedHashMap<>();

            for (User u : repo.users) {
                if (u.role == User.Role.PARTICIPANT) {
                    acc.put(u.id, new double[4]);
                    names.put(u.id, u.name);
                    emails.put(u.id, u.email);
                }
            }
            // per-user quiz sets need nesting; compute with a two-pass approach
            Map<String, Map<String, Boolean>> userQuizzes = new LinkedHashMap<>();
            for (Attempt a : repo.attempts) {
                double[] d = acc.get(a.userId);
                if (d == null) continue;
                d[0]++;
                d[1] += a.percentage();
                if (a.percentage() > d[2]) d[2] = a.percentage();
                userQuizzes.computeIfAbsent(a.userId, k -> new LinkedHashMap<>()).put(a.quizId, true);
            }

            List<Object[]> raw = new ArrayList<>();
            for (Map.Entry<String, double[]> e : acc.entrySet()) {
                double[] d = e.getValue();
                if (d[0] == 0) continue;
                double avg = d[1] / d[0];
                int quizzes = userQuizzes.getOrDefault(e.getKey(), Map.of()).size();
                raw.add(new Object[]{e.getKey(), quizzes, avg, d[2]});
            }
            raw.sort((x, y) -> {
                int c = Double.compare((double) y[3], (double) x[3]);
                if (c != 0) return c;
                c = Double.compare((double) y[2], (double) x[2]);
                if (c != 0) return c;
                return names.get((String) x[0]).compareToIgnoreCase(names.get((String) y[0]));
            });

            List<Row> out = new ArrayList<>();
            int rank = 1;
            for (Object[] r : raw) {
                String id = (String) r[0];
                out.add(new Row(rank++, names.get(id), emails.get(id), (int) r[1],
                        (double) r[2], (double) r[3], id.equals(currentUserId)));
            }
            return out;
        }
    }
}
