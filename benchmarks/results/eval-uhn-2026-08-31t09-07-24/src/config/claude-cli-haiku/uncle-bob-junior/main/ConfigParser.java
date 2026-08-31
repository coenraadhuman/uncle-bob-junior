import java.util.*;
import java.util.regex.*;

class ConfigParser {
    private final ConfigSchema schema;
    private final List<String> errors = new ArrayList<>();
    
    ConfigParser(ConfigSchema schema) {
        this.schema = schema;
    }
    
    Configuration parse(String content) throws ValidationException {
        String[] lines = content.split("\n");
        Map<String, Map<String, ConfigValue>> config = new LinkedHashMap<>();
        String[] currentSection = new String[1];
        
        for (int i = 0; i < lines.length; i++) {
            parseLine(lines[i], i + 1, currentSection, config);
        }
        
        applyDefaults(config);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("\n", errors));
        }
        
        return new Configuration(config);
    }
    
    private void parseLine(String raw, int lineNum, String[] currentSection, Map<String, Map<String, ConfigValue>> config) {
        String line = stripCommentAndTrim(raw);
        if (line.isEmpty()) return;
        
        if (isSection(line)) {
            parseSection(line, lineNum, currentSection, config);
            return;
        }
        
        if (currentSection[0] == null) {
            errors.add("Line " + lineNum + ": key=value outside any section");
            return;
        }
        
        parsePair(line, lineNum, currentSection[0], config);
    }
    
    private void parseSection(String line, int lineNum, String[] currentSection, Map<String, Map<String, ConfigValue>> config) {
        String name = extractSection(line);
        if (!schema.hasSection(name)) {
            errors.add("Line " + lineNum + ": unknown section '" + name + "'");
            currentSection[0] = null;
        } else {
            currentSection[0] = name;
            config.putIfAbsent(name, new LinkedHashMap<>());
        }
    }
    
    private String stripCommentAndTrim(String line) {
        int hash = line.indexOf('#');
        String content = hash >= 0 ? line.substring(0, hash) : line;
        return content.trim();
    }
    
    private boolean isSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }
    
    private String extractSection(String line) {
        return line.substring(1, line.length() - 1).trim();
    }
    
    private void parsePair(String line, int lineNum, String section, Map<String, Map<String, ConfigValue>> config) {
        int eq = line.indexOf('=');
        if (eq <= 0 || eq >= line.length() - 1) {
            errors.add("Line " + lineNum + ": malformed key=value");
            return;
        }
        
        String key = line.substring(0, eq).trim();
        String rawValue = line.substring(eq + 1).trim();
        
        SectionSpec spec = schema.section(section);
        if (!spec.hasKey(key)) {
            errors.add("Line " + lineNum + ": unknown key '" + key + "'");
            return;
        }
        
        KeySpec keySpec = spec.key(key);
        ConfigValue parsed = parseValue(rawValue, keySpec.type(), lineNum, key);
        if (parsed != null) {
            config.get(section).put(key, parsed);
        }
    }
    
    private ConfigValue parseValue(String raw, KeyType type, int lineNum, String key) {
        try {
            return switch (type) {
                case INT -> new IntValue(Integer.parseInt(raw));
                case BOOLEAN -> parseBoolean(raw);
                case DURATION -> parseDuration(raw);
            };
        } catch (NumberFormatException | IllegalArgumentException e) {
            errors.add("Line " + lineNum + ": invalid " + type + " '" + raw + "' for key '" + key + "'");
            return null;
        }
    }
    
    private BooleanValue parseBoolean(String raw) {
        if ("true".equalsIgnoreCase(raw)) return new BooleanValue(true);
        if ("false".equalsIgnoreCase(raw)) return new BooleanValue(false);
        throw new IllegalArgumentException("not a boolean");
    }
    
    private DurationValue parseDuration(String raw) {
        Pattern p = Pattern.compile("^(\\d+)([smh])$");
        Matcher m = p.matcher(raw);
        if (!m.matches()) throw new IllegalArgumentException("invalid format");
        
        long num = Long.parseLong(m.group(1));
        char unit = m.group(2).charAt(0);
        long millis = switch (unit) {
            case 's' -> num * 1_000L;
            case 'm' -> num * 60_000L;
            case 'h' -> num * 3_600_000L;
            default -> throw new IllegalArgumentException("unknown unit");
        };
        return new DurationValue(millis);
    }
    
    private void applyDefaults(Map<String, Map<String, ConfigValue>> config) {
        for (SectionSpec sectionSpec : schema.sections()) {
            String sectionName = sectionSpec.name();
            Map<String, ConfigValue> sectionValues = config.get(sectionName);
            
            for (KeySpec keySpec : sectionSpec.keys().values()) {
                sectionValues.putIfAbsent(keySpec.name(), keySpec.defaultValue());
            }
        }
    }
}
