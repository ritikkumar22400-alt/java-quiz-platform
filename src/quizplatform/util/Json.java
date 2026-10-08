package quizplatform.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal dependency-free JSON parser/writer (plain javac, no libraries). */
public final class Json {

    private Json() {}

    // ------------------------------------------------------------------ write
    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            writeString(s, sb);
        } else if (v instanceof Boolean || v instanceof Integer || v instanceof Long) {
            sb.append(v);
        } else if (v instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) sb.append("null");
            else sb.append(d);
        } else if (v instanceof Number n) {
            sb.append(n.doubleValue());
        } else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(String.valueOf(e.getKey()), sb);
                sb.append(':');
                writeValue(e.getValue(), sb);
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object o : it) {
                if (!first) sb.append(',');
                first = false;
                writeValue(o, sb);
            }
            sb.append(']');
        } else {
            writeString(String.valueOf(v), sb);
        }
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ------------------------------------------------------------------ parse
    public static Object parse(String text) {
        Parser p = new Parser(text);
        Object v = p.parseValue();
        p.skipWs();
        if (p.pos < p.len) throw p.err("trailing characters");
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object v = parse(text);
        if (v instanceof Map) return (Map<String, Object>) v;
        throw new IllegalStateException("Expected JSON object");
    }

    private static final class Parser {
        final String s;
        final int len;
        int pos = 0;

        Parser(String s) { this.s = s; this.len = s.length(); }

        RuntimeException err(String msg) {
            return new IllegalStateException("JSON error at " + pos + ": " + msg);
        }

        void skipWs() {
            while (pos < len) {
                char c = s.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
                else break;
            }
        }

        char peek() {
            if (pos >= len) throw err("unexpected end");
            return s.charAt(pos);
        }

        void expect(char c) {
            if (pos >= len || s.charAt(pos) != c) throw err("expected '" + c + "'");
            pos++;
        }

        Object parseValue() {
            skipWs();
            char c = peek();
            return switch (c) {
                case '{' -> parseObj();
                case '[' -> parseArr();
                case '"' -> parseStr();
                case 't' -> parseLit("true", Boolean.TRUE);
                case 'f' -> parseLit("false", Boolean.FALSE);
                case 'n' -> parseLit("null", null);
                default -> parseNum();
            };
        }

        Object parseLit(String lit, Object val) {
            if (!s.startsWith(lit, pos)) throw err("invalid literal");
            pos += lit.length();
            return val;
        }

        Map<String, Object> parseObj() {
            expect('{');
            Map<String, Object> m = new LinkedHashMap<>();
            skipWs();
            if (peek() == '}') { pos++; return m; }
            while (true) {
                skipWs();
                String key = parseStr();
                skipWs();
                expect(':');
                m.put(key, parseValue());
                skipWs();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == '}') { pos++; return m; }
                throw err("expected ',' or '}'");
            }
        }

        List<Object> parseArr() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                list.add(parseValue());
                skipWs();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == ']') { pos++; return list; }
                throw err("expected ',' or ']'");
            }
        }

        String parseStr() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= len) throw err("unterminated string");
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= len) throw err("bad escape");
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            if (pos + 4 > len) throw err("bad unicode escape");
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                        default -> throw err("bad escape '\\" + e + "'");
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        Object parseNum() {
            int start = pos;
            if (pos < len && (s.charAt(pos) == '-' || s.charAt(pos) == '+')) pos++;
            boolean dot = false;
            while (pos < len) {
                char c = s.charAt(pos);
                if (c >= '0' && c <= '9') pos++;
                else if (c == '.' && !dot) { dot = true; pos++; }
                else if (c == 'e' || c == 'E') pos++;
                else if ((c == '-' || c == '+') && pos > start && (s.charAt(pos - 1) == 'e' || s.charAt(pos - 1) == 'E')) pos++;
                else break;
            }
            String num = s.substring(start, pos);
            if (num.isEmpty()) throw err("invalid number");
            if (dot || num.indexOf('e') >= 0 || num.indexOf('E') >= 0) return Double.parseDouble(num);
            try { return Integer.parseInt(num); } catch (NumberFormatException ex) { return Long.parseLong(num); }
        }
    }

    // --------------------------------------------------------------- helpers
    public static String str(Map<String, Object> o, String k, String def) {
        Object v = o.get(k);
        return v instanceof String s ? s : def;
    }

    public static int num(Map<String, Object> o, String k, int def) {
        Object v = o.get(k);
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof Double d) return d.intValue();
        return def;
    }

    public static long numLong(Map<String, Object> o, String k, long def) {
        Object v = o.get(k);
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l;
        if (v instanceof Double d) return d.longValue();
        return def;
    }

    public static double numDouble(Map<String, Object> o, String k, double def) {
        Object v = o.get(k);
        if (v instanceof Double d) return d;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l;
        return def;
    }

    public static boolean bool(Map<String, Object> o, String k, boolean def) {
        Object v = o.get(k);
        return v instanceof Boolean b ? b : def;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> arr(Map<String, Object> o, String k) {
        Object v = o.get(k);
        return v instanceof List ? (List<Object>) v : new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(Map<String, Object> o, String k) {
        Object v = o.get(k);
        return v instanceof Map ? (Map<String, Object>) v : new LinkedHashMap<>();
    }

    public static Map<String, Object> mapOf(Object v) {
        if (v instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) v;
            return m;
        }
        return new LinkedHashMap<>();
    }

    public static Map<String, Object> map() { return new LinkedHashMap<>(); }

    public static List<Object> list() { return new ArrayList<>(); }
}
