package quizplatform.model;

import quizplatform.util.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Attempt {

    public String id;
    public String quizId;
    public String quizTitle;
    public String userId;
    public String userName;
    public List<Integer> answers = new ArrayList<>();   // -1 = unanswered
    public int score;
    public int maxScore;
    public long startedAt;
    public long submittedAt;
    public int timeTakenSec;
    public boolean graded;
    public String feedback = "";

    public double percentage() {
        return maxScore <= 0 ? 0 : (score * 100.0) / maxScore;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("quizId", quizId);
        o.put("quizTitle", quizTitle);
        o.put("userId", userId);
        o.put("userName", userName);
        List<Object> ans = Json.list();
        ans.addAll(answers);
        o.put("answers", ans);
        o.put("score", score);
        o.put("maxScore", maxScore);
        o.put("startedAt", startedAt);
        o.put("submittedAt", submittedAt);
        o.put("timeTakenSec", timeTakenSec);
        o.put("graded", graded);
        o.put("feedback", feedback);
        return o;
    }

    public static Attempt fromJson(Map<String, Object> o) {
        Attempt a = new Attempt();
        a.id = Json.str(o, "id", "");
        a.quizId = Json.str(o, "quizId", "");
        a.quizTitle = Json.str(o, "quizTitle", "");
        a.userId = Json.str(o, "userId", "");
        a.userName = Json.str(o, "userName", "");
        for (Object v : Json.arr(o, "answers")) {
            if (v instanceof Integer i) a.answers.add(i);
            else if (v instanceof Long l) a.answers.add(l.intValue());
            else if (v instanceof Double d) a.answers.add(d.intValue());
            else a.answers.add(-1);
        }
        a.score = Json.num(o, "score", 0);
        a.maxScore = Json.num(o, "maxScore", 0);
        a.startedAt = Json.numLong(o, "startedAt", 0);
        a.submittedAt = Json.numLong(o, "submittedAt", 0);
        a.timeTakenSec = Json.num(o, "timeTakenSec", 0);
        a.graded = Json.bool(o, "graded", false);
        a.feedback = Json.str(o, "feedback", "");
        return a;
    }
}
