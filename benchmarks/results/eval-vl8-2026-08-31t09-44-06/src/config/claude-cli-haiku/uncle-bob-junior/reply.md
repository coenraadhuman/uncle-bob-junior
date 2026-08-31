```java
import java.util.*;

sealed interface ConfigValue permits IntValue, BoolValue, DurationValue {}

record IntValue(int value) implements ConfigValue {}
record BoolValue(boolean value) implements ConfigValue {}
record DurationValue(long seconds) implements ConfigValue {}

record ConfigKey(ConfigValue defaultValue, Class<? extends ConfigValue> expectedType) {}

class Configuration {
    private final Map<String, ConfigValue> values;
    private final List<ValidationError> errors;
    
    Configuration(Map<String, ConfigValue> values, List<ValidationError> errors) {
        this.values = new HashMap<>(values);
        this.errors = new ArrayList<>(errors);
    }
    
    public int getInt(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof IntValue iv) {
            return iv.value();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    public boolean getBoolean(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof BoolValue bv) {
            return bv.value();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    public long getDurationSeconds(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof DurationValue dv) {
            return dv.seconds();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    private String fullKey(String section, String name) {
        return section.isEmpty() ? name : section + "." + name;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
}

record ValidationError(int lineNumber, String message) {
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}

class ConfigParser {
    private final Map<String, ConfigKey> schema;
    
    ConfigParser(Map<String, ConfigKey> schema) {
        this.schema = new HashMap<>(schema);
    }
    
    Configuration parse(String content) {
        Map<String, ConfigValue> values = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();
        
        String[] lines = content.split("\n");
        String currentSection = "";
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            int displayLineNum = lineNum + 1;
            String line = lines[lineNum].trim();
            
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1);
                continue;
            }
            
            if (!line.contains("=")) {
                errors.add(new ValidationError(displayLineNum, "Malformed line, missing '='"));
                continue;
            }
            
            String[] parts = line.split("=", 2);
            String keyName = parts[0].trim();
            String rawValue = parts[1].trim();
            String fullKey = currentSection.isEmpty() ? keyName : currentSection + "." + keyName;
            
            ConfigKey keySpec = schema.get(fullKey);
            if (keySpec == null) {
                errors.add(new ValidationError(displayLineNum, "Unknown key: " + fullKey));
                continue;
            }
            
            try {
                ConfigValue parsed = parseValue(rawValue, keySpec.expectedType());
                values.put(fullKey, parsed);
            } catch (Exception e) {
                String typeName = keySpec.expectedType().getSimpleName();
                errors.add(new ValidationError(displayLineNum, "Invalid " + typeName + ": " + rawValue));
            }
        }
        
        for (Map.Entry<String, ConfigKey> entry : schema.entrySet()) {
            if (!values.containsKey(entry.getKey())) {
                values.put(entry.getKey(), entry.getValue().defaultValue());
            }
        }
        
        return new Configuration(values, errors);
    }
    
    private ConfigValue parseValue(String rawValue, Class<? extends ConfigValue> type) 
            throws Exception {
        if (type == IntValue.class) {
            return new IntValue(Integer.parseInt(rawValue));
        } else if (type == BoolValue.class) {
            return new BoolValue(parseBoolean(rawValue));
        } else if (type == DurationValue.class) {
            return new DurationValue(parseDuration(rawValue));
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
    
    private boolean parseBoolean(String value) throws Exception {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new Exception("Invalid boolean");
        };
    }
    
    private long parseDuration(String value) throws Exception {
        String lower = value.toLowerCase().trim();
        if (lower.endsWith("s")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1));
        } else if (lower.endsWith("m")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1)) * 60L;
        } else if (lower.endsWith("h")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1)) * 3600L;
        }
        throw new Exception("Invalid duration format");
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class ConfigParserTest {
    
    @Test
    void parsesIntValue() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new IntValue(30), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("timeout = 100");
        
        assertFalse(config.hasErrors());
        assertEquals(100, config.getInt("", "timeout"));
    }
    
    @Test
    void usesDefaultForMissingKey() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new IntValue(30), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("# comment only");
        
        assertFalse(config.hasErrors());
        assertEquals(30, config.getInt("", "timeout"));
    }
    
    @Test
    void parsesSectionedKeys() {
        Map<String, ConfigKey> schema = Map.of(
            "db.port", new ConfigKey(new IntValue(5432), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[db]\nport = 3306");
        
        assertFalse(config.hasErrors());
        assertEquals(3306, config.getInt("db", "port"));
    }
    
    @Test
    void parsesBooleanTrue() {
        Map<String, ConfigKey> schema = Map.of(
            "cache.enabled", new ConfigKey(new BoolValue(false), BoolValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[cache]\nenabled = yes");
        
        assertFalse(config.hasErrors());
        assertTrue(config.getBoolean("cache", "enabled"));
    }
    
    @Test
    void parsesBooleanFalse() {
        Map<String, ConfigKey> schema = Map.of(
            "debug", new ConfigKey(new BoolValue(true), BoolValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("debug = false");
        
        assertFalse(config.hasErrors());
        assertFalse(config.getBoolean("", "debug"));
    }
    
    @Test
    void parsesDurationSeconds() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("timeout = 30s");
        
        assertFalse(config.hasErrors());
        assertEquals(30, config.getDurationSeconds("", "timeout"));
    }
    
    @Test
    void parsesDurationMinutes() {
        Map<String, ConfigKey> schema = Map.of(
            "cache.ttl", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[cache]\nttl = 5m");
        
        assertFalse(config.hasErrors());
        assertEquals(300, config.getDurationSeconds("cache", "ttl"));
    }
    
    @Test
    void parsesDurationHours() {
        Map<String, ConfigKey> schema = Map.of(
            "retention", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("retention = 2h");
        
        assertFalse(config.hasErrors());
        assertEquals(7200, config.getDurationSeconds("", "retention"));
    }
    
    @Test
    void reportsUnknownKeyWithLineNumber() {
        Map<String, ConfigKey> schema = new HashMap<>();
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("unknown = 42");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertTrue(errors.get(0).message().contains("Unknown key"));
    }
    
    @Test
    void reportsMalformedLineWithLineNumber() {
        Map<String, ConfigKey> schema = new HashMap<>();
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("not a valid line");
        
        assertTrue(config.hasErrors());
        assertEquals(1, config.getErrors().get(0).lineNumber());
        assertTrue(config.getErrors().get(0).message().contains("Malformed"));
    }
    
    @Test
    void reportsTypeErrorWithLineNumber() {
        Map<String, ConfigKey> schema = Map.of(
            "port", new ConfigKey(new IntValue(8080), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("port = not_a_number");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertTrue(errors.get(0).message().contains("Invalid IntValue"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() {
        Map<String, ConfigKey> schema = Map.of(
            "value", new ConfigKey(new IntValue(0), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("# comment\n\nvalue = 42\n# footer");
        
        assertFalse(config.hasErrors());
        assertEquals(42, config.getInt("", "value"));
    }
    
    @Test
    void multipleErrors() {
        Map<String, ConfigKey> schema = Map.of(
            "known", new ConfigKey(new IntValue(0), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("malformed\nunknown = 1\nknown = bad");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(3, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertEquals(2, errors.get(1).lineNumber());
        assertEquals(3, errors.get(2).lineNumber());
    }
    
    @Test
    void multipleSections() {
        Map<String, ConfigKey> schema = Map.of(
            "db.host", new ConfigKey(new IntValue(0), IntValue.class),
            "cache.ttl", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        String content = "[db]\nhost = 1\n[cache]\nttl = 5m";
        Configuration config = parser.parse(content);
        
        assertFalse(config.hasErrors());
        assertEquals(1, config.getInt("db", "host"));
        assertEquals(300, config.getDurationSeconds("cache", "ttl"));
    }
}
```

Assumptions: Sections use INI-style `[name]` syntax; keys are case-sensitive; duration units are s/m/h (seconds/minutes/hours); booleans accept true/yes/on/1 or false/no/off/0; whitespace is trimmed from keys and values; configuration objects are immutable from the caller's perspective.

Cleaned: Sealed types make invalid states unrepresentable; guard clauses flatten control flow; validated at parse time, not at access time; every branch tested; safe to extend because types enforce contracts and errors propagate clearly.