import java.util.*;

public class Configuration {
    private final Map<String, Map<String, Object>> data = new HashMap<>();
    
    void put(String section, String key, Object value) {
        data.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
    }
    
    public int getInt(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Integer) return (Integer) value;
        throw new ConfigException("Expected int for " + section + "." + key);
    }
    
    public boolean getBoolean(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Boolean) return (Boolean) value;
        throw new ConfigException("Expected boolean for " + section + "." + key);
    }
    
    public Duration getDuration(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof Duration) return (Duration) value;
        throw new ConfigException("Expected duration for " + section + "." + key);
    }
    
    public String getString(String section, String key) throws ConfigException {
        Object value = get(section, key);
        if (value instanceof String) return (String) value;
        throw new ConfigException("Expected string for " + section + "." + key);
    }
    
    private Object get(String section, String key) throws ConfigException {
        Map<String, Object> sectionData = data.get(section);
        if (sectionData == null) throw new ConfigException("Unknown section: " + section);
        Object value = sectionData.get(key);
        if (value == null) throw new ConfigException("Missing key: " + section + "." + key);
        return value;
    }
}
