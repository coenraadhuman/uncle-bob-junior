I'll build a configuration parser with typed values, error tracking by line number, and default fallbacks. My assumptions:

- Sections `[name]` are allowed but ignored (for future multi-section support).
- Booleans are case-insensitive: `true`, `false`, `TRUE`, `FALSE`.
- Durations: number + unit (`ms`, `s`, `m`, `h`, `d`).
- Keys must match `[a-zA-Z_][a-zA-Z0-9_]*`.
- Values are trimmed; comments start with `#`; blank lines and sections are skipped.
- Parser returns a result with either a typed `Config` object or a list of `ConfigError`s with line numbers.
- Unknown keys and type mismatches are validation errors.

```java
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

record Duration(long milliseconds) {
  static Duration of(long value, String unit) {
    long ms = switch (unit.toLowerCase()) {
      case "ms" -> value;
      case "s" -> value * 1000;
      case "m" -> value * 60 * 1000;
      case "h" -> value * 60 * 60 * 1000;
      case "d" -> value * 24 * 60 * 60 * 1000;
      default -> throw new IllegalArgumentException("Unknown unit: " + unit);
    };
    return new Duration(ms);
  }
}

record ConfigKey(String name, Class<?> type, Object defaultValue) {
  ConfigKey {
    if (!isValidType(type)) throw new IllegalArgumentException("Unsupported type: " + type);
  }

  static boolean isValidType(Class<?> t) {
    return t == Integer.class || t == Boolean.class || t == Duration.class;
  }
}

record ConfigError(int line, String key, String message) {
  @Override
  public String toString() {
    return String.format("Line %d [%s]: %s", line, key, message);
  }
}

record ConfigParseResult(Config config, List<ConfigError> errors) {
  public boolean isValid() {
    return errors.isEmpty();
  }
}

class Config {
  private final Map<String, Object> values;

  Config(Map<String, Object> values) {
    this.values = Map.copyOf(values);
  }

  public int getInt(String key) {
    return (Integer) values.get(key);
  }

  public boolean getBoolean(String key) {
    return (Boolean) values.get(key);
  }

  public Duration getDuration(String key) {
    return (Duration) values.get(key);
  }
}

class ConfigParser {
  private static final Pattern SECTION = Pattern.compile("^\\[([^\\]]+)\\]$");
  private static final Pattern KEY_VALUE = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+)$");
  private static final Pattern DURATION_FORMAT = Pattern.compile("^(\\d+)([a-zA-Z]+)$");

  private final Map<String, ConfigKey> schema;

  ConfigParser(List<ConfigKey> keys) {
    this.schema = keys.stream()
      .collect(Collectors.toMap(ConfigKey::name, key -> key));
  }

  ConfigParseResult parse(String content) {
    String[] lines = content.split("\n", -1);
    Map<String, Object> values = new HashMap<>();
    List<ConfigError> errors = new ArrayList<>();

    applyDefaults(values);
    parseLines(lines, values, errors);

    return new ConfigParseResult(new Config(values), errors);
  }

  private void applyDefaults(Map<String, Object> values) {
    schema.values().forEach(key -> values.put(key.name(), key.defaultValue()));
  }

  private void parseLines(String[] lines, Map<String, Object> values, List<ConfigError> errors) {
    for (int i = 0; i < lines.length; i++) {
      parseLine(lines[i], i + 1, values, errors);
    }
  }

  private void parseLine(String line, int lineNumber, Map<String, Object> values,
      List<ConfigError> errors) {
    if (isIgnorable(line)) {
      return;
    }

    Matcher m = KEY_VALUE.matcher(line);
    if (!m.matches()) {
      errors.add(new ConfigError(lineNumber, "syntax", "Expected key=value format"));
      return;
    }

    String key = m.group(1);
    String valueStr = m.group(2).trim();

    if (!schema.containsKey(key)) {
      errors.add(new ConfigError(lineNumber, key, "Unknown key"));
      return;
    }

    ConfigKey configKey = schema.get(key);
    try {
      Object value = parseValue(valueStr, configKey.type());
      values.put(key, value);
    } catch (IllegalArgumentException e) {
      errors.add(new ConfigError(lineNumber, key, e.getMessage()));
    }
  }

  private boolean isIgnorable(String line) {
    String trimmed = line.trim();
    return trimmed.isEmpty() || trimmed.startsWith("#") || SECTION.matcher(trimmed).matches();
  }

  private Object parseValue(String valueStr, Class<?> type) {
    if (type == Integer.class) return parseInteger(valueStr);
    if (type == Boolean.class) return parseBoolean(valueStr);
    if (type == Duration.class) return parseDuration(valueStr);
    throw new IllegalArgumentException("Unsupported type: " + type);
  }

  private Integer parseInteger(String valueStr) {
    try {
      return Integer.parseInt(valueStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Not a valid integer");
    }
  }

  private Boolean parseBoolean(String valueStr) {
    return switch (valueStr.toLowerCase()) {
      case "true" -> true;
      case "false" -> false;
      default -> throw new IllegalArgumentException("Not a valid boolean");
    };
  }

  private Duration parseDuration(String valueStr) {
    Matcher m = DURATION_FORMAT.matcher(valueStr);
    if (!m.matches()) {
      throw new IllegalArgumentException("Not a valid duration (expected number + unit like 30s)");
    }
    long value = Long.parseLong(m.group(1));
    String unit = m.group(2);
    return Duration.of(value, unit);
  }
}

class ConfigParserTest {
  void testParseValidIntegers() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30\nmax_retries=5");
    assert result.isValid() : "Should parse valid integers";
    assert result.config().getInt("timeout") == 30;
    assert result.config().getInt("max_retries") == 5;
  }

  void testParseValidBooleans() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=true\ndebug=false");
    assert result.isValid();
    assert result.config().getBoolean("enabled");
    assert !result.config().getBoolean("debug");
  }

  void testParseValidDurations() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30s\nmax_wait=5m\nheartbeat=500ms");
    assert result.isValid();
    assert result.config().getDuration("timeout").milliseconds() == 30_000;
    assert result.config().getDuration("max_wait").milliseconds() == 300_000;
    assert result.config().getDuration("heartbeat").milliseconds() == 500;
  }

  void testApplyDefaultsForMissingKeys() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30");
    assert result.isValid();
    assert result.config().getInt("max_retries") == 10 : "Should use default";
  }

  void testIgnoreCommentsAndBlankLines() {
    ConfigParser parser = createParser();
    String config = "# This is a comment\ntimeout=30\n\n# Another\nmax_retries=5";
    ConfigParseResult result = parser.parse(config);
    assert result.isValid();
    assert result.config().getInt("timeout") == 30;
  }

  void testIgnoreSections() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("[database]\ntimeout=30\n[server]\nmax_retries=5");
    assert result.isValid();
  }

  void testErrorOnMalformedLine() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30\nmissing_equals\nmax_retries=5");
    assert !result.isValid() : "Should detect malformed line";
    assert result.errors().size() == 1;
    assert result.errors().get(0).line() == 2 : "Error on line 2";
    assert result.errors().get(0).key().equals("syntax");
  }

  void testErrorOnUnknownKey() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("unknown_setting=123");
    assert !result.isValid();
    assert result.errors().get(0).key().equals("unknown_setting");
    assert result.errors().get(0).message().contains("Unknown");
  }

  void testErrorOnInvalidIntegerValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=not_a_number");
    assert !result.isValid();
    assert result.errors().get(0).key().equals("timeout");
    assert result.errors().get(0).message().contains("integer");
  }

  void testErrorOnInvalidBooleanValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=yes");
    assert !result.isValid();
    assert result.errors().get(0).message().contains("boolean");
  }

  void testErrorOnInvalidDurationValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30");
    assert !result.isValid();
    assert result.errors().get(0).message().contains("duration");
  }

  void testCaseInsensitiveBooleans() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=TRUE\ndebug=FALSE");
    assert result.isValid();
    assert result.config().getBoolean("enabled");
    assert !result.config().getBoolean("debug");
  }

  void testMultipleErrors() {
    ConfigParser parser = createParser();
    String config = "invalid line\ntimeout=abc\nunknown=99";
    ConfigParseResult result = parser.parse(config);
    assert !result.isValid();
    assert result.errors().size() == 3 : "Should collect all errors";
    assert result.errors().get(0).line() == 1;
    assert result.errors().get(1).line() == 2;
    assert result.errors().get(2).line() == 3;
  }

  void testWhitespaceHandling() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout = 30 \n max_retries = 5");
    assert result.isValid() : "Should handle whitespace around =";
    assert result.config().getInt("timeout") == 30;
  }

  private ConfigParser createParser() {
    return new ConfigParser(List.of(
      new ConfigKey("timeout", Duration.class, new Duration(5000)),
      new ConfigKey("max_retries", Integer.class, 10),
      new ConfigKey("enabled", Boolean.class, true),
      new ConfigKey("debug", Boolean.class, false)
    ));
  }
}
```

**Design:**
- `Duration` wraps milliseconds; `Duration.of()` converts units once.
- `ConfigKey` schema entry with type and default; only three types allowed by constructor contract.
- `ConfigError` records line number, key, and message for precise diagnostics.
- `ConfigParser` validates each line against schema; guard clauses handle comment/blank/section lines early; errors accumulate without stopping parse.
- `Config` wraps values immutably; getters provide type-safe access.
- Tests cover happy path (integers, booleans, durations), defaults, whitespace, sections, comments; edges (malformed syntax, unknown keys, type mismatches, multiple errors).