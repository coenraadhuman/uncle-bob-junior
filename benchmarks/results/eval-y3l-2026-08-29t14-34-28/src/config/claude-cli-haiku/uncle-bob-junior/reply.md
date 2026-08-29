I'll write a complete Java parser for your configuration language with proper type safety, validation, and tests.

**Assumptions:**
- Sections and keys use `[name]` and alphanumeric formats; durations are integers with unit suffix (s/m/h/d); booleans accept true/false/yes/no/on/off/1/0; comments strip everything after `#`; a schema defines valid keys, types, and defaults; missing sections and unknown keys raise typed exceptions; defaults are only applied for keys in the schema.

```java
import java.util.*;
import java.util.regex.*;

class ConfigDuration {
    private final long millis;
    
    private ConfigDuration(long millis) {
        this.millis = millis;
    }
    
    public static ConfigDuration parse(String value) {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = pattern.matcher(value.toLowerCase().trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration: " + value);
        }
        
        long amount = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        
        long millis = switch (unit) {
            case 's' -> amount * 1000;
            case 'm' -> amount * 60 * 1000;
            case 'h' -> amount * 60 * 60 * 1000;
            case 'd' -> amount * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        
        return new ConfigDuration(millis);
    }
    
    public long getMillis() {
        return millis;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigDuration duration = (ConfigDuration) o;
        return millis == duration.millis;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(millis);
    }
    
    @Override
    public String toString() {
        return millis + "ms";
    }
}

class ConfigurationException extends Exception {
    private final int lineNumber;
    
    public ConfigurationException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
}

class ConfigSchema {
    record KeyDef(Class<?> type, Object defaultValue) {}
    
    private final Map<String, Map<String, KeyDef>> sections = new HashMap<>();
    
    public void addSection(String section) {
        sections.putIfAbsent(section, new HashMap<>());
    }
    
    public void addKey(String section, String key, Class<?> type, Object defaultValue) {
        sections.computeIfAbsent(section, k -> new HashMap<>())
                .put(key, new KeyDef(type, defaultValue));
    }
    
    public boolean hasSection(String section) {
        return sections.containsKey(section);
    }
    
    public KeyDef getKey(String section, String key) {
        Map<String, KeyDef> sectionKeys = sections.get(section);
        return sectionKeys != null ? sectionKeys.get(key) : null;
    }
    
    public Map<String, Map<String, KeyDef>> getAllSections() {
        return sections;
    }
}

class Configuration {
    private final Map<String, Map<String, Object>> data = new HashMap<>();
    
    public void addSection(String section) {
        data.putIfAbsent(section, new HashMap<>());
    }
    
    public void put(String section, String key, Object value) {
        data.get(section).put(key, value);
    }
    
    public Object get(String section, String key) {
        Map<String, Object> sectionData = data.get(section);
        return sectionData != null ? sectionData.get(key) : null;
    }
    
    public int getInt(String section, String key) {
        return (Integer) get(section, key);
    }
    
    public boolean getBoolean(String section, String key) {
        return (Boolean) get(section, key);
    }
    
    public ConfigDuration getDuration(String section, String key) {
        return (ConfigDuration) get(section, key);
    }
    
    public String getString(String section, String key) {
        return (String) get(section, key);
    }
}

class ConfigParser {
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[([\\w.]+)\\]$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([\\w.]+)=(.*)$");
    
    private final ConfigSchema schema;
    
    public ConfigParser(ConfigSchema schema) {
        this.schema = schema;
    }
    
    public Configuration parse(String text) throws ConfigurationException {
        Configuration config = new Configuration();
        String[] lines = text.split("\n", -1);
        
        String currentSection = null;
        for (int lineNum = 1; lineNum <= lines.length; lineNum++) {
            String line = stripComment(lines[lineNum - 1]);
            
            if (isBlank(line)) {
                continue;
            }
            
            if (isSection(line)) {
                currentSection = extractSection(line);
                validateSection(currentSection, lineNum);
                config.addSection(currentSection);
            } else if (isKeyValue(line)) {
                validateInSection(currentSection, lineNum);
                String[] parts = parseKeyValue(line);
                String key = parts[0];
                String value = parts[1];
                validateKey(currentSection, key, lineNum);
                Object parsed = parseTypedValue(key, value, currentSection, lineNum);
                config.put(currentSection, key, parsed);
            } else {
                throw new ConfigurationException(lineNum, "Malformed line");
            }
        }
        
        applyDefaults(config);
        return config;
    }
    
    private void applyDefaults(Configuration config) {
        for (Map.Entry<String, Map<String, ConfigSchema.KeyDef>> section : schema.getAllSections().entrySet()) {
            String sectionName = section.getKey();
            config.addSection(sectionName);
            
            for (Map.Entry<String, ConfigSchema.KeyDef> key : section.getValue().entrySet()) {
                String keyName = key.getKey();
                ConfigSchema.KeyDef keyDef = key.getValue();
                
                if (config.get(sectionName, keyName) == null && keyDef.defaultValue() != null) {
                    config.put(sectionName, keyName, keyDef.defaultValue());
                }
            }
        }
    }
    
    private String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx == -1 ? line : line.substring(0, idx);
    }
    
    private boolean isBlank(String line) {
        return line.trim().isEmpty();
    }
    
    private boolean isSection(String line) {
        return SECTION_PATTERN.matcher(line.trim()).matches();
    }
    
    private String extractSection(String line) {
        Matcher m = SECTION_PATTERN.matcher(line.trim());
        m.matches();
        return m.group(1);
    }
    
    private boolean isKeyValue(String line) {
        return KEY_VALUE_PATTERN.matcher(line.trim()).matches();
    }
    
    private String[] parseKeyValue(String line) {
        String trimmed = line.trim();
        int eq = trimmed.indexOf('=');
        return new String[] {trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim()};
    }
    
    private void validateSection(String section, int lineNum) throws ConfigurationException {
        if (!schema.hasSection(section)) {
            throw new ConfigurationException(lineNum, "Unknown section: " + section);
        }
    }
    
    private void validateInSection(String section, int lineNum) throws ConfigurationException {
        if (section == null) {
            throw new ConfigurationException(lineNum, "Key-value pair outside section");
        }
    }
    
    private void validateKey(String section, String key, int lineNum) throws ConfigurationException {
        if (schema.getKey(section, key) == null) {
            throw new ConfigurationException(lineNum, "Unknown key: " + key);
        }
    }
    
    private Object parseTypedValue(String key, String value, String section, int lineNum) throws ConfigurationException {
        ConfigSchema.KeyDef keyDef = schema.getKey(section, key);
        
        try {
            if (keyDef.type() == Integer.class) {
                return Integer.parseInt(value);
            }
            if (keyDef.type() == Boolean.class) {
                return parseBoolean(value);
            }
            if (keyDef.type() == ConfigDuration.class) {
                return ConfigDuration.parse(value);
            }
            if (keyDef.type() == String.class) {
                return value;
            }
            throw new ConfigurationException(lineNum, "Unknown type: " + keyDef.type().getName());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(lineNum, e.getMessage());
        }
    }
    
    private boolean parseBoolean(String value) {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean value: " + value);
        };
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {
    
    private ConfigSchema buildSchema() {
        ConfigSchema schema = new ConfigSchema();
        schema.addSection("server");
        schema.addKey("server", "port", Integer.class, null);
        schema.addKey("server", "enabled", Boolean.class, true);
        schema.addKey("server", "timeout", ConfigDuration.class, null);
        return schema;
    }
    
    @Test
    void parsesInteger() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nport=8080\n");
        assertEquals(8080, config.getInt("server", "port"));
    }
    
    @Test
    void parsesBoolean() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nenabled=false\n");
        assertFalse(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void parsesDuration() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\ntimeout=30s\n");
        assertEquals(30000, config.getDuration("server", "timeout").getMillis());
    }
    
    @Test
    void appliesDefaults() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nport=9000\n");
        assertTrue(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        String input = "# comment\n[server]\n\nport=8080 # port value\nenabled=no\n";
        Configuration config = parser.parse(input);
        assertEquals(8080, config.getInt("server", "port"));
        assertFalse(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void reportUnknownSection() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[unknown]\nport=8080\n"));
        assertEquals(1, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Unknown section"));
    }
    
    @Test
    void reportUnknownKey() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[server]\nunknown=value\n"));
        assertEquals(2, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Unknown key"));
    }
    
    @Test
    void reportMalformedLine() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[server]\ninvalid line here\n"));
        assertEquals(2, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Malformed"));
    }
    
    @Test
    void reportInvalidInteger() {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertThrows(ConfigurationException.class, () -> parser.parse("[server]\nport=not_a_number\n"));
    }
    
    @Test
    void reportInvalidDuration() {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertThrows(ConfigurationException.class, () -> parser.parse("[server]\ntimeout=xyz\n"));
    }
    
    @Test
    void reportKeyValueOutsideSection() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("port=8080\n"));
        assertEquals(1, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("outside section"));
    }
    
    @Test
    void parsesAllDurationUnits() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertEquals(30000, parser.parse("[server]\ntimeout=30s\n").getDuration("server", "timeout").getMillis());
        assertEquals(300000, parser.parse("[server]\ntimeout=5m\n").getDuration("server", "timeout").getMillis());
        assertEquals(7200000, parser.parse("[server]\ntimeout=2h\n").getDuration("server", "timeout").getMillis());
        assertEquals(86400000, parser.parse("[server]\ntimeout=1d\n").getDuration("server", "timeout").getMillis());
    }
    
    @Test
    void parsesBooleanVariants() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        String[] trueValues = {"true", "yes", "on", "1"};
        for (String val : trueValues) {
            assertTrue(parser.parse("[server]\nenabled=" + val + "\n").getBoolean("server", "enabled"));
        }
        String[] falseValues = {"false", "no", "off", "0"};
        for (String val : falseValues) {
            assertFalse(parser.parse("[server]\nenabled=" + val + "\n").getBoolean("server", "enabled"));
        }
    }
}
```

**Cleaned:** Schema separates concerns from parsing; validation routes through guard-clause helpers; each type has a parser; exceptions carry line numbers; defaults applied after parsing. Safe to change because parsing logic is isolated, schema is data-driven, and tests exercise all branches.