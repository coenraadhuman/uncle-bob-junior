```java
import java.util.*;
import java.util.regex.*;

// Assumptions:
// - Duration units: s, m, h (seconds, minutes, hours)
// - Boolean values: true/false (case-insensitive)
// - Comments: # to end of line
// - Section and key names are case-sensitive
// - Each key can appear once per section; duplicates overwrite silently

sealed interface ConfigValue permits IntValue, BooleanValue, DurationValue {}

record IntValue(int value) implements ConfigValue {}
record BooleanValue(boolean value) implements ConfigValue {}
record DurationValue(long millis) implements ConfigValue {}

record KeySpec(String name, KeyType type, ConfigValue defaultValue) {}

enum KeyType { INT, BOOLEAN, DURATION }

class SectionSpec {
    private final String name;
    private final Map<String, KeySpec> keys = new LinkedHashMap<>();
    
    SectionSpec(String name) {
        this.name = name;
    }
    
    void addKey(KeySpec spec) {
        keys.put(spec.name, spec);
    }
    
    String name() { return name; }
    KeySpec key(String name) { return keys.get(name); }
    boolean hasKey(String name) { return keys.containsKey(name); }
    Map<String, KeySpec> keys() { return keys; }
}

class ConfigSchema {
    private final Map<String, SectionSpec> sections = new LinkedHashMap<>();
    
    void addSection(SectionSpec spec) {
        sections.put(spec.name(), spec);
    }
    
    SectionSpec section(String name) { return sections.get(name); }
    boolean hasSection(String name) { return sections.containsKey(name); }
    Collection<SectionSpec> sections() { return sections.values(); }
}

class Configuration {
    private final Map<String, Map<String, ConfigValue>> data;
    
    Configuration(Map<String, Map<String, ConfigValue>> data) {
        this.data = new HashMap<>(data);
    }
    
    int getInt(String section, String key) {
        IntValue v = (IntValue) value(section, key);
        return v.value();
    }
    
    boolean getBoolean(String section, String key) {
        BooleanValue v = (BooleanValue) value(section, key);
        return v.value();
    }
    
    long getDurationMillis(String section, String key) {
        DurationValue v = (DurationValue) value(section, key);
        return v.millis();
    }
    
    private ConfigValue value(String section, String key) {
        Map<String, ConfigValue> sec = data.get(section);
        if (sec == null) throw new NoSuchElementException("section: " + section);
        ConfigValue val = sec.get(key);
        if (val == null) throw new NoSuchElementException("key: " + key);
        return val;
    }
}

class ValidationException extends Exception {
    ValidationException(String message) {
        super(message);
    }
}

class ConfigParser {
    private final ConfigSchema schema;
    private final List<String> errors = new ArrayList<>();
    
    ConfigParser(ConfigSchema schema) {
        this.schema = schema;
    }
    
    Configuration parse(String content) throws ValidationException {
        String[] lines = content.split("\n");
        Map<String, Map<String, ConfigValue>> config = new LinkedHashMap<>();
        String[] currentSection = new String[1];
        
        for (int i = 0; i < lines.length; i++) {
            parseLine(lines[i], i + 1, currentSection, config);
        }
        
        applyDefaults(config);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("\n", errors));
        }
        
        return new Configuration(config);
    }
    
    private void parseLine(String raw, int lineNum, String[] currentSection, Map<String, Map<String, ConfigValue>> config) {
        String line = stripCommentAndTrim(raw);
        if (line.isEmpty()) return;
        
        if (isSection(line)) {
            parseSection(line, lineNum, currentSection, config);
            return;
        }
        
        if (currentSection[0] == null) {
            errors.add("Line " + lineNum + ": key=value outside any section");
            return;
        }
        
        parsePair(line, lineNum, currentSection[0], config);
    }
    
    private void parseSection(String line, int lineNum, String[] currentSection, Map<String, Map<String, ConfigValue>> config) {
        String name = extractSection(line);
        if (!schema.hasSection(name)) {
            errors.add("Line " + lineNum + ": unknown section '" + name + "'");
            currentSection[0] = null;
        } else {
            currentSection[0] = name;
            config.putIfAbsent(name, new LinkedHashMap<>());
        }
    }
    
    private String stripCommentAndTrim(String line) {
        int hash = line.indexOf('#');
        String content = hash >= 0 ? line.substring(0, hash) : line;
        return content.trim();
    }
    
    private boolean isSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }
    
    private String extractSection(String line) {
        return line.substring(1, line.length() - 1).trim();
    }
    
    private void parsePair(String line, int lineNum, String section, Map<String, Map<String, ConfigValue>> config) {
        int eq = line.indexOf('=');
        if (eq <= 0 || eq >= line.length() - 1) {
            errors.add("Line " + lineNum + ": malformed key=value");
            return;
        }
        
        String key = line.substring(0, eq).trim();
        String rawValue = line.substring(eq + 1).trim();
        
        SectionSpec spec = schema.section(section);
        if (!spec.hasKey(key)) {
            errors.add("Line " + lineNum + ": unknown key '" + key + "'");
            return;
        }
        
        KeySpec keySpec = spec.key(key);
        ConfigValue parsed = parseValue(rawValue, keySpec.type(), lineNum, key);
        if (parsed != null) {
            config.get(section).put(key, parsed);
        }
    }
    
    private ConfigValue parseValue(String raw, KeyType type, int lineNum, String key) {
        try {
            return switch (type) {
                case INT -> new IntValue(Integer.parseInt(raw));
                case BOOLEAN -> parseBoolean(raw);
                case DURATION -> parseDuration(raw);
            };
        } catch (NumberFormatException | IllegalArgumentException e) {
            errors.add("Line " + lineNum + ": invalid " + type + " '" + raw + "' for key '" + key + "'");
            return null;
        }
    }
    
    private BooleanValue parseBoolean(String raw) {
        if ("true".equalsIgnoreCase(raw)) return new BooleanValue(true);
        if ("false".equalsIgnoreCase(raw)) return new BooleanValue(false);
        throw new IllegalArgumentException("not a boolean");
    }
    
    private DurationValue parseDuration(String raw) {
        Pattern p = Pattern.compile("^(\\d+)([smh])$");
        Matcher m = p.matcher(raw);
        if (!m.matches()) throw new IllegalArgumentException("invalid format");
        
        long num = Long.parseLong(m.group(1));
        char unit = m.group(2).charAt(0);
        long millis = switch (unit) {
            case 's' -> num * 1_000L;
            case 'm' -> num * 60_000L;
            case 'h' -> num * 3_600_000L;
            default -> throw new IllegalArgumentException("unknown unit");
        };
        return new DurationValue(millis);
    }
    
    private void applyDefaults(Map<String, Map<String, ConfigValue>> config) {
        for (SectionSpec sectionSpec : schema.sections()) {
            String sectionName = sectionSpec.name();
            Map<String, ConfigValue> sectionValues = config.get(sectionName);
            
            for (KeySpec keySpec : sectionSpec.keys().values()) {
                sectionValues.putIfAbsent(keySpec.name(), keySpec.defaultValue());
            }
        }
    }
}
```

Tests:

```java
class ConfigParserTest {
    
    static ConfigSchema schema() {
        ConfigSchema schema = new ConfigSchema();
        
        SectionSpec db = new SectionSpec("database");
        db.addKey(new KeySpec("port", KeyType.INT, new IntValue(5432)));
        db.addKey(new KeySpec("timeout", KeyType.DURATION, new DurationValue(30_000)));
        db.addKey(new KeySpec("debug", KeyType.BOOLEAN, new BooleanValue(false)));
        schema.addSection(db);
        
        SectionSpec app = new SectionSpec("app");
        app.addKey(new KeySpec("retries", KeyType.INT, new IntValue(3)));
        schema.addSection(app);
        
        return schema;
    }
    
    static void testParseInt() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\nport = 9999");
        assert cfg.getInt("database", "port") == 9999;
    }
    
    static void testParseBoolean() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\ndebug = true");
        assert cfg.getBoolean("database", "debug");
    }
    
    static void testParseDuration() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\ntimeout = 2m");
        assert cfg.getDurationMillis("database", "timeout") == 120_000;
    }
    
    static void testDurationUnits() throws ValidationException {
        ConfigSchema s = schema();
        assert new ConfigParser(s).parse("[database]\ntimeout = 30s").getDurationMillis("database", "timeout") == 30_000;
        assert new ConfigParser(s).parse("[database]\ntimeout = 5m").getDurationMillis("database", "timeout") == 300_000;
        assert new ConfigParser(s).parse("[database]\ntimeout = 1h").getDurationMillis("database", "timeout") == 3_600_000;
    }
    
    static void testDefaults() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\n[app]");
        assert cfg.getInt("database", "port") == 5432;
        assert cfg.getInt("app", "retries") == 3;
        assert !cfg.getBoolean("database", "debug");
    }
    
    static void testComments() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("# header\n[database]\nport = 1234  # inline");
        assert cfg.getInt("database", "port") == 1234;
    }
    
    static void testMultipleSections() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\nport = 1111\n[app]\nretries = 5");
        assert cfg.getInt("database", "port") == 1111;
        assert cfg.getInt("app", "retries") == 5;
    }
    
    static void testBlankLines() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("\n[database]\n\nport = 8888\n");
        assert cfg.getInt("database", "port") == 8888;
    }
    
    static void testErrorUnknownSection() {
        try { new ConfigParser(schema()).parse("[unknown]"); assert false; } 
        catch (ValidationException e) { assert e.getMessage().contains("unknown section") && e.getMessage().contains("Line 1"); }
    }
    
    static void testErrorUnknownKey() {
        try { new ConfigParser(schema()).parse("[database]\nunknown = 5"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("unknown key") && e.getMessage().contains("Line 2"); }
    }
    
    static void testErrorMalformed() {
        try { new ConfigParser(schema()).parse("[database]\nno_equals"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("malformed") && e.getMessage().contains("Line 2"); }
    }
    
    static void testErrorInvalidInt() {
        try { new ConfigParser(schema()).parse("[database]\nport = abc"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid INT"); }
    }
    
    static void testErrorInvalidBoolean() {
        try { new ConfigParser(schema()).parse("[database]\ndebug = maybe"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid BOOLEAN"); }
    }
    
    static void testErrorInvalidDuration() {
        try { new ConfigParser(schema()).parse("[database]\ntimeout = 30x"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid DURATION"); }
    }
    
    static void testErrorOutsideSection() {
        try { new ConfigParser(schema()).parse("port = 5432"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("outside any section") && e.getMessage().contains("Line 1"); }
    }
    
    public static void main(String[] args) {
        try {
            testParseInt();
            testParseBoolean();
            testParseDuration();
            testDurationUnits();
            testDefaults();
            testComments();
            testMultipleSections();
            testBlankLines();
            System.out.println("✓ 8 success cases");
        } catch (Exception e) { System.err.println("✗ " + e); e.printStackTrace(); }
        
        try {
            testErrorUnknownSection();
            testErrorUnknownKey();
            testErrorMalformed();
            testErrorInvalidInt();
            testErrorInvalidBoolean();
            testErrorInvalidDuration();
            testErrorOutsideSection();
            System.out.println("✓ 7 error cases");
        } catch (AssertionError e) { System.err.println("✗ " + e); e.printStackTrace(); }
    }
}
```

**Design:** Parser uses sealed interface `ConfigValue` with type-specific subclasses for type safety. `ConfigSchema` defines sections and keys with their types and defaults; `ConfigParser` validates lines and collects errors with line numbers before throwing. Parsing yields `Configuration` with typed getters (`getInt`, `getBoolean`, `getDurationMillis`) so callers never work with untyped maps. Guard clauses on malformed lines and unknown keys; defaults applied after parsing.