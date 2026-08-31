I'll create a complete configuration parser with type safety, validation errors, and default fallbacks.

**Assumptions:**
- Values are integers, booleans (true/false/yes/no/1/0), or durations (30s/5m/1h format)
- Sections in brackets are parsed but only group visually—all keys are global to the config object
- Malformed lines are those missing `=`, empty keys, or unparseable values
- Unknown keys are validation errors
- Missing keys are silently filled with defaults defined in the schema
- The config object is a user-defined Java class with public fields matching the keys

```java
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;

public class Duration {
    private final long seconds;
    
    private Duration(long seconds) {
        this.seconds = seconds;
    }
    
    public static Duration parse(String value) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^(\\d+)([smh])$");
        Matcher matcher = pattern.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format (expected e.g. 30s, 5m, 1h)");
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        long totalSeconds = switch(unit) {
            case "s" -> amount;
            case "m" -> amount * 60;
            case "h" -> amount * 3600;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        return new Duration(totalSeconds);
    }
    
    public static Duration ofSeconds(long seconds) {
        return new Duration(seconds);
    }
    
    public long getSeconds() {
        return seconds;
    }
    
    @Override
    public String toString() {
        if (seconds % 3600 == 0) return (seconds / 3600) + "h";
        if (seconds % 60 == 0) return (seconds / 60) + "m";
        return seconds + "s";
    }
}

public class ValidationError {
    public final int lineNumber;
    public final String message;
    
    public ValidationError(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}

public class ParseResult<T> {
    public final T config;
    public final List<ValidationError> errors;
    
    public ParseResult(T config, List<ValidationError> errors) {
        this.config = config;
        this.errors = errors;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

public class ConfigParser {
    
    private static class KeyDef {
        String name;
        Class<?> type;
        Object defaultValue;
        
        KeyDef(String name, Class<?> type, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }
    
    private final Map<String, KeyDef> schema = new LinkedHashMap<>();
    
    public ConfigParser define(String key, Class<?> type, Object defaultValue) {
        schema.put(key, new KeyDef(key, type, defaultValue));
        return this;
    }
    
    public <T> ParseResult<T> parse(String text, T config) {
        List<ValidationError> errors = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        Set<String> providedKeys = new HashSet<>();
        
        for (int i = 0; i < lines.length; i++) {
            int lineNum = i + 1;
            String line = lines[i];
            
            int commentIdx = line.indexOf('#');
            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }
            line = line.trim();
            
            if (line.isEmpty()) continue;
            
            if (line.startsWith("[") && line.endsWith("]")) {
                continue;
            }
            
            int eqIdx = line.indexOf('=');
            if (eqIdx < 0) {
                errors.add(new ValidationError(lineNum, "Malformed line: missing '='"));
                continue;
            }
            
            String key = line.substring(0, eqIdx).trim();
            String value = line.substring(eqIdx + 1).trim();
            
            if (key.isEmpty()) {
                errors.add(new ValidationError(lineNum, "Malformed line: empty key"));
                continue;
            }
            
            KeyDef keyDef = schema.get(key);
            if (keyDef == null) {
                errors.add(new ValidationError(lineNum, "Unknown key: " + key));
                continue;
            }
            
            try {
                Object parsed = parseValue(value, keyDef.type);
                setField(config, key, parsed);
                providedKeys.add(key);
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(lineNum, 
                    "Invalid value for '" + key + "': " + e.getMessage()));
            }
        }
        
        for (KeyDef keyDef : schema.values()) {
            if (!providedKeys.contains(keyDef.name) && keyDef.defaultValue != null) {
                setField(config, keyDef.name, keyDef.defaultValue);
            }
        }
        
        return new ParseResult<>(config, errors);
    }
    
    private Object parseValue(String value, Class<?> type) throws IllegalArgumentException {
        if (type == Integer.class || type == int.class) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("not a valid integer");
            }
        } else if (type == Boolean.class || type == boolean.class) {
            return parseBoolean(value);
        } else if (type == Duration.class) {
            return Duration.parse(value);
        }
        throw new IllegalArgumentException("Unsupported type: " + type.getSimpleName());
    }
    
    private boolean parseBoolean(String value) throws IllegalArgumentException {
        return switch(value.toLowerCase()) {
            case "true", "yes", "1" -> true;
            case "false", "no", "0" -> false;
            default -> throw new IllegalArgumentException("expected true/false/yes/no/1/0");
        };
    }
    
    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}

public class AppConfig {
    public int port = 0;
    public boolean debug = false;
    public Duration timeout = null;
    public int maxConnections = 0;
    public boolean ssl = false;
    
    @Override
    public String toString() {
        return "AppConfig{port=" + port + ", debug=" + debug + ", timeout=" + timeout +
               ", maxConnections=" + maxConnections + ", ssl=" + ssl + '}';
    }
}

public class Main {
    public static void main(String[] args) {
        String configText = """
            # Server configuration
            [server]
            port=9000
            debug=true
            timeout=2m
            
            # Connection settings
            [connection]
            maxConnections=50
            ssl=yes
            
            # Missing keys use defaults
            # badKey=value
            """;
        
        ConfigParser parser = new ConfigParser()
            .define("port", int.class, 8080)
            .define("debug", boolean.class, false)
            .define("timeout", Duration.class, Duration.ofSeconds(30))
            .define("maxConnections", int.class, 100)
            .define("ssl", boolean.class, false);
        
        AppConfig config = new AppConfig();
        ParseResult<AppConfig> result = parser.parse(configText, config);
        
        System.out.println(result.config);
        if (result.hasErrors()) {
            System.out.println("\nValidation errors:");
            result.errors.forEach(System.out::println);
        }
    }
}
```

**Key features:**
- **Type-safe:** Returns a strongly typed config object, not a Map
- **Validation:** Collects all errors with precise line numbers
- **Defaults:** Missing keys automatically use defaults from the schema
- **Extensible:** Add new types by handling them in `parseValue()`
- **Robust:** Handles edge cases (empty keys, missing `=`, invalid values, unknown keys)