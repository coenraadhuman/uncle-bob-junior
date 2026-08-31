I'll create a complete configuration parser with strong typing and validation. Assumptions:
- Sections and keys are case-sensitive
- Duration units: s (seconds), m (minutes), h (hours), d (days)
- Booleans are true/false (case-insensitive)
- Comments start with # and remove to end of line (including inline)
- Unknown keys are errors only if you define a valid key set for that section
- Missing keys fall back to defaults, otherwise throw on access
- Type errors occur at getter time, not parse time, allowing graceful degradation

```java
import java.util.*;
import java.util.regex.*;

public class Duration {
    private final long millis;
    
    private Duration(long millis) {
        this.millis = millis;
    }
    
    public static Duration parse(String value) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = pattern.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + value);
        }
        
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        long millis = switch (unit) {
            case "s" -> amount * 1000;
            case "m" -> amount * 60 * 1000;
            case "h" -> amount * 60 * 60 * 1000;
            case "d" -> amount * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        return new Duration(millis);
    }
    
    public long toMillis() { return millis; }
    public long toSeconds() { return millis / 1000; }
    
    @Override
    public String toString() { return millis + "ms"; }
}

public class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
    }
}

public class Configuration {
    private final Map<String, Map<String, Object>> data = new HashMap<>();
    
    void put(String section, String key, Object value) {
        data.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
    }
    
    public int getInt(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Integer) return (Integer) value;
        throw new ConfigException("Expected int for " + section + "." + key);
    }
    
    public boolean getBoolean(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Boolean) return (Boolean) value;
        throw new ConfigException("Expected boolean for " + section + "." + key);
    }
    
    public Duration getDuration(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Duration) return (Duration) value;
        throw new ConfigException("Expected duration for " + section + "." + key);
    }
    
    public String getString(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof String) return (String) value;
        throw new ConfigException("Expected string for " + section + "." + key);
    }
    
    private Object get(String section, String key) throws ConfigException {
        Map<String, Object> sectionData = data.get(section);
        if (sectionData == null) throw new ConfigException("Unknown section: " + section);
        Object value = sectionData.get(key);
        if (value == null) throw new ConfigException("Missing key: " + section + "." + key);
        return value;
    }
}

public class ConfigParser {
    private final Map<String, Map<String, Object>> defaults;
    private final Map<String, Set<String>> validKeys;
    
    public ConfigParser() {
        this.defaults = new HashMap<>();
        this.validKeys = new HashMap<>();
    }
    
    public ConfigParser addDefaults(String section, String key, Object value) {
        defaults.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
        return this;
    }
    
    public ConfigParser addValidKey(String section, String key) {
        validKeys.computeIfAbsent(section, k -> new HashSet<>()).add(key);
        return this;
    }
    
    public Configuration parse(String input) throws ConfigException {
        Configuration config = new Configuration();
        
        for (var entry : defaults.entrySet()) {
            for (var kv : entry.getValue().entrySet()) {
                config.put(entry.getKey(), kv.getKey(), kv.getValue());
            }
        }
        
        String[] lines = input.split("\n", -1);
        String currentSection = null;
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            int actualLineNum = lineNum + 1;
            
            int commentIdx = line.indexOf('#');
            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }
            
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                if (currentSection.isEmpty()) {
                    throw new ConfigException("Empty section name", actualLineNum);
                }
                continue;
            }
            
            if (currentSection == null) {
                throw new ConfigException("Key-value pair before any section", actualLineNum);
            }
            
            int eqIdx = line.indexOf('=');
            if (eqIdx < 0) {
                throw new ConfigException("Malformed line (no '=' found)", actualLineNum);
            }
            
            String key = line.substring(0, eqIdx).trim();
            String valueStr = line.substring(eqIdx + 1).trim();
            
            if (key.isEmpty()) {
                throw new ConfigException("Empty key", actualLineNum);
            }
            if (valueStr.isEmpty()) {
                throw new ConfigException("Empty value", actualLineNum);
            }
            
            Set<String> sectionKeys = validKeys.get(currentSection);
            if (sectionKeys != null && !sectionKeys.contains(key)) {
                throw new ConfigException("Unknown key '" + key + "' in section '" + currentSection + "'", actualLineNum);
            }
            
            Object value;
            try {
                value = parseValue(valueStr);
            } catch (IllegalArgumentException e) {
                throw new ConfigException("Invalid value: " + e.getMessage(), actualLineNum);
            }
            
            config.put(currentSection, key, value);
        }
        
        return config;
    }
    
    private Object parseValue(String valueStr) throws IllegalArgumentException {
        if ("true".equalsIgnoreCase(valueStr)) return true;
        if ("false".equalsIgnoreCase(valueStr)) return false;
        
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) { }
        
        if (valueStr.matches("\\d+[smhd]")) {
            return Duration.parse(valueStr);
        }
        
        return valueStr;
    }
}
```

```java
public class ConfigExample {
    public static void main(String[] args) {
        String config = """
            [server]
            port = 8080
            timeout = 30s
            debug = true
            
            [database]
            maxConnections = 50
            connectionTimeout = 5m
            enabled = false
            """;
        
        ConfigParser parser = new ConfigParser()
            .addValidKey("server", "port")
            .addValidKey("server", "timeout")
            .addValidKey("server", "debug")
            .addValidKey("database", "maxConnections")
            .addValidKey("database", "connectionTimeout")
            .addValidKey("database", "enabled")
            .addDefaults("server", "port", 3000)
            .addDefaults("server", "debug", false);
        
        try {
            Configuration cfg = parser.parse(config);
            System.out.println("port: " + cfg.getInt("server", "port"));
            System.out.println("timeout: " + cfg.getDuration("server", "timeout").toSeconds() + "s");
            System.out.println("debug: " + cfg.getBoolean("server", "debug"));
            System.out.println("maxConnections: " + cfg.getInt("database", "maxConnections"));
        } catch (ConfigException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```