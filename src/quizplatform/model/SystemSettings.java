package quizplatform.model;

import quizplatform.util.Json;

import java.util.Map;

public class SystemSettings {

    public String siteName = "JavaQuiz Platform";
    public int defaultDurationMins = 10;
    public int passPercentage = 60;
    public int maxAttemptsPerQuiz = 3;
    public boolean allowRetries = true;
    public boolean shuffleQuestions = false;
    public boolean leaderboardEnabled = true;
    public boolean registrationOpen = false;

    public Map<String, Object> toJson() {
        Map<String, Object> o = Json.map();
        o.put("siteName", siteName);
        o.put("defaultDurationMins", defaultDurationMins);
        o.put("passPercentage", passPercentage);
        o.put("maxAttemptsPerQuiz", maxAttemptsPerQuiz);
        o.put("allowRetries", allowRetries);
        o.put("shuffleQuestions", shuffleQuestions);
        o.put("leaderboardEnabled", leaderboardEnabled);
        o.put("registrationOpen", registrationOpen);
        return o;
    }

    public static SystemSettings fromJson(Map<String, Object> o) {
        SystemSettings s = new SystemSettings();
        s.siteName = Json.str(o, "siteName", s.siteName);
        s.defaultDurationMins = Json.num(o, "defaultDurationMins", s.defaultDurationMins);
        s.passPercentage = Json.num(o, "passPercentage", s.passPercentage);
        s.maxAttemptsPerQuiz = Json.num(o, "maxAttemptsPerQuiz", s.maxAttemptsPerQuiz);
        s.allowRetries = Json.bool(o, "allowRetries", s.allowRetries);
        s.shuffleQuestions = Json.bool(o, "shuffleQuestions", s.shuffleQuestions);
        s.leaderboardEnabled = Json.bool(o, "leaderboardEnabled", s.leaderboardEnabled);
        s.registrationOpen = Json.bool(o, "registrationOpen", s.registrationOpen);
        return s;
    }
}
