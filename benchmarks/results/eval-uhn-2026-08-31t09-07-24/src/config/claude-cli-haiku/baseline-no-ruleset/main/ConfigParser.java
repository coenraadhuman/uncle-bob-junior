import java.util.*;
import java.util.regex.*;

public class ConfigParser {
    
    public static class ValidationError extends Exception {
        public final int lineNumber;
        
        public ValidationError(int lineNumber, String message) {
            super("Line " + lineNumber + ": " + message);
            this.lineNumber = lineNumber;
        }
    }
    
    public static class Configuration {
        private final Map<String, Map<String, Object>> sections = new HashMap<>();
        
        private void set(String section, String key, Object value) {
            sections.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
        }
        
        private Object get(String section, String key) {
            Map<String, Object> sectionMap = sections.get(section);
            return sectionMap != null ? sectionMap.get(key) : null;
        }
        
        public int getInt(String section, String key, int defaultValue) {
            Object val = get(section, key);
            return val != null ? (Integer) val : defaultValue;
        }
        
        public boolean getBoolean(String section, String key, boolean defaultValue) {
            Object val = get(section, key);
            return val != null ? (Boolean) val : defaultValue;
        }
        
        public long getDuration(String section, String key, long defaultMillis) {
            Object val = get(section, key);
            return val != null ? (Long) val : defaultMillis;
        }
    }
    
    private final Set<String> knownKeys;
    
    public ConfigParser(String... knownKeys) {
        this.knownKeys = new HashSet<>(Arrays.asList(knownKeys));
    }
    
    public Configuration parse(String content) throws ValidationError {
        Configuration config = new Configuration();
        String[] lines = content.split("\n", -1);
        String currentSection = null;
        
        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i];
            
            int commentIdx = line.indexOf('#');
            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }
            
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                if (currentSection.isEmpty()) {
                    throw new ValidationError(lineNumber, "Empty section name");
                }
                continue;
            }
            
            if (currentSection == null) {
                throw new ValidationError(lineNumber, "Key-value pair before any section");
            }
            
            int eqIdx = line.indexOf('=');
            if (eqIdx <= 0) {
                throw new ValidationError(lineNumber, "Malformed line: expected key=value");
            }
            
            String key = line.substring(0, eqIdx).trim();
            String valueStr = line.substring(eqIdx + 1).trim();
            
            if (key.isEmpty()) {
                throw new ValidationError(lineNumber, "Empty key");
            }
            
            if (valueStr.isEmpty()) {
                throw new ValidationError(lineNumber, "Empty value");
            }
            
            if (!knownKeys.contains(key)) {
                throw new ValidationError(lineNumber, "Unknown key: " + key);
            }
            
            Object parsedValue = parseValue(valueStr, lineNumber);
            config.set(currentSection, key, parsedValue);
        }
        
        return config;
    }
    
    private Object parseValue(String valueStr, int lineNumber) throws ValidationError {
        if ("true".equalsIgnoreCase(valueStr)) {
            return true;
        }
        if ("false".equalsIgnoreCase(valueStr)) {
            return false;
        }
        
        if (valueStr.matches("-?\\d+")) {
            try {
                return Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                throw new ValidationError(lineNumber, "Invalid integer: " + valueStr);
            }
        }
        
        Pattern durationPattern = Pattern.compile("^(\\d+)([a-zA-Z]+)$");
        Matcher matcher = durationPattern.matcher(valueStr);
        if (matcher.matches()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            long millis = switch (unit) {
                case "ms" -> amount;
                case "s" -> amount * 1000;
                case "m" -> amount * 60 * 1000;
                case "h" -> amount * 60 * 60 * 1000;
                default -> throw new ValidationError(lineNumber, "Unknown duration unit: " + unit);
            };
            return millis;
        }
        
        throw new ValidationError(lineNumber, "Invalid value: " + valueStr);
    }
    
    public static void main(String[] args) throws ValidationError {
        String configText = """
            # Database config
            [database]
            timeout = 30s
            port = 5432
            debug = true
            
            [cache]
            ttl = 5m
            enabled = false
            max_size = 1024
            """;
        
        ConfigParser parser = new ConfigParser(
            "timeout", "port", "debug", "ttl", "enabled", "max_size"
        );
        
        Configuration config = parser.parse(configText);
        
        System.out.println("timeout: " + config.getDuration("database", "timeout", 0) + "ms");
        System.out.println("port: " + config.getInt("database", "port", 0));
        System.out.println("debug: " + config.getBoolean("database", "debug", false));
        System.out.println("ttl: " + config.getDuration("cache", "ttl", 0) + "ms");
        System.out.println("enabled: " + config.getBoolean("cache", "enabled", false));
        System.out.println("missing_key with default: " + config.getInt("cache", "missing_key", 999));
    }
}
