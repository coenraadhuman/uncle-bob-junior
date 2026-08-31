import java.util.*;

public class ConfigurationParser {
    private final List<String> lines;
    private final ConfigSchema schema;
    private final List<String> errors = new ArrayList<>();
    
    public ConfigurationParser(String content, ConfigSchema schema) {
        this.lines = Arrays.asList(content.split("\n"));
        this.schema = schema;
    }
    
    public ParseResult parse() {
        Map<String, Object> values = new HashMap<>();
        String currentSection = null;
        
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            
            if (isCommentOrBlank(line)) {
                continue;
            }
            
            if (isSectionHeader(line)) {
                currentSection = extractSectionName(line);
                continue;
            }
            
            if (!isValidKeyValuePair(line)) {
                errors.add("Malformed line " + lineNumber + ": expected key=value format");
                continue;
            }
            
            parseKeyValue(line, currentSection, values, lineNumber);
        }
        
        if (!errors.isEmpty()) {
            return new ParseResult(null, errors);
        }
        
        Configuration config = new Configuration(values, schema);
        return new ParseResult(config, Collections.emptyList());
    }
    
    private boolean isCommentOrBlank(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
    
    private boolean isSectionHeader(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]");
    }
    
    private String extractSectionName(String line) {
        String trimmed = line.trim();
        return trimmed.substring(1, trimmed.length() - 1).trim();
    }
    
    private boolean isValidKeyValuePair(String line) {
        return line.trim().contains("=");
    }
    
    private void parseKeyValue(String line, String section, Map<String, Object> values, int lineNumber) {
        String trimmed = line.trim();
        String[] parts = trimmed.split("=", 2);
        String key = parts[0].trim();
        String rawValue = parts[1].trim();
        String fullKey = section != null ? section + "." + key : key;
        
        if (!schema.hasKey(fullKey)) {
            errors.add("Unknown key at line " + lineNumber + ": " + fullKey);
            return;
        }
        
        try {
            Object value = schema.parse(fullKey, rawValue);
            values.put(fullKey, value);
        } catch (IllegalArgumentException e) {
            errors.add("Invalid value at line " + lineNumber + " for key " + fullKey + ": " + e.getMessage());
        }
    }
}
