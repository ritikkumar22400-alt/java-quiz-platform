package quizplatform.model;

import quizplatform.util.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Question {

    public String text;
    public List<String> options = new ArrayList<>();
    public int correctIndex;
    public int points = 1;

    public Question() {}

    public Question(String text, List<String> options, int correctIndex, int points) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.points = points;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("text", text);
        List<Object> opts = Json.list();
        opts.addAll(options);
        o.put("options", opts);
        o.put("correctIndex", correctIndex);
        o.put("points", points);
        return o;
    }

    public static Question fromJson(Map<String, Object> o) {
        Question q = new Question();
        q.text = Json.str(o, "text", "");
        for (Object opt : Json.arr(o, "options")) q.options.add(String.valueOf(opt));
        while (q.options.size() < 4) q.options.add("");
        q.correctIndex = Json.num(o, "correctIndex", 0);
        q.points = Json.num(o, "points", 1);
        return q;
    }
}
