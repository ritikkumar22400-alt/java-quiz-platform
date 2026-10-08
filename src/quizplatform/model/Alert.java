package quizplatform.model;

import quizplatform.util.Json;

import java.util.Map;

public class Alert {

    public String id;
    public String type;      // INFO, WARNING, SUCCESS
    public String message;
    public long createdAt;
    public boolean read;

    public Alert() {}

    public Alert(String id, String type, String message) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("type", type);
        o.put("message", message);
        o.put("createdAt", createdAt);
        o.put("read", read);
        return o;
    }

    public static Alert fromJson(Map<String, Object> o) {
        Alert a = new Alert();
        a.id = Json.str(o, "id", "");
        a.type = Json.str(o, "type", "INFO");
        a.message = Json.str(o, "message", "");
        a.createdAt = Json.numLong(o, "createdAt", 0);
        a.read = Json.bool(o, "read", false);
        return a;
    }
}
