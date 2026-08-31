import java.util.*;

public class ConfigSchema {
    private final Map<String, Map<String, KeyDef>> schema = new LinkedHashMap<>();
    
    private static class KeyDef {
        final ValueType type;
        final ConfigValue defaultValue;
        
        KeyDef(ValueType type, ConfigValue defaultValue) {
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }
    
    public ConfigSchema addSection(String name) {
        schema.put(name, new LinkedHashMap<>());
        return this;
    }
    
    public ConfigSchema addKey(String section, String key, ValueType type) {
        return addKey(section, key, type, null);
    }
    
    public ConfigSchema addKey(String section, String key, ValueType type, ConfigValue defaultValue) {
        schema.computeIfAbsent(section, s -> new LinkedHashMap<>())
              .put(key, new KeyDef(type, defaultValue));
        return this;
    }
    
    public boolean hasSection(String name) {
        return schema.containsKey(name);
    }
    
    public boolean hasKey(String section, String key) {
        return schema.getOrDefault(section, Map.of()).containsKey(key);
    }
    
    public ValueType getType(String section, String key) {
        return schema.getOrDefault(section, Map.of()).get(key).type;
    }
    
    public ConfigValue getDefault(String section, String key) {
        KeyDef def = schema.getOrDefault(section, Map.of()).get(key);
        return def != null ? def.defaultValue : null;
    }
    
    public Set<String> sections() {
        return schema.keySet();
    }
    
    public Set<String> keys(String section) {
        return schema.getOrDefault(section, Map.of()).keySet();
    }
}
