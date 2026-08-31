import java.util.*;
import java.util.regex.*;

class Configuration {
    private final Map<String, Map<String, Object>> data = new HashMap<>();
    
    public void addSection(String section) {
        data.putIfAbsent(section, new HashMap<>());
    }
    
    public void put(String section, String key, Object value) {
        data.get(section).put(key, value);
    }
    
    public Object get(String section, String key) {
        Map<String, Object> sectionData = data.get(section);
        return sectionData != null ? sectionData.get(key) : null;
    }
    
    public int getInt(String section, String key) {
        return (Integer) get(section, key);
    }
    
    public boolean getBoolean(String section, String key) {
        return (Boolean) get(section, key);
    }
    
    public ConfigDuration getDuration(String section, String key) {
        return (ConfigDuration) get(section, key);
    }
    
    public String getString(String section, String key) {
        return (String) get(section, key);
    }
}
