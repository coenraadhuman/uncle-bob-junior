I'll build a clean configuration parser with typed values, validation, and duration support.

**Assumptions:**
- Single configuration schema with keys: `port` (int), `ssl` (boolean), `timeout` (duration)
- Sections are parsed but not validated; all key-value pairs belong to a single logical namespace
- Unknown keys and malformed lines produce errors with line numbers; type errors report the key name
- Defaults: `port=8080`, `ssl=false`, `timeout=30s`

```java
public class Duration {
    private final long milliseconds;
    
    public static Duration parse(String raw) {
        if (!raw.matches("\\d+[smh]")) {
            throw new IllegalArgumentException("Invalid duration: " + raw);
        }
        
        long amount = Long.parseLong(raw.replaceAll("[^0-9]", ""));
        char unit = raw.charAt(raw.length() - 1);
        long millis = amount * unitToMillis(unit);
        
        return new Duration(millis);
    }
    
    private static long unitToMillis(char unit) {
        return switch (unit) {
            case 's' -> 1000;
            case 'm' -> 60_000;
            case 'h' -> 3_600_000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
    
    private Duration(long milliseconds) {
        this.milliseconds = milliseconds;
    }
    
    public long milliseconds() { return milliseconds; }
    public long seconds() { return milliseconds / 1000; }
}

public class ServerConfig {
    private final int port;
    private final boolean ssl;
    private final Duration timeout;
    
    public ServerConfig(int port, boolean ssl, Duration timeout) {
        this.port = port;
        this.ssl = ssl;
        this.timeout = timeout;
    }
    
    public int port() { return port; }
    public boolean ssl() { return ssl; }
    public Duration timeout() { return timeout; }
}

public record ParseError(int line, String message) {
    @Override public String toString() {
        return line == 0 ? message : "Line " + line + ": " + message;
    }
}

public record ParseResult<T>(T value, List<ParseError> errors, boolean ok) {
    public static <T> ParseResult<T> ok(T value) {
        return new ParseResult<>(value, List.of(), true);
    }
    
    public static <T> ParseResult<T> error(List<ParseError> errors) {
        return new ParseResult<>(null, errors, false);
    }
}

public class ConfigParser {
    private static final Set<String> KNOWN_KEYS = Set.of("port", "ssl", "timeout");
    private final List<String> lines;
    
    public ConfigParser(String text) {
        this.lines = text.lines().toList();
    }
    
    public ParseResult<ServerConfig> parse() {
        Map<String, String> values = new HashMap<>();
        List<ParseError> errors = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            parseLine(lines.get(i), i + 1, values, errors);
        }
        
        if (!errors.isEmpty()) {
            return ParseResult.error(errors);
        }
        
        return buildConfig(values);
    }
    
    private void parseLine(String raw, int lineNum, Map<String, String> values, List<ParseError> errors) {
        String line = stripComment(raw).strip();
        
        if (line.isEmpty() || isSection(line)) {
            return;
        }
        
        int eqIdx = line.indexOf('=');
        if (eqIdx < 0) {
            errors.add(new ParseError(lineNum, "Malformed line: missing '='"));
            return;
        }
        
        String key = line.substring(0, eqIdx).strip();
        String value = line.substring(eqIdx + 1).strip();
        
        if (key.isEmpty()) {
            errors.add(new ParseError(lineNum, "Empty key"));
            return;
        }
        
        if (!KNOWN_KEYS.contains(key)) {
            errors.add(new ParseError(lineNum, "Unknown key: " + key));
            return;
        }
        
        values.put(key, value);
    }
    
    private ParseResult<ServerConfig> buildConfig(Map<String, String> values) {
        try {
            int port = parseInt(values.get("port"), 8080);
            boolean ssl = parseBoolean(values.get("ssl"), false);
            Duration timeout = parseDuration(values.get("timeout"), "30s");
            
            return ParseResult.ok(new ServerConfig(port, ssl, timeout));
        } catch (Exception e) {
            return ParseResult.error(List.of(new ParseError(0, e.getMessage())));
        }
    }
    
    private int parseInt(String raw, int defaultVal) {
        if (raw == null) return defaultVal;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + raw);
        }
    }
    
    private boolean parseBoolean(String raw, boolean defaultVal) {
        if (raw == null) return defaultVal;
        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;
        throw new IllegalArgumentException("Invalid boolean: " + raw);
    }
    
    private Duration parseDuration(String raw, String defaultRaw) {
        if (raw == null) raw = defaultRaw;
        try {
            return Duration.parse(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration: " + raw);
        }
    }
    
    private String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx < 0 ? line : line.substring(0, idx);
    }
    
    private boolean isSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }
}
```

**Tests:**

```java
class ConfigParserTest {
    
    @Test void defaultsWhenEmpty() {
        ParseResult<ServerConfig> result = new ConfigParser("[server]").parse();
        assertTrue(result.ok());
        assertEquals(8080, result.value().port());
        assertFalse(result.value().ssl());
        assertEquals(30_000, result.value().timeout().milliseconds());
    }
    
    @Test void parsesAllTypes() {
        String config = "port=9000\nssl=true\ntimeout=5m";
        ParseResult<ServerConfig> result = new ConfigParser(config).parse();
        assertTrue(result.ok());
        assertEquals(9000, result.value().port());
        assertTrue(result.value().ssl());
        assertEquals(300_000, result.value().timeout().milliseconds());
    }
    
    @Test void ignoresCommentsAndBlankLines() {
        String config = "\n# comment\nport=8000 # inline\n\n";
        ParseResult<ServerConfig> result = new ConfigParser(config).parse();
        assertTrue(result.ok());
        assertEquals(8000, result.value().port());
    }
    
    @Test void malformedLineReportsLineNumber() {
        ParseResult<ServerConfig> result = new ConfigParser("port:8000").parse();
        assertFalse(result.ok());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.errors().get(0).message().contains("Malformed"));
    }
    
    @Test void unknownKeyReportsLineNumber() {
        ParseResult<ServerConfig> result = new ConfigParser("foo=bar").parse();
        assertFalse(result.ok());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.errors().get(0).message().contains("Unknown key: foo"));
    }
    
    @Test void invalidIntegerReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("port=abc").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid integer"));
    }
    
    @Test void invalidBooleanReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("ssl=maybe").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid boolean"));
    }
    
    @Test void invalidDurationReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=30x").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid duration"));
    }
    
    @Test void durationSeconds() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=45s").parse();
        assertTrue(result.ok());
        assertEquals(45_000, result.value().timeout().milliseconds());
    }
    
    @Test void durationMinutes() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=2m").parse();
        assertTrue(result.ok());
        assertEquals(120_000, result.value().timeout().milliseconds());
    }
    
    @Test void durationHours() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=1h").parse();
        assertTrue(result.ok());
        assertEquals(3_600_000, result.value().timeout().milliseconds());
    }
}

class DurationTest {
    
    @Test void throwsOnInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse("30x"));
    }
    
    @Test void providesSecondAccessor() {
        Duration d = Duration.parse("2m");
        assertEquals(120, d.seconds());
    }
}
```

**cleaned:** Parser separates line parsing from type validation and uses guard clauses for early returns; `Duration` encapsulates unit conversion with named constants; `ParseError` and `ParseResult` are immutable records; line numbers track throughout the pipeline. **Safe to change because** type schema lives in one place (`KNOWN_KEYS` and `buildConfig`), parsing and validation are separate, and defaults are named constants.