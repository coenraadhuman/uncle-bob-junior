import java.util.*;

class ConfigParser {
    private final Map<String, ConfigKey> schema;
    
    ConfigParser(Map<String, ConfigKey> schema) {
        this.schema = new HashMap<>(schema);
    }
    
    Configuration parse(String content) {
        Map<String, ConfigValue> values = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();
        
        String[] lines = content.split("\n");
        String currentSection = "";
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            int displayLineNum = lineNum + 1;
            String line = lines[lineNum].trim();
            
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1);
                continue;
            }
            
            if (!line.contains("=")) {
                errors.add(new ValidationError(displayLineNum, "Malformed line, missing '='"));
                continue;
            }
            
            String[] parts = line.split("=", 2);
            String keyName = parts[0].trim();
            String rawValue = parts[1].trim();
            String fullKey = currentSection.isEmpty() ? keyName : currentSection + "." + keyName;
            
            ConfigKey keySpec = schema.get(fullKey);
            if (keySpec == null) {
                errors.add(new ValidationError(displayLineNum, "Unknown key: " + fullKey));
                continue;
            }
            
            try {
                ConfigValue parsed = parseValue(rawValue, keySpec.expectedType());
                values.put(fullKey, parsed);
            } catch (Exception e) {
                String typeName = keySpec.expectedType().getSimpleName();
                errors.add(new ValidationError(displayLineNum, "Invalid " + typeName + ": " + rawValue));
            }
        }
        
        for (Map.Entry<String, ConfigKey> entry : schema.entrySet()) {
            if (!values.containsKey(entry.getKey())) {
                values.put(entry.getKey(), entry.getValue().defaultValue());
            }
        }
        
        return new Configuration(values, errors);
    }
    
    private ConfigValue parseValue(String rawValue, Class<? extends ConfigValue> type) 
            throws Exception {
        if (type == IntValue.class) {
            return new IntValue(Integer.parseInt(rawValue));
        } else if (type == BoolValue.class) {
            return new BoolValue(parseBoolean(rawValue));
        } else if (type == DurationValue.class) {
            return new DurationValue(parseDuration(rawValue));
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
    
    private boolean parseBoolean(String value) throws Exception {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new Exception("Invalid boolean");
        };
    }
    
    private long parseDuration(String value) throws Exception {
        String lower = value.toLowerCase().trim();
        if (lower.endsWith("s")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1));
        } else if (lower.endsWith("m")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1)) * 60L;
        } else if (lower.endsWith("h")) {
            return Long.parseLong(lower.substring(0, lower.length() - 1)) * 3600L;
        }
        throw new Exception("Invalid duration format");
    }
}
