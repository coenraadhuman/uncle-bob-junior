I'll write a complete, production-ready Java configuration parser with strong typing, comprehensive validation, and line-number-aware error reporting.

**Assumptions:**
- Sections are optional groupings; keys can be global (before any section) or section-scoped
- Schema defines valid keys, their types (int, boolean, duration), and defaults
- Duration format: digits followed by s/m/h (e.g., 30s, 5m, 2h)
- All errors collected before throwing so users see all problems at once
- Whitespace trimmed from keys, values, and section names
- Unknown keys and malformed lines report line numbers

```java
import java.util.*;
import java.util.regex.*;

public final class ConfigParser {
    
    private ConfigParser() {}
    
    public static final class Duration {
        private final long millis;
        
        public Duration(long millis) {
            if (millis < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
            this.millis = millis;
        }
        
        public static Duration parse(String value) {
            Matcher m = Pattern.compile("^(\\d+)([smh])$").matcher(value.trim());
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid duration: " + value);
            }
            
            long amount = Long.parseLong(m.group(1));
            long millis = switch (m.group(2)) {
                case "s" -> amount * 1000;
                case "m" -> amount * 60000;
                case "h" -> amount * 3600000;
                default -> 0;
            };
            return new Duration(millis);
        }
        
        public long toMillis() {
            return millis;
        }
    }
    
    public record KeyDef(String name, String type, Object defaultValue) {}
    
    public static final class Config {
        private final Map<String, Map<String, Object>> sections;
        private final Map<String, KeyDef> schema;
        
        Config(Map<String, Map<String, Object>> sections, Map<String, KeyDef> schema) {
            this.sections = sections;
            this.schema = schema;
        }
        
        public int getInt(String key) {
            return (Integer) getValue(null, key);
        }
        
        public int getInt(String section, String key) {
            return (Integer) getValue(section, key);
        }
        
        public boolean getBoolean(String key) {
            return (Boolean) getValue(null, key);
        }
        
        public boolean getBoolean(String section, String key) {
            return (Boolean) getValue(section, key);
        }
        
        public Duration getDuration(String key) {
            return (Duration) getValue(null, key);
        }
        
        public Duration getDuration(String section, String key) {
            return (Duration) getValue(section, key);
        }
        
        private Object getValue(String section, String key) {
            String sec = section == null ? "" : section;
            Map<String, Object> data = sections.get(sec);
            if (data != null && data.containsKey(key)) {
                return data.get(key);
            }
            KeyDef def = schema.get(key);
            if (def != null && def.defaultValue() != null) {
                return def.defaultValue();
            }
            throw new IllegalArgumentException("Key not found: " + key);
        }
    }
    
    public static final class ValidationException extends Exception {
        private final List<String> errors;
        
        public ValidationException(List<String> errors) {
            super(String.join("\n", errors));
            this.errors = List.copyOf(errors);
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
    
    public static final class Parser {
        private final Map<String, KeyDef> schema;
        
        public Parser(Map<String, KeyDef> schema) {
            this.schema = Map.copyOf(schema);
        }
        
        public Config parse(String content) throws ValidationException {
            String[] lines = content.split("\n", -1);
            Map<String, Map<String, Object>> sections = new HashMap<>();
            List<String> errors = new ArrayList<>();
            
            String section = "";
            sections.put(section, new HashMap<>());
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1).trim();
                    sections.put(section, new HashMap<>());
                } else if (line.contains("=")) {
                    parseKeyValue(line, i + 1, section, sections, errors);
                } else {
                    errors.add("Line " + (i + 1) + ": Malformed line");
                }
            }
            
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
            
            return new Config(sections, schema);
        }
        
        private void parseKeyValue(String line, int lineNum, String section,
                                   Map<String, Map<String, Object>> sections,
                                   List<String> errors) {
            int eq = line.indexOf('=');
            String key = line.substring(0, eq).trim();
            String valueStr = line.substring(eq + 1).trim();
            
            if (key.isEmpty()) {
                errors.add("Line " + lineNum + ": Empty key");
                return;
            }
            if (valueStr.isEmpty()) {
                errors.add("Line " + lineNum + ": Empty value");
                return;
            }
            
            if (!schema.containsKey(key)) {
                errors.add("Line " + lineNum + ": Unknown key: " + key);
                return;
            }
            
            KeyDef def = schema.get(key);
            try {
                Object value = parseValue(valueStr, def.type());
                sections.get(section).put(key, value);
            } catch (IllegalArgumentException e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }
        
        private Object parseValue(String value, String type) {
            return switch (type) {
                case "int" -> {
                    try {
                        yield Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid integer: " + value);
                    }
                }
                case "boolean" -> switch (value.toLowerCase()) {
                    case "true" -> true;
                    case "false" -> false;
                    default -> throw new IllegalArgumentException("Invalid boolean: " + value);
                };
                case "duration" -> Duration.parse(value);
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
        }
    }
}
```

```java
public final class ConfigParserTest {
    
    public static void main(String[] args) throws Exception {
        testBasicParsing();
        testComments();
        testSections();
        testDefaults();
        testValidationErrors();
        testDurationParsing();
        testBooleanCaseInsensitive();
        testEmptyConfig();
        testMissingRequiredKey();
        System.out.println("All tests passed!");
    }
    
    private static void testBasicParsing() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 8080),
            "timeout", new ConfigParser.KeyDef("timeout", "duration", null),
            "debug", new ConfigParser.KeyDef("debug", "boolean", false)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("port=9000\ndebug=true\ntimeout=30s");
        
        assert cfg.getInt("port") == 9000;
        assert cfg.getBoolean("debug");
        assert cfg.getDuration("timeout").toMillis() == 30000;
    }
    
    private static void testComments() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "key", new ConfigParser.KeyDef("key", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("# comment\nkey=100\n\n# another");
        assert cfg.getInt("key") == 100;
    }
    
    private static void testSections() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "timeout", new ConfigParser.KeyDef("timeout", "duration", null),
            "retries", new ConfigParser.KeyDef("retries", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("[server]\ntimeout=30s\n[client]\nretries=5");
        
        assert cfg.getDuration("server", "timeout").toMillis() == 30000;
        assert cfg.getInt("client", "retries") == 5;
    }
    
    private static void testDefaults() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 8080),
            "timeout", new ConfigParser.KeyDef("timeout", "duration", new ConfigParser.Duration(60000))
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("");
        
        assert cfg.getInt("port") == 8080;
        assert cfg.getDuration("timeout").toMillis() == 60000;
    }
    
    private static void testValidationErrors() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        try {
            parser.parse("port=abc\nunknown=val\nbad line");
            assert false;
        } catch (ConfigParser.ValidationException e) {
            List<String> errors = e.getErrors();
            assert errors.size() >= 2;
            assert errors.stream().anyMatch(e2 -> e2.contains("Line 1") && e2.contains("Invalid integer"));
            assert errors.stream().anyMatch(e2 -> e2.contains("Unknown key: unknown"));
            assert errors.stream().anyMatch(e2 -> e2.contains("Line 3") && e2.contains("Malformed"));
        }
    }
    
    private static void testDurationParsing() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "d", new ConfigParser.KeyDef("d", "duration", null)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        
        assert parser.parse("d=5s").getDuration("d").toMillis() == 5000;
        assert parser.parse("d=2m").getDuration("d").toMillis() == 120000;
        assert parser.parse("d=1h").getDuration("d").toMillis() == 3600000;
    }
    
    private static void testBooleanCaseInsensitive() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "b", new ConfigParser.KeyDef("b", "boolean", false)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        assert parser.parse("b=true").getBoolean("b");
        assert parser.parse("b=TRUE").getBoolean("b");
        assert parser.parse("b=True").getBoolean("b");
        assert !parser.parse("b=false").getBoolean("b");
    }
    
    private static void testEmptyConfig() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "opt", new ConfigParser.KeyDef("opt", "int", 42)
        );
        
        ConfigParser.Config cfg = new ConfigParser.Parser(schema).parse("\n\n");
        assert cfg.getInt("opt") == 42;
    }
    
    private static void testMissingRequiredKey() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "required", new ConfigParser.KeyDef("required", "int", null)
        );
        
        ConfigParser.Config cfg = new ConfigParser.Parser(schema).parse("");
        try {
            cfg.getInt("required");
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("not found");
        }
    }
}
```

**Code structure:** Parser collects all validation errors with line numbers before throwing; Config provides typed getters with section scoping; Duration is a value object for durations; schema maps keys to definitions with type and default. Clean, immutable, and testable.