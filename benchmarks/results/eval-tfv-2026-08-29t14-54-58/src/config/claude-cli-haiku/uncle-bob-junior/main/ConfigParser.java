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
