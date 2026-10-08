package quizplatform.model;

import quizplatform.util.Json;

import java.util.Map;

public class Message {

    public String id;
    public String fromUserId;
    public String fromName;
    public String fromRole;
    public String toUserId;
    public String toName;
    public String text;
    public long sentAt;
    public boolean read;

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("fromUserId", fromUserId);
        o.put("fromName", fromName);
        o.put("fromRole", fromRole);
        o.put("toUserId", toUserId);
        o.put("toName", toName);
        o.put("text", text);
        o.put("sentAt", sentAt);
        o.put("read", read);
        return o;
    }

    public static Message fromJson(Map<String, Object> o) {
        Message m = new Message();
        m.id = Json.str(o, "id", "");
        m.fromUserId = Json.str(o, "fromUserId", "");
        m.fromName = Json.str(o, "fromName", "");
        m.fromRole = Json.str(o, "fromRole", "");
        m.toUserId = Json.str(o, "toUserId", "");
        m.toName = Json.str(o, "toName", "");
        m.text = Json.str(o, "text", "");
        m.sentAt = Json.numLong(o, "sentAt", 0);
        m.read = Json.bool(o, "read", false);
        return m;
    }
}
