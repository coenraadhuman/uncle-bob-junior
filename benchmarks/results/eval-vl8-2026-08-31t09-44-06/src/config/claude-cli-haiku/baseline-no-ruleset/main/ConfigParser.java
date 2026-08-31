import java.util.*;

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
