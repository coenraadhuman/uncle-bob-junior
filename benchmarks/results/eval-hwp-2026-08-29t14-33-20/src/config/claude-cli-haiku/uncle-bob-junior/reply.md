Assumptions: Schema declares known keys upfront with their types; parsing collects all errors before returning (not failing fast); booleans accept true/false/yes/no/on/off (case-insensitive); durations use 's'/'m'/'h' suffixes; fullKey format is "section.key" internally; missing keys return the default provided at access time; unknown keys are validation errors.

```java
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([^=\\s]+)\\s*=\\s*(.+)$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d+[smh]$");
    
    public static class Config {
        private final Map<String, Integer> ints;
        private final Map<String, Boolean> booleans;
        private final Map<String, Duration> durations;
        private final List<String> errors;
        
        Config(Map<String, Integer> ints, Map<String, Boolean> booleans, 
               Map<String, Duration> durations, List<String> errors) {
            this.ints = Collections.unmodifiableMap(ints);
            this.booleans = Collections.unmodifiableMap(booleans);
            this.durations = Collections.unmodifiableMap(durations);
            this.errors = Collections.unmodifiableList(errors);
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public int getInt(String section, String key, int defaultValue) {
            return ints.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        public boolean getBoolean(String section, String key, boolean defaultValue) {
            return booleans.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        public Duration getDuration(String section, String key, Duration defaultValue) {
            return durations.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        private static String fullKey(String section, String key) {
            return section + "." + key;
        }
    }
    
    public static class Schema {
        enum ValueType { INTEGER, BOOLEAN, DURATION }
        
        record FieldSpec(String section, String key, ValueType type) {}
        
        private final Set<FieldSpec> fields = new HashSet<>();
        
        public Schema addInt(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.INTEGER));
            return this;
        }
        
        public Schema addBoolean(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.BOOLEAN));
            return this;
        }
        
        public Schema addDuration(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.DURATION));
            return this;
        }
        
        Optional<ValueType> typeOf(String section, String key) {
            return fields.stream()
                .filter(f -> f.section.equals(section) && f.key.equals(key))
                .map(f -> f.type)
                .findFirst();
        }
    }
    
    public static Config parse(String input, Schema schema) {
        Map<String, Integer> ints = new HashMap<>();
        Map<String, Boolean> booleans = new HashMap<>();
        Map<String, Duration> durations = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        String[] lines = input.split("\n", -1);
        String currentSection = null;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;
            
            if (isIgnored(line)) {
                continue;
            }
            
            Optional<String> section = parseSection(line);
            if (section.isPresent()) {
                currentSection = section.get();
                continue;
            }
            
            Optional<KeyValue> kv = parseKeyValue(line);
            if (kv.isPresent()) {
                processKeyValue(kv.get(), currentSection, lineNum, schema, ints, booleans, durations, errors);
                continue;
            }
            
            errors.add("Line " + lineNum + ": malformed line");
        }
        
        return new Config(ints, booleans, durations, errors);
    }
    
    private static boolean isIgnored(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
    
    private static Optional<String> parseSection(String line) {
        Matcher m = SECTION_PATTERN.matcher(line);
        return m.matches() ? Optional.of(m.group(1)) : Optional.empty();
    }
    
    private static class KeyValue {
        final String key;
        final String valueStr;
        
        KeyValue(String key, String valueStr) {
            this.key = key;
            this.valueStr = valueStr;
        }
    }
    
    private static Optional<KeyValue> parseKeyValue(String line) {
        Matcher m = KEY_VALUE_PATTERN.matcher(line);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new KeyValue(m.group(1), m.group(2).trim()));
    }
    
    private static void processKeyValue(KeyValue kv, String currentSection, int lineNum,
                                       Schema schema, Map<String, Integer> ints,
                                       Map<String, Boolean> booleans,
                                       Map<String, Duration> durations, List<String> errors) {
        if (currentSection == null) {
            errors.add("Line " + lineNum + ": key-value pair outside section");
            return;
        }
        
        Optional<Schema.ValueType> typeOpt = schema.typeOf(currentSection, kv.key);
        if (typeOpt.isEmpty()) {
            errors.add("Line " + lineNum + ": unknown key '" + kv.key + "' in section [" + currentSection + "]");
            return;
        }
        
        Schema.ValueType type = typeOpt.get();
        Optional<Object> value = parseValue(kv.valueStr, type);
        if (value.isEmpty()) {
            errors.add("Line " + lineNum + ": invalid " + type.name().toLowerCase() + " value '" + kv.valueStr + "'");
            return;
        }
        
        storeValue(currentSection, kv.key, value.get(), type, ints, booleans, durations);
    }
    
    private static Optional<Object> parseValue(String valueStr, Schema.ValueType type) {
        return switch (type) {
            case INTEGER -> parseInteger(valueStr);
            case BOOLEAN -> parseBoolean(valueStr);
            case DURATION -> parseDuration(valueStr);
        };
    }
    
    private static Optional<Object> parseInteger(String valueStr) {
        if (!INTEGER_PATTERN.matcher(valueStr).matches()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(valueStr));
    }
    
    private static Optional<Object> parseBoolean(String valueStr) {
        return switch (valueStr.toLowerCase()) {
            case "true", "yes", "on" -> Optional.of(true);
            case "false", "no", "off" -> Optional.of(false);
            default -> Optional.empty();
        };
    }
    
    private static Optional<Object> parseDuration(String valueStr) {
        if (!DURATION_PATTERN.matcher(valueStr).matches()) {
            return Optional.empty();
        }
        long amount = Long.parseLong(valueStr.substring(0, valueStr.length() - 1));
        char unit = valueStr.charAt(valueStr.length() - 1);
        Duration d = switch (unit) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            default -> null;
        };
        return Optional.ofNullable(d);
    }
    
    private static void storeValue(String section, String key, Object value, Schema.ValueType type,
                                  Map<String, Integer> ints, Map<String, Boolean> booleans,
                                  Map<String, Duration> durations) {
        String fullKey = section + "." + key;
        switch (type) {
            case INTEGER -> ints.put(fullKey, (Integer) value);
            case BOOLEAN -> booleans.put(fullKey, (Boolean) value);
            case DURATION -> durations.put(fullKey, (Duration) value);
        }
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;

class ConfigParserTest {
    
    @Test
    void parsesSimpleConfig() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("database", "port")
            .addBoolean("database", "enabled");
        
        String input = "[database]\nport = 5432\nenabled = true";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(5432, config.getInt("database", "port", -1));
        assertTrue(config.getBoolean("database", "enabled", false));
    }
    
    @Test
    void handlesBooleanFormats() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addBoolean("test", "v1")
            .addBoolean("test", "v2")
            .addBoolean("test", "v3")
            .addBoolean("test", "v4")
            .addBoolean("test", "v5")
            .addBoolean("test", "v6");
        
        String input = "[test]\nv1 = true\nv2 = yes\nv3 = on\nv4 = false\nv5 = no\nv6 = off";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertTrue(config.getBoolean("test", "v1", false));
        assertTrue(config.getBoolean("test", "v2", false));
        assertTrue(config.getBoolean("test", "v3", false));
        assertFalse(config.getBoolean("test", "v4", true));
        assertFalse(config.getBoolean("test", "v5", true));
        assertFalse(config.getBoolean("test", "v6", true));
    }
    
    @Test
    void parsesDurations() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addDuration("timeout", "read")
            .addDuration("timeout", "write")
            .addDuration("timeout", "cache");
        
        String input = "[timeout]\nread = 30s\nwrite = 5m\ncache = 1h";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(Duration.ofSeconds(30), config.getDuration("timeout", "read", null));
        assertEquals(Duration.ofMinutes(5), config.getDuration("timeout", "write", null));
        assertEquals(Duration.ofHours(1), config.getDuration("timeout", "cache", null));
    }
    
    @Test
    void reportsErrorForMalformedLine() {
        ConfigParser.Schema schema = new ConfigParser.Schema();
        
        String input = "[section]\nthis is malformed";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(1, config.getErrors().size());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
    }
    
    @Test
    void reportsErrorForUnknownKey() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "[db]\nhost = localhost\nport = 5432";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(1, config.getErrors().size());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
        assertTrue(config.getErrors().get(0).contains("unknown key"));
    }
    
    @Test
    void reportsErrorForInvalidType() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "[server]\nport = not_a_number";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
        assertTrue(config.getErrors().get(0).contains("invalid"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "# This is a comment\n\n[server]\n# another\nport = 8080\n\n# end";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(8080, config.getInt("server", "port", -1));
    }
    
    @Test
    void usesDefaultsForMissingKeys() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "[other]";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(5432, config.getInt("db", "port", 5432));
    }
    
    @Test
    void reportsErrorForKeyOutsideSection() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "port = 8080\n[db]";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertTrue(config.getErrors().get(0).contains("Line 1"));
        assertTrue(config.getErrors().get(0).contains("outside section"));
    }
    
    @Test
    void parsesNegativeIntegers() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("calc", "offset");
        
        String input = "[calc]\noffset = -42";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(-42, config.getInt("calc", "offset", 0));
    }
    
    @Test
    void trimsWhitespace() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "[server]  \n  port  =  8080  ";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(8080, config.getInt("server", "port", -1));
    }
    
    @Test
    void collectsMultipleErrors() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "bad line 1\n[db]\nbad line 2\nport = abc\nbad line 3";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(3, config.getErrors().size());
    }
}
```

Cleaned: Schema enforces type safety upfront; parsing is single-pass with error accumulation; each type has one parser; `processKeyValue` handles one concern. Safe to change because: type validation and defaults are stateless; errors always include line numbers; unknown keys and format errors are caught before storage; tests cover happy path, all type cases, invalid formats, and missing sections.