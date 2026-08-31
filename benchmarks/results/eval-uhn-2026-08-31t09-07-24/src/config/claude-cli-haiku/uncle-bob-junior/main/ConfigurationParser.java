import java.util.*;
import java.util.regex.*;

public class ConfigurationParser {
    
    public static class ConfigurationException extends Exception {
        public final List<String> errors;
        
        public ConfigurationException(List<String> errors) {
            super();
            this.errors = List.copyOf(errors);
        }
        
        @Override
        public String getMessage() {
            return "Configuration errors:\n  " + String.join("\n  ", errors);
        }
    }
    
    public static class Configuration {
        private final Map<String, Object> values;
        
        private Configuration(Map<String, Object> values) {
            this.values = Map.copyOf(values);
        }
        
        public int getInt(String key) {
            Object value = values.get(key);
            if (value == null) throw new IllegalArgumentException("Missing key: " + key);
            return (Integer) value;
        }
        
        public boolean getBoolean(String key) {
            Object value = values.get(key);
            if (value == null) throw new IllegalArgumentException("Missing key: " + key);
            return (Boolean) value;
        }
        
        public long getDuration(String key) {
            Object value = values.get(key);
            if (value == null) throw new IllegalArgumentException("Missing key: " + key);
            return (Long) value;
        }
    }
    
    private static final long SECOND_MS = 1_000L;
    private static final long MINUTE_MS = 60L * SECOND_MS;
    private static final long HOUR_MS = 60L * MINUTE_MS;
    private static final long DAY_MS = 24L * HOUR_MS;
    
    public static Configuration parse(String content, Map<String, Object> defaults) throws ConfigurationException {
        List<String> errors = new ArrayList<>();
        Map<String, Object> parsed = new HashMap<>(defaults);
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;
            
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                continue;
            }
            
            int eqIndex = trimmed.indexOf('=');
            if (eqIndex == -1) {
                errors.add("Line " + lineNum + ": malformed line, missing =");
                continue;
            }
            
            String key = trimmed.substring(0, eqIndex).trim();
            String rawValue = trimmed.substring(eqIndex + 1).trim();
            
            if (key.isEmpty()) {
                errors.add("Line " + lineNum + ": empty key");
                continue;
            }
            
            Object defaultValue = defaults.get(key);
            if (defaultValue == null) {
                errors.add("Line " + lineNum + ": unknown key '" + key + "'");
                continue;
            }
            
            try {
                Object parsedValue = parseValue(rawValue, defaultValue.getClass());
                parsed.put(key, parsedValue);
            } catch (IllegalArgumentException e) {
                errors.add("Line " + lineNum + ": invalid value for '" + key + "': " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ConfigurationException(errors);
        }
        
        return new Configuration(parsed);
    }
    
    private static Object parseValue(String value, Class<?> expectedType) {
        if (expectedType == Boolean.class) {
            return parseBoolean(value);
        }
        if (expectedType == Integer.class) {
            return parseInt(value);
        }
        if (expectedType == Long.class) {
            return parseDuration(value);
        }
        throw new IllegalArgumentException("unsupported type: " + expectedType.getSimpleName());
    }
    
    private static boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on")) {
            return true;
        }
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off")) {
            return false;
        }
        throw new IllegalArgumentException("not a boolean");
    }
    
    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not an integer");
        }
    }
    
    private static long parseDuration(String value) {
        Pattern durationPattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = durationPattern.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a duration (format: 30s, 5m, 2h, 1d)");
        }
        
        long amount = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        return switch (unit) {
            case 's' -> amount * SECOND_MS;
            case 'm' -> amount * MINUTE_MS;
            case 'h' -> amount * HOUR_MS;
            case 'd' -> amount * DAY_MS;
            default -> throw new IllegalArgumentException("unknown unit");
        };
    }
}
