package quizplatform.model;

import quizplatform.util.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Quiz {

    public enum Status { DRAFT, PENDING, APPROVED, REJECTED }

    public String id;
    public String title;
    public String description;
    public String topic;
    public int durationMins = 10;
    public String creatorId;
    public String creatorName;
    public Status status = Status.DRAFT;
    public String reviewNote = "";
    public long createdAt;
    public long submittedAt;
    public long reviewedAt;
    public List<Question> questions = new ArrayList<>();

    public int totalPoints() {
        int t = 0;
        for (Question q : questions) t += q.points;
        return t;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("title", title);
        o.put("description", description);
        o.put("topic", topic);
        o.put("durationMins", durationMins);
        o.put("creatorId", creatorId);
        o.put("creatorName", creatorName);
        o.put("status", status.name());
        o.put("reviewNote", reviewNote);
        o.put("createdAt", createdAt);
        o.put("submittedAt", submittedAt);
        o.put("reviewedAt", reviewedAt);
        List<Object> qs = Json.list();
        for (Question q : questions) qs.add(q.toJson());
        o.put("questions", qs);
        return o;
    }

    public static Quiz fromJson(Map<String, Object> o) {
        Quiz qz = new Quiz();
        qz.id = Json.str(o, "id", "");
        qz.title = Json.str(o, "title", "");
        qz.description = Json.str(o, "description", "");
        qz.topic = Json.str(o, "topic", "");
        qz.durationMins = Json.num(o, "durationMins", 10);
        qz.creatorId = Json.str(o, "creatorId", "");
        qz.creatorName = Json.str(o, "creatorName", "");
        try {
            qz.status = Status.valueOf(Json.str(o, "status", "DRAFT"));
        } catch (IllegalArgumentException e) {
            qz.status = Status.DRAFT;
        }
        qz.reviewNote = Json.str(o, "reviewNote", "");
        qz.createdAt = Json.numLong(o, "createdAt", 0);
        qz.submittedAt = Json.numLong(o, "submittedAt", 0);
        qz.reviewedAt = Json.numLong(o, "reviewedAt", 0);
        for (Object item : Json.arr(o, "questions")) {
            if (item instanceof Map) qz.questions.add(Question.fromJson(Json.mapOf(item)));
        }
        return qz;
    }
}
