import java.util.*;
import java.util.regex.*;

public class ConfigParser {
    
    public static class ConfigurationException extends Exception {
        public final int lineNumber;
        
        public ConfigurationException(String message, int lineNumber) {
            super("Line " + lineNumber + ": " + message);
            this.lineNumber = lineNumber;
        }
        
        public ConfigurationException(String message) {
            super(message);
            this.lineNumber = -1;
        }
    }
    
    public enum ValueType {
        INTEGER, BOOLEAN, DURATION
    }
    
    public static class ConfigSchema {
        private final Map<String, Map<String, KeyDefinition>> sections = new HashMap<>();
        
        public class KeyDefinition {
            public final String name;
            public final ValueType type;
            public final Object defaultValue;
            
            KeyDefinition(String name, ValueType type, Object defaultValue) {
                this.name = name;
                this.type = type;
                this.defaultValue = defaultValue;
            }
        }
        
        public void addKey(String section, String key, ValueType type, Object defaultValue) {
            sections.computeIfAbsent(section, k -> new HashMap<>())
                   .put(key, new KeyDefinition(key, type, defaultValue));
        }
    }
    
    public static class Configuration {
        private final Map<String, Map<String, Object>> data = new HashMap<>();
        
        private void set(String section, String key, Object value) {
            data.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
        }
        
        public Integer getInt(String section, String key) {
            return (Integer) data.getOrDefault(section, new HashMap<>()).get(key);
        }
        
        public Boolean getBoolean(String section, String key) {
            return (Boolean) data.getOrDefault(section, new HashMap<>()).get(key);
        }
        
        public Long getDuration(String section, String key) {
            return (Long) data.getOrDefault(section, new HashMap<>()).get(key);
        }
    }
    
    private static long parseDuration(String value, int lineNumber) 
            throws ConfigurationException {
        Pattern pattern = Pattern.compile("^(\\d+)([smh])$");
        Matcher matcher = pattern.matcher(value.trim());
        
        if (!matcher.matches()) {
            throw new ConfigurationException("Invalid duration format: " + value, lineNumber);
        }
        
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        
        return switch (unit) {
            case "s" -> amount * 1000;
            case "m" -> amount * 60 * 1000;
            case "h" -> amount * 60 * 60 * 1000;
            default -> throw new ConfigurationException("Unknown duration unit: " + unit, lineNumber);
        };
    }
    
    private static boolean parseBoolean(String value) throws NumberFormatException {
        return switch (value.toLowerCase()) {
            case "true", "yes", "1", "on" -> true;
            case "false", "no", "0", "off" -> false;
            default -> throw new NumberFormatException("Invalid boolean: " + value);
        };
    }
    
    public static Configuration parse(String text, ConfigSchema schema) 
            throws ConfigurationException {
        Configuration config = new Configuration();
        String[] lines = text.split("\n", -1);
        String currentSection = null;
        
        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i];
            
            int commentIdx = line.indexOf('#');
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx);
            }
            
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                if (!schema.sections.containsKey(currentSection)) {
                    throw new ConfigurationException(
                        "Unknown section: " + currentSection, lineNumber);
                }
                continue;
            }
            
            int eqIdx = line.indexOf('=');
            if (eqIdx == -1) {
                throw new ConfigurationException("Malformed line, expected key=value", lineNumber);
            }
            
            String key = line.substring(0, eqIdx).trim();
            String value = line.substring(eqIdx + 1).trim();
            
            if (key.isEmpty() || value.isEmpty()) {
                throw new ConfigurationException("Empty key or value", lineNumber);
            }
            
            if (currentSection == null) {
                throw new ConfigurationException("Key-value pair outside of section", lineNumber);
            }
            
            Map<String, ConfigSchema.KeyDefinition> sectionDefs = 
                schema.sections.get(currentSection);
            
            if (!sectionDefs.containsKey(key)) {
                throw new ConfigurationException("Unknown key: " + key, lineNumber);
            }
            
            ConfigSchema.KeyDefinition def = sectionDefs.get(key);
            Object parsedValue;
            
            try {
                parsedValue = switch (def.type) {
                    case INTEGER -> Integer.parseInt(value);
                    case BOOLEAN -> parseBoolean(value);
                    case DURATION -> parseDuration(value, lineNumber);
                };
            } catch (NumberFormatException e) {
                throw new ConfigurationException(
                    "Invalid " + def.type.name().toLowerCase() + " value: " + value, lineNumber);
            }
            
            config.set(currentSection, key, parsedValue);
        }
        
        for (Map.Entry<String, Map<String, ConfigSchema.KeyDefinition>> sectionEntry :
             schema.sections.entrySet()) {
            String section = sectionEntry.getKey();
            for (ConfigSchema.KeyDefinition def : sectionEntry.getValue().values()) {
                if (config.data.getOrDefault(section, new HashMap<>()).get(def.name) == null 
                    && def.defaultValue != null) {
                    config.set(section, def.name, def.defaultValue);
                }
            }
        }
        
        return config;
    }
}
