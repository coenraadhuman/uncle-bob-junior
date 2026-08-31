import java.util.*;
import java.util.regex.*;

public class ConfigParser {
    private final String input;
    private final Map<String, ConfigKey> schema;
    private final List<String> errors = new ArrayList<>();
    
    public ConfigParser(String input, Map<String, ConfigKey> schema) {
        this.input = input;
        this.schema = schema;
    }
    
    public Configuration parse() throws ConfigValidationException {
        Map<String, Object> values = new HashMap<>(getDefaults());
        String[] lines = input.split("\n", -1);
        String currentSection = null;
        
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            int lineNumber = lineIdx + 1;
            
            line = removeComment(line).trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                continue;
            }
            
            if (!line.contains("=")) {
                errors.add("Line " + lineNumber + ": malformed line (expected key=value)");
                continue;
            }
            
            int eqIdx = line.indexOf('=');
            String key = line.substring(0, eqIdx).trim();
            String value = line.substring(eqIdx + 1).trim();
            
            if (key.isEmpty() || value.isEmpty()) {
                errors.add("Line " + lineNumber + ": malformed line (empty key or value)");
                continue;
            }
            
            String fullKey = currentSection != null ? currentSection + "." + key : key;
            ConfigKey configKey = schema.get(fullKey);
            if (configKey == null) {
                configKey = schema.get(key);
            }
            
            if (configKey == null) {
                errors.add("Line " + lineNumber + ": unknown key '" + key + "'");
                continue;
            }
            
            try {
                Object parsed = parseTypedValue(value, configKey.type());
                values.put(configKey.name(), parsed);
            } catch (Exception e) {
                errors.add("Line " + lineNumber + ": invalid " + configKey.type().name().toLowerCase() + 
                           " value for key '" + key + "': " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ConfigValidationException(errors);
        }
        
        return new Configuration(values);
    }
    
    private String removeComment(String line) {
        int hashIdx = line.indexOf('#');
        return hashIdx == -1 ? line : line.substring(0, hashIdx);
    }
    
    private Object parseTypedValue(String value, ConfigType type) throws Exception {
        return switch (type) {
            case INTEGER -> parseInteger(value);
            case BOOLEAN -> parseBoolean(value);
            case DURATION -> parseDuration(value);
        };
    }
    
    private int parseInteger(String value) throws Exception {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new Exception("not a valid integer");
        }
    }
    
    private boolean parseBoolean(String value) throws Exception {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new Exception("not a valid boolean");
        };
    }
    
    private long parseDuration(String value) throws Exception {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            throw new Exception("expected <number><unit> (s/m/h/d)");
        }
        long amount = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        return switch (unit) {
            case 's' -> amount;
            case 'm' -> amount * 60;
            case 'h' -> amount * 3600;
            case 'd' -> amount * 86400;
            default -> throw new Exception("unknown unit");
        };
    }
    
    private Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        for (ConfigKey key : schema.values()) {
            if (key.defaultValue() != null) {
                defaults.put(key.name(), key.defaultValue());
            }
        }
        return defaults;
    }
}
