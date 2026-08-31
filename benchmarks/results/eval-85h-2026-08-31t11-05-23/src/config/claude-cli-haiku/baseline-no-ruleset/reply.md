# Configuration Language Parser

**Assumptions:**
- Keys and section names are case-insensitive.
- Booleans accept: `true`/`false`/`yes`/`no`.
- Durations: integers with unit suffix (`ms`, `s`, `m`, `h`), returned as milliseconds (long).
- Schema is predefined with defaults; validation errors don't halt parsing—all errors collected and thrown together.
- If a section header is present, keys must belong to that section.
- Comments and blank lines ignored; malformed lines flagged with line number.

```java
import java.util.*;
import java.util.regex.*;

public class ConfigParser {
    
    public static class Config {
        private final Map<String, Integer> ints = new HashMap<>();
        private final Map<String, Boolean> bools = new HashMap<>();
        private final Map<String, Long> durations = new HashMap<>();
        
        public int getInt(String key) {
            Integer val = ints.get(key);
            return val != null ? val : 0;
        }
        
        public boolean getBoolean(String key) {
            Boolean val = bools.get(key);
            return val != null ? val : false;
        }
        
        public long getDuration(String key) {
            Long val = durations.get(key);
            return val != null ? val : 0L;
        }
        
        void setInt(String key, int value) { ints.put(key, value); }
        void setBoolean(String key, boolean value) { bools.put(key, value); }
        void setDuration(String key, long value) { durations.put(key, value); }
    }
    
    private enum ValueType { INTEGER, BOOLEAN, DURATION }
    
    private static class SchemaEntry {
        final String section;
        final ValueType type;
        final Object defaultValue;
        
        SchemaEntry(String section, ValueType type, Object defaultValue) {
            this.section = section;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }
    
    public static class ValidationError {
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
    
    public static class ParseException extends Exception {
        public final List<ValidationError> errors;
        
        public ParseException(List<ValidationError> errors) {
            super("Configuration parsing failed with " + errors.size() + " error(s)");
            this.errors = errors;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(getMessage() + "\n");
            for (ValidationError e : errors) {
                sb.append("  ").append(e).append("\n");
            }
            return sb.toString();
        }
    }
    
    private static final Map<String, SchemaEntry> SCHEMA = new HashMap<>();
    static {
        SCHEMA.put("port", new SchemaEntry("server", ValueType.INTEGER, 8080));
        SCHEMA.put("timeout", new SchemaEntry("server", ValueType.DURATION, 30000L));
        SCHEMA.put("debug", new SchemaEntry("server", ValueType.BOOLEAN, false));
        SCHEMA.put("retry_delay", new SchemaEntry("client", ValueType.DURATION, 5000L));
        SCHEMA.put("enable_cache", new SchemaEntry("client", ValueType.BOOLEAN, true));
    }
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final Pattern KV_PATTERN = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+?)\\s*$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)\\s*(ms|s|m|h)$");
    
    public static Config parse(String input) throws ParseException {
        List<ValidationError> errors = new ArrayList<>();
        Config config = new Config();
        Set<String> parsedKeys = new HashSet<>();
        String currentSection = null;
        
        String[] lines = input.split("\n", -1);
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            String trimmed = line.trim();
            
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            if (sectionMatcher.matches()) {
                currentSection = sectionMatcher.group(1).toLowerCase();
                continue;
            }
            
            Matcher kvMatcher = KV_PATTERN.matcher(line);
            if (!kvMatcher.matches()) {
                errors.add(new ValidationError(lineIdx + 1, "Malformed line"));
                continue;
            }
            
            String key = kvMatcher.group(1).toLowerCase();
            String value = kvMatcher.group(2).trim();
            
            if (!SCHEMA.containsKey(key)) {
                errors.add(new ValidationError(lineIdx + 1, "Unknown key: " + key));
                continue;
            }
            
            SchemaEntry schemaEntry = SCHEMA.get(key);
            
            if (currentSection != null && !currentSection.equals(schemaEntry.section)) {
                errors.add(new ValidationError(lineIdx + 1, 
                    "Key '" + key + "' belongs to [" + schemaEntry.section + "], not [" + currentSection + "]"));
                continue;
            }
            
            try {
                switch (schemaEntry.type) {
                    case INTEGER:
                        config.setInt(key, Integer.parseInt(value));
                        break;
                    case BOOLEAN:
                        config.setBoolean(key, parseBoolean(value));
                        break;
                    case DURATION:
                        config.setDuration(key, parseDuration(value));
                        break;
                }
                parsedKeys.add(key);
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(lineIdx + 1, 
                    "Invalid " + schemaEntry.type.toString().toLowerCase() + " value: " + value));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ParseException(errors);
        }
        
        for (Map.Entry<String, SchemaEntry> entry : SCHEMA.entrySet()) {
            if (!parsedKeys.contains(entry.getKey())) {
                SchemaEntry schema = entry.getValue();
                switch (schema.type) {
                    case INTEGER:
                        config.setInt(entry.getKey(), (Integer) schema.defaultValue);
                        break;
                    case BOOLEAN:
                        config.setBoolean(entry.getKey(), (Boolean) schema.defaultValue);
                        break;
                    case DURATION:
                        config.setDuration(entry.getKey(), (Long) schema.defaultValue);
                        break;
                }
            }
        }
        
        return config;
    }
    
    private static boolean parseBoolean(String value) {
        String lower = value.toLowerCase();
        if (lower.equals("true") || lower.equals("yes")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("no")) {
            return false;
        }
        throw new IllegalArgumentException("Not a valid boolean");
    }
    
    private static long parseDuration(String value) {
        Matcher matcher = DURATION_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a valid duration");
        }
        
        long number = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        
        return switch (unit) {
            case "ms" -> number;
            case "s" -> number * 1000;
            case "m" -> number * 60 * 1000;
            case "h" -> number * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
    
    public static void main(String[] args) {
        String input = """
            [server]
            port = 9000
            timeout = 2m
            debug = true
            
            # Client section
            [client]
            enable_cache = false
            retry_delay = 10s
            """;
        
        try {
            Config config = parse(input);
            System.out.println("port: " + config.getInt("port"));
            System.out.println("timeout: " + config.getDuration("timeout") + " ms");
            System.out.println("debug: " + config.getBoolean("debug"));
            System.out.println("enable_cache: " + config.getBoolean("enable_cache"));
            System.out.println("retry_delay: " + config.getDuration("retry_delay") + " ms");
        } catch (ParseException e) {
            System.out.println(e);
        }
    }
}
```

The `Config` class holds typed values; parsing returns it directly, not a string map. The `SCHEMA` map defines all valid keys with their section, type, and default. All validation errors are collected before throwing, so users see all issues at once.