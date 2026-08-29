import java.util.*;
import java.util.regex.*;

public class ConfigurationParser {
    
    sealed interface ConfigValue permits IntValue, BooleanValue, DurationValue {}
    
    static final class IntValue implements ConfigValue {
        final int value;
        IntValue(int value) { this.value = value; }
        @Override public String toString() { return String.valueOf(value); }
    }
    
    static final class BooleanValue implements ConfigValue {
        final boolean value;
        BooleanValue(boolean value) { this.value = value; }
        @Override public String toString() { return String.valueOf(value); }
    }
    
    static final class DurationValue implements ConfigValue {
        final long seconds;
        DurationValue(long seconds) { this.seconds = seconds; }
        @Override public String toString() { return seconds + "s"; }
    }
    
    static class ConfigSchema {
        private final Map<String, Map<String, ConfigValue>> defaults = new HashMap<>();
        
        void addDefault(String section, String key, ConfigValue value) {
            defaults.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
        }
        
        boolean isKnownKey(String section, String key) {
            Map<String, ConfigValue> sectionDefaults = defaults.get(section);
            return sectionDefaults != null && sectionDefaults.containsKey(key);
        }
        
        ConfigValue getDefault(String section, String key) {
            Map<String, ConfigValue> sectionDefaults = defaults.get(section);
            return sectionDefaults != null ? sectionDefaults.get(key) : null;
        }
    }
    
    public static class Configuration {
        private final Map<String, Map<String, ConfigValue>> data = new HashMap<>();
        
        void set(String section, String key, ConfigValue value) {
            data.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
        }
        
        ConfigValue get(String section, String key) {
            Map<String, ConfigValue> sectionData = data.get(section);
            return sectionData != null ? sectionData.get(key) : null;
        }
        
        public int getInt(String section, String key) {
            ConfigValue v = get(section, key);
            if (v instanceof IntValue iv) return iv.value;
            throw new IllegalArgumentException("Not an int: " + section + "." + key);
        }
        
        public boolean getBoolean(String section, String key) {
            ConfigValue v = get(section, key);
            if (v instanceof BooleanValue bv) return bv.value;
            throw new IllegalArgumentException("Not a boolean: " + section + "." + key);
        }
        
        public long getDurationSeconds(String section, String key) {
            ConfigValue v = get(section, key);
            if (v instanceof DurationValue dv) return dv.seconds;
            throw new IllegalArgumentException("Not a duration: " + section + "." + key);
        }
        
        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            for (var section : data.entrySet()) {
                sb.append("[").append(section.getKey()).append("]\n");
                for (var entry : section.getValue().entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            }
            return sb.toString();
        }
    }
    
    static class Parser {
        private final List<String> lines;
        private final ConfigSchema schema;
        private final List<String> errors = new ArrayList<>();
        private String currentSection;
        
        Parser(String text, ConfigSchema schema) {
            this.lines = Arrays.asList(text.split("\n", -1));
            this.schema = schema;
        }
        
        Configuration parse() {
            Configuration config = new Configuration();
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                int lineNum = i + 1;
                
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).trim();
                } else if (line.contains("=")) {
                    parseKeyValue(line, lineNum, config);
                } else {
                    errors.add("Line " + lineNum + ": malformed line");
                }
            }
            
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(String.join("\n", errors));
            }
            
            applyDefaults(config);
            return config;
        }
        
        private void parseKeyValue(String line, int lineNum, Configuration config) {
            int eqIdx = line.indexOf('=');
            String key = line.substring(0, eqIdx).trim();
            String value = line.substring(eqIdx + 1).trim();
            
            if (currentSection == null) {
                errors.add("Line " + lineNum + ": key-value without section");
                return;
            }
            
            if (!schema.isKnownKey(currentSection, key)) {
                errors.add("Line " + lineNum + ": unknown key '" + key + "' in section '" + currentSection + "'");
                return;
            }
            
            try {
                ConfigValue parsed = parseValue(value);
                config.set(currentSection, key, parsed);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": invalid value for key '" + key + "': " + e.getMessage());
            }
        }
        
        private ConfigValue parseValue(String value) {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return new BooleanValue(Boolean.parseBoolean(value));
            }
            
            if (value.matches("\\d+[smhd]$")) {
                return parseDuration(value);
            }
            
            if (value.matches("-?\\d+")) {
                return new IntValue(Integer.parseInt(value));
            }
            
            throw new IllegalArgumentException("cannot parse as int, boolean, or duration");
        }
        
        private DurationValue parseDuration(String value) {
            Pattern p = Pattern.compile("(\\d+)([smhd])");
            Matcher m = p.matcher(value);
            if (!m.matches()) throw new IllegalArgumentException("invalid duration");
            
            long amount = Long.parseLong(m.group(1));
            char unit = m.group(2).charAt(0);
            
            long seconds = switch (unit) {
                case 's' -> amount;
                case 'm' -> amount * 60;
                case 'h' -> amount * 3600;
                case 'd' -> amount * 86400;
                default -> throw new IllegalArgumentException("unknown unit");
            };
            
            return new DurationValue(seconds);
        }
        
        private void applyDefaults(Configuration config) {
            for (var section : schema.defaults.entrySet()) {
                for (var entry : section.getValue().entrySet()) {
                    if (config.get(section.getKey(), entry.getKey()) == null) {
                        config.set(section.getKey(), entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }
    
    public static Configuration parse(String text, ConfigSchema schema) {
        return new Parser(text, schema).parse();
    }
    
    public static void main(String[] args) {
        ConfigSchema schema = new ConfigSchema();
        schema.addDefault("server", "port", new IntValue(8080));
        schema.addDefault("server", "enabled", new BooleanValue(true));
        schema.addDefault("server", "timeout", new DurationValue(30));
        schema.addDefault("logging", "level", new IntValue(2));
        schema.addDefault("logging", "retention", new DurationValue(86400));
        
        String configText = """
            [server]
            port=9000
            enabled=false
            timeout=60s
            
            # Server section complete
            [logging]
            level=3
            retention=7d
            """;
        
        Configuration config = parse(configText, schema);
        System.out.println(config);
        System.out.println("server.port=" + config.getInt("server", "port"));
        System.out.println("server.timeout=" + config.getDurationSeconds("server", "timeout") + "s");
        System.out.println("logging.retention=" + config.getDurationSeconds("logging", "retention") + "s");
    }
}
