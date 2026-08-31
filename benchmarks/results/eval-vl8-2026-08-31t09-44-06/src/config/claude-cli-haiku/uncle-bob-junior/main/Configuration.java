import java.util.*;

class Configuration {
    private final Map<String, ConfigValue> values;
    private final List<ValidationError> errors;
    
    Configuration(Map<String, ConfigValue> values, List<ValidationError> errors) {
        this.values = new HashMap<>(values);
        this.errors = new ArrayList<>(errors);
    }
    
    public int getInt(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof IntValue iv) {
            return iv.value();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    public boolean getBoolean(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof BoolValue bv) {
            return bv.value();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    public long getDurationSeconds(String section, String name) {
        ConfigValue val = values.get(fullKey(section, name));
        if (val instanceof DurationValue dv) {
            return dv.seconds();
        }
        throw new IllegalArgumentException("Key not found or wrong type: " + fullKey(section, name));
    }
    
    private String fullKey(String section, String name) {
        return section.isEmpty() ? name : section + "." + name;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
}
