import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([^=\\s]+)\\s*=\\s*(.+)$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d+[smh]$");
    
    public static class Config {
        private final Map<String, Integer> ints;
        private final Map<String, Boolean> booleans;
        private final Map<String, Duration> durations;
        private final List<String> errors;
        
        Config(Map<String, Integer> ints, Map<String, Boolean> booleans, 
               Map<String, Duration> durations, List<String> errors) {
            this.ints = Collections.unmodifiableMap(ints);
            this.booleans = Collections.unmodifiableMap(booleans);
            this.durations = Collections.unmodifiableMap(durations);
            this.errors = Collections.unmodifiableList(errors);
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public int getInt(String section, String key, int defaultValue) {
            return ints.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        public boolean getBoolean(String section, String key, boolean defaultValue) {
            return booleans.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        public Duration getDuration(String section, String key, Duration defaultValue) {
            return durations.getOrDefault(fullKey(section, key), defaultValue);
        }
        
        private static String fullKey(String section, String key) {
            return section + "." + key;
        }
    }
    
    public static class Schema {
        enum ValueType { INTEGER, BOOLEAN, DURATION }
        
        record FieldSpec(String section, String key, ValueType type) {}
        
        private final Set<FieldSpec> fields = new HashSet<>();
        
        public Schema addInt(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.INTEGER));
            return this;
        }
        
        public Schema addBoolean(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.BOOLEAN));
            return this;
        }
        
        public Schema addDuration(String section, String key) {
            fields.add(new FieldSpec(section, key, ValueType.DURATION));
            return this;
        }
        
        Optional<ValueType> typeOf(String section, String key) {
            return fields.stream()
                .filter(f -> f.section.equals(section) && f.key.equals(key))
                .map(f -> f.type)
                .findFirst();
        }
    }
    
    public static Config parse(String input, Schema schema) {
        Map<String, Integer> ints = new HashMap<>();
        Map<String, Boolean> booleans = new HashMap<>();
        Map<String, Duration> durations = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        String[] lines = input.split("\n", -1);
        String currentSection = null;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;
            
            if (isIgnored(line)) {
                continue;
            }
            
            Optional<String> section = parseSection(line);
            if (section.isPresent()) {
                currentSection = section.get();
                continue;
            }
            
            Optional<KeyValue> kv = parseKeyValue(line);
            if (kv.isPresent()) {
                processKeyValue(kv.get(), currentSection, lineNum, schema, ints, booleans, durations, errors);
                continue;
            }
            
            errors.add("Line " + lineNum + ": malformed line");
        }
        
        return new Config(ints, booleans, durations, errors);
    }
    
    private static boolean isIgnored(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
    
    private static Optional<String> parseSection(String line) {
        Matcher m = SECTION_PATTERN.matcher(line);
        return m.matches() ? Optional.of(m.group(1)) : Optional.empty();
    }
    
    private static class KeyValue {
        final String key;
        final String valueStr;
        
        KeyValue(String key, String valueStr) {
            this.key = key;
            this.valueStr = valueStr;
        }
    }
    
    private static Optional<KeyValue> parseKeyValue(String line) {
        Matcher m = KEY_VALUE_PATTERN.matcher(line);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new KeyValue(m.group(1), m.group(2).trim()));
    }
    
    private static void processKeyValue(KeyValue kv, String currentSection, int lineNum,
                                       Schema schema, Map<String, Integer> ints,
                                       Map<String, Boolean> booleans,
                                       Map<String, Duration> durations, List<String> errors) {
        if (currentSection == null) {
            errors.add("Line " + lineNum + ": key-value pair outside section");
            return;
        }
        
        Optional<Schema.ValueType> typeOpt = schema.typeOf(currentSection, kv.key);
        if (typeOpt.isEmpty()) {
            errors.add("Line " + lineNum + ": unknown key '" + kv.key + "' in section [" + currentSection + "]");
            return;
        }
        
        Schema.ValueType type = typeOpt.get();
        Optional<Object> value = parseValue(kv.valueStr, type);
        if (value.isEmpty()) {
            errors.add("Line " + lineNum + ": invalid " + type.name().toLowerCase() + " value '" + kv.valueStr + "'");
            return;
        }
        
        storeValue(currentSection, kv.key, value.get(), type, ints, booleans, durations);
    }
    
    private static Optional<Object> parseValue(String valueStr, Schema.ValueType type) {
        return switch (type) {
            case INTEGER -> parseInteger(valueStr);
            case BOOLEAN -> parseBoolean(valueStr);
            case DURATION -> parseDuration(valueStr);
        };
    }
    
    private static Optional<Object> parseInteger(String valueStr) {
        if (!INTEGER_PATTERN.matcher(valueStr).matches()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(valueStr));
    }
    
    private static Optional<Object> parseBoolean(String valueStr) {
        return switch (valueStr.toLowerCase()) {
            case "true", "yes", "on" -> Optional.of(true);
            case "false", "no", "off" -> Optional.of(false);
            default -> Optional.empty();
        };
    }
    
    private static Optional<Object> parseDuration(String valueStr) {
        if (!DURATION_PATTERN.matcher(valueStr).matches()) {
            return Optional.empty();
        }
        long amount = Long.parseLong(valueStr.substring(0, valueStr.length() - 1));
        char unit = valueStr.charAt(valueStr.length() - 1);
        Duration d = switch (unit) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            default -> null;
        };
        return Optional.ofNullable(d);
    }
    
    private static void storeValue(String section, String key, Object value, Schema.ValueType type,
                                  Map<String, Integer> ints, Map<String, Boolean> booleans,
                                  Map<String, Duration> durations) {
        String fullKey = section + "." + key;
        switch (type) {
            case INTEGER -> ints.put(fullKey, (Integer) value);
            case BOOLEAN -> booleans.put(fullKey, (Boolean) value);
            case DURATION -> durations.put(fullKey, (Duration) value);
        }
    }
}
