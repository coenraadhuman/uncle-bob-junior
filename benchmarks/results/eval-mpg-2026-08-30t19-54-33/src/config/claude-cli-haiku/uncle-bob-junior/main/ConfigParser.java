import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

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
