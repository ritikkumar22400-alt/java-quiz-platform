package quizplatform.model;

import quizplatform.util.Json;

import java.util.Map;

public class User {

    public enum Role { ADMIN, CREATOR, PARTICIPANT }

    public String id;
    public String name;
    public String email;
    public String passwordHash;
    public Role role;
    public boolean active = true;
    public long createdAt;

    public User() {}

    public User(String id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("id", id);
        o.put("name", name);
        o.put("email", email);
        o.put("passwordHash", passwordHash);
        o.put("role", role.name());
        o.put("active", active);
        o.put("createdAt", createdAt);
        return o;
    }

    public static User fromJson(Map<String, Object> o) {
        User u = new User();
        u.id = Json.str(o, "id", "");
        u.name = Json.str(o, "name", "");
        u.email = Json.str(o, "email", "");
        u.passwordHash = Json.str(o, "passwordHash", "");
        try {
            u.role = Role.valueOf(Json.str(o, "role", "PARTICIPANT"));
        } catch (IllegalArgumentException e) {
            u.role = Role.PARTICIPANT;
        }
        u.active = Json.bool(o, "active", true);
        u.createdAt = Json.numLong(o, "createdAt", 0);
        return u;
    }
}
