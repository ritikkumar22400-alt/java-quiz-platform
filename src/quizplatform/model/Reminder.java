package quizplatform.model;

import quizplatform.util.Json;

import java.util.Map;

public class Reminder {

    public String id;
    public String userId;
    public String quizId;
    public String quizTitle;
    public String note;
    public long remindAt;

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("userId", userId);
        o.put("quizId", quizId);
        o.put("quizTitle", quizTitle);
        o.put("note", note);
        o.put("remindAt", remindAt);
        return o;
    }

    public static Reminder fromJson(Map<String, Object> o) {
        Reminder r = new Reminder();
        r.id = Json.str(o, "id", "");
        r.userId = Json.str(o, "userId", "");
        r.quizId = Json.str(o, "quizId", "");
        r.quizTitle = Json.str(o, "quizTitle", "");
        r.note = Json.str(o, "note", "");
        r.remindAt = Json.numLong(o, "remindAt", 0);
        return r;
    }
}
