import java.util.*;
import java.util.regex.*;

class ConfigParser {
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[([\\w.]+)\\]$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([\\w.]+)=(.*)$");
    
    private final ConfigSchema schema;
    
    public ConfigParser(ConfigSchema schema) {
        this.schema = schema;
    }
    
    public Configuration parse(String text) throws ConfigurationException {
        Configuration config = new Configuration();
        String[] lines = text.split("\n", -1);
        
        String currentSection = null;
        for (int lineNum = 1; lineNum <= lines.length; lineNum++) {
            String line = stripComment(lines[lineNum - 1]);
            
            if (isBlank(line)) {
                continue;
            }
            
            if (isSection(line)) {
                currentSection = extractSection(line);
                validateSection(currentSection, lineNum);
                config.addSection(currentSection);
            } else if (isKeyValue(line)) {
                validateInSection(currentSection, lineNum);
                String[] parts = parseKeyValue(line);
                String key = parts[0];
                String value = parts[1];
                validateKey(currentSection, key, lineNum);
                Object parsed = parseTypedValue(key, value, currentSection, lineNum);
                config.put(currentSection, key, parsed);
            } else {
                throw new ConfigurationException(lineNum, "Malformed line");
            }
        }
        
        applyDefaults(config);
        return config;
    }
    
    private void applyDefaults(Configuration config) {
        for (Map.Entry<String, Map<String, ConfigSchema.KeyDef>> section : schema.getAllSections().entrySet()) {
            String sectionName = section.getKey();
            config.addSection(sectionName);
            
            for (Map.Entry<String, ConfigSchema.KeyDef> key : section.getValue().entrySet()) {
                String keyName = key.getKey();
                ConfigSchema.KeyDef keyDef = key.getValue();
                
                if (config.get(sectionName, keyName) == null && keyDef.defaultValue() != null) {
                    config.put(sectionName, keyName, keyDef.defaultValue());
                }
            }
        }
    }
    
    private String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx == -1 ? line : line.substring(0, idx);
    }
    
    private boolean isBlank(String line) {
        return line.trim().isEmpty();
    }
    
    private boolean isSection(String line) {
        return SECTION_PATTERN.matcher(line.trim()).matches();
    }
    
    private String extractSection(String line) {
        Matcher m = SECTION_PATTERN.matcher(line.trim());
        m.matches();
        return m.group(1);
    }
    
    private boolean isKeyValue(String line) {
        return KEY_VALUE_PATTERN.matcher(line.trim()).matches();
    }
    
    private String[] parseKeyValue(String line) {
        String trimmed = line.trim();
        int eq = trimmed.indexOf('=');
        return new String[] {trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim()};
    }
    
    private void validateSection(String section, int lineNum) throws ConfigurationException {
        if (!schema.hasSection(section)) {
            throw new ConfigurationException(lineNum, "Unknown section: " + section);
        }
    }
    
    private void validateInSection(String section, int lineNum) throws ConfigurationException {
        if (section == null) {
            throw new ConfigurationException(lineNum, "Key-value pair outside section");
        }
    }
    
    private void validateKey(String section, String key, int lineNum) throws ConfigurationException {
        if (schema.getKey(section, key) == null) {
            throw new ConfigurationException(lineNum, "Unknown key: " + key);
        }
    }
    
    private Object parseTypedValue(String key, String value, String section, int lineNum) throws ConfigurationException {
        ConfigSchema.KeyDef keyDef = schema.getKey(section, key);
        
        try {
            if (keyDef.type() == Integer.class) {
                return Integer.parseInt(value);
            }
            if (keyDef.type() == Boolean.class) {
                return parseBoolean(value);
            }
            if (keyDef.type() == ConfigDuration.class) {
                return ConfigDuration.parse(value);
            }
            if (keyDef.type() == String.class) {
                return value;
            }
            throw new ConfigurationException(lineNum, "Unknown type: " + keyDef.type().getName());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(lineNum, e.getMessage());
        }
    }
    
    private boolean parseBoolean(String value) {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean value: " + value);
        };
    }
}
