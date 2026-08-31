import java.util.*;

public class ConfigSection {
    private final String name;
    private final Map<String, ConfigValue> entries = new LinkedHashMap<>();
    
    public ConfigSection(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }
    
    public void put(String key, ConfigValue value) {
        entries.put(key, value);
    }
    
    public ConfigValue get(String key) {
        return entries.get(key);
    }
    
    public boolean has(String key) {
        return entries.containsKey(key);
    }
}
