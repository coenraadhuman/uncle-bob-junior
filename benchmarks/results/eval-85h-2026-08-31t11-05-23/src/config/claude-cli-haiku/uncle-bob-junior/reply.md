# Java Configuration Parser

I'll build a type-safe configuration parser with validation, sealed value types, and default fallback. The solution defines a specific schema (timeout, debug, retry_delay) and can be extended by adding fields to `Configuration` and cases to the `parseKeyValue` switch.

**Assumptions:**
- Configuration schema: `timeout` (int, default 30), `debug` (bool, default false), `retry_delay` (duration, default 5000ms)
- Sections are recognized but ignored (parse continues)
- Duration units: ms, s, m, h
- Boolean values accept: true/yes/on/1, false/no/off/0 (case-insensitive)
- Keys are case-insensitive, underscores and digits allowed
- Lines with errors are skipped; all errors collected and returned

```java
sealed interface ConfigValue {
    record IntValue(int value) implements ConfigValue {}
    record BoolValue(boolean value) implements ConfigValue {}
    record DurationValue(long millis) implements ConfigValue {}
}

record ValidationError(int lineNumber, String message) {}

public class Configuration {
    private static final int DEFAULT_TIMEOUT = 30;
    private static final boolean DEFAULT_DEBUG = false;
    private static final long DEFAULT_RETRY_DELAY_MS = 5000;
    
    private final int timeout;
    private final boolean debug;
    private final long retryDelayMs;
    private final List<ValidationError> errors;
    
    Configuration(Map<String, ConfigValue> values, List<ValidationError> errors) {
        this.timeout = extractInt(values, "timeout", DEFAULT_TIMEOUT);
        this.debug = extractBool(values, "debug", DEFAULT_DEBUG);
        this.retryDelayMs = extractDuration(values, "retry_delay", DEFAULT_RETRY_DELAY_MS);
        this.errors = errors;
    }
    
    private static int extractInt(Map<String, ConfigValue> values, String key, int defaultValue) {
        return values.get(key) instanceof ConfigValue.IntValue iv ? iv.value() : defaultValue;
    }
    
    private static boolean extractBool(Map<String, ConfigValue> values, String key, boolean defaultValue) {
        return values.get(key) instanceof ConfigValue.BoolValue bv ? bv.value() : defaultValue;
    }
    
    private static long extractDuration(Map<String, ConfigValue> values, String key, long defaultValue) {
        return values.get(key) instanceof ConfigValue.DurationValue dv ? dv.millis() : defaultValue;
    }
    
    public int getTimeout() { return timeout; }
    public boolean isDebug() { return debug; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public List<ValidationError> getErrors() { return errors; }
    public boolean hasErrors() { return !errors.isEmpty(); }
    
    public static Configuration parse(String input) {
        return new ConfigParser().parse(input);
    }
}

class ConfigParser {
    private static final java.util.regex.Pattern SECTION = 
        java.util.regex.Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final java.util.regex.Pattern KEY_VALUE = 
        java.util.regex.Pattern.compile("^\\s*([a-z_][a-z0-9_]*)\\s*=\\s*(.+?)\\s*$", 
        java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final java.util.regex.Pattern COMMENT_OR_BLANK = 
        java.util.regex.Pattern.compile("^\\s*(?:#.*)?$");
    private static final Set<String> KNOWN_KEYS = Set.of("timeout", "debug", "retry_delay");
    
    Configuration parse(String input) {
        Map<String, ConfigValue> values = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();
        String[] lines = input.split("\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            parseLine(lines[i], i + 1, values, errors);
        }
        
        return new Configuration(values, errors);
    }
    
    private void parseLine(String line, int lineNumber, Map<String, ConfigValue> values,
            List<ValidationError> errors) {
        if (COMMENT_OR_BLANK.matcher(line).matches()) {
            return;
        }
        
        if (SECTION.matcher(line).matches()) {
            return;
        }
        
        java.util.regex.Matcher kv = KEY_VALUE.matcher(line);
        if (!kv.matches()) {
            errors.add(new ValidationError(lineNumber, "Malformed line"));
            return;
        }
        
        String key = kv.group(1);
        if (!KNOWN_KEYS.contains(key)) {
            errors.add(new ValidationError(lineNumber, "Unknown key: " + key));
            return;
        }
        
        parseKeyValue(key, kv.group(2), lineNumber, values, errors);
    }
    
    private void parseKeyValue(String key, String valueStr, int lineNumber,
            Map<String, ConfigValue> values, List<ValidationError> errors) {
        ConfigValue value = switch (key) {
            case "timeout" -> parseInteger(valueStr, lineNumber, errors);
            case "debug" -> parseBoolean(valueStr, lineNumber, errors);
            case "retry_delay" -> parseDuration(valueStr, lineNumber, errors);
            default -> null;
        };
        
        if (value != null) {
            values.put(key, value);
        }
    }
    
    private ConfigValue parseInteger(String str, int lineNumber, List<ValidationError> errors) {
        try {
            return new ConfigValue.IntValue(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(lineNumber, "Invalid integer: " + str));
            return null;
        }
    }
    
    private ConfigValue parseBoolean(String str, int lineNumber, List<ValidationError> errors) {
        String lower = str.toLowerCase();
        
        if (lower.equals("true") || lower.equals("yes") || lower.equals("on") || lower.equals("1")) {
            return new ConfigValue.BoolValue(true);
        }
        
        if (lower.equals("false") || lower.equals("no") || lower.equals("off") || lower.equals("0")) {
            return new ConfigValue.BoolValue(false);
        }
        
        errors.add(new ValidationError(lineNumber, "Invalid boolean: " + str));
        return null;
    }
    
    private ConfigValue parseDuration(String str, int lineNumber, List<ValidationError> errors) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d+)\\s*([a-z]+)$",
            java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(str);
        
        if (!m.matches()) {
            errors.add(new ValidationError(lineNumber, "Invalid duration: " + str));
            return null;
        }
        
        long value = Long.parseLong(m.group(1));
        long multiplier = durationMultiplier(m.group(2).toLowerCase());
        
        if (multiplier < 0) {
            errors.add(new ValidationError(lineNumber, "Unknown duration unit: " + m.group(2)));
            return null;
        }
        
        return new ConfigValue.DurationValue(value * multiplier);
    }
    
    private long durationMultiplier(String unit) {
        return switch (unit) {
            case "ms" -> 1;
            case "s" -> 1_000;
            case "m" -> 60_000;
            case "h" -> 3_600_000;
            default -> -1;
        };
    }
}
```

**Tests:**

```java
class ConfigurationTest {
    @Test void parsesValidConfiguration() {
        String input = "timeout=60\ndebug=true\nretry_delay=5s";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(true, config.isDebug());
        assertEquals(5000, config.getRetryDelayMs());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void usesDefaults() {
        Configuration config = Configuration.parse("");
        assertEquals(30, config.getTimeout());
        assertEquals(false, config.isDebug());
        assertEquals(5000, config.getRetryDelayMs());
    }
    
    @Test void skipsCommentsAndBlankLines() {
        String input = "# comment\ntimeout=60\n\n# another\n";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void reportsMalformedLineWithNumber() {
        String input = "timeout 60";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals(1, config.getErrors().get(0).lineNumber());
        assertEquals("Malformed line", config.getErrors().get(0).message());
    }
    
    @Test void reportsUnknownKeyWithNumber() {
        String input = "timeout=60\nunknown_key=value";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals(2, config.getErrors().get(0).lineNumber());
        assertEquals("Unknown key: unknown_key", config.getErrors().get(0).message());
    }
    
    @Test void reportsInvalidInteger() {
        String input = "timeout=abc";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Invalid integer: abc", config.getErrors().get(0).message());
    }
    
    @Test void reportsInvalidBoolean() {
        String input = "debug=maybe";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Invalid boolean: maybe", config.getErrors().get(0).message());
    }
    
    @Test void parsesDurationWithVariousUnits() {
        assertEquals(30_000, Configuration.parse("retry_delay=30s").getRetryDelayMs());
        assertEquals(2_000, Configuration.parse("retry_delay=2s").getRetryDelayMs());
        assertEquals(120_000, Configuration.parse("retry_delay=2m").getRetryDelayMs());
        assertEquals(500, Configuration.parse("retry_delay=500ms").getRetryDelayMs());
        assertEquals(3_600_000, Configuration.parse("retry_delay=1h").getRetryDelayMs());
    }
    
    @Test void reportsInvalidDurationUnit() {
        String input = "retry_delay=10x";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Unknown duration unit: x", config.getErrors().get(0).message());
    }
    
    @Test void ignoresSections() {
        String input = "[section1]\ntimeout=60\n[section2]\ndebug=true";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(true, config.isDebug());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void handlesWhitespace() {
        String input = "  timeout  =  42  \n  debug = true  ";
        Configuration config = Configuration.parse(input);
        assertEquals(42, config.getTimeout());
        assertEquals(true, config.isDebug());
    }
    
    @Test void acceptsBooleanVariants() {
        assertEquals(true, Configuration.parse("debug=yes").isDebug());
        assertEquals(true, Configuration.parse("debug=on").isDebug());
        assertEquals(true, Configuration.parse("debug=1").isDebug());
        assertEquals(false, Configuration.parse("debug=no").isDebug());
        assertEquals(false, Configuration.parse("debug=off").isDebug());
    }
}
```

Cleaned: sealed value types encode validity, guard clauses flatten control flow, one job per method, no magic values (defaults and multipliers named), immutable records. Safe to change because field extraction is isolated, type parsing is testable and modular, and error collection never throws.