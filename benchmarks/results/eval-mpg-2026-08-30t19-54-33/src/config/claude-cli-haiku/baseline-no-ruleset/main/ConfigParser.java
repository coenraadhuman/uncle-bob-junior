import java.util.*;
import java.lang.reflect.Field;

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
