import java.util.*;

public class ConfigSchema {
    public enum ValueType { INT, BOOLEAN, DURATION, STRING }
    
    static class KeySpec {
        final ValueType type;
        final Object defaultValue;
        
        KeySpec(ValueType type, Object defaultValue) {
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }
    
    private final Map<String, Map<String, KeySpec>> specs = new HashMap<>();
    
    public ConfigSchema addSection(String section) {
        specs.putIfAbsent(section, new HashMap<>());
        return this;
    }
    
    public ConfigSchema addInt(String section, String key, int defaultValue) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.INT, defaultValue));
        return this;
    }
    
    public ConfigSchema addIntRequired(String section, String key) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.INT, null));
        return this;
    }
    
    public ConfigSchema addBoolean(String section, String key, boolean defaultValue) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.BOOLEAN, defaultValue));
        return this;
    }
    
    public ConfigSchema addBooleanRequired(String section, String key) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.BOOLEAN, null));
        return this;
    }
    
    public ConfigSchema addDuration(String section, String key, Duration defaultValue) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.DURATION, defaultValue));
        return this;
    }
    
    public ConfigSchema addDurationRequired(String section, String key) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.DURATION, null));
        return this;
    }
    
    public ConfigSchema addString(String section, String key, String defaultValue) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.STRING, defaultValue));
        return this;
    }
    
    public ConfigSchema addStringRequired(String section, String key) {
        ensureSection(section);
        specs.get(section).put(key, new KeySpec(ValueType.STRING, null));
        return this;
    }
    
    private void ensureSection(String section) {
        specs.putIfAbsent(section, new HashMap<>());
    }
    
    boolean isSectionDefined(String section) {
        return specs.containsKey(section);
    }
    
    boolean isKeyDefined(String section, String key) {
        Map<String, KeySpec> sectionSpecs = specs.get(section);
        return sectionSpecs != null && sectionSpecs.containsKey(key);
    }
    
    KeySpec getKeySpec(String section, String key) {
        Map<String, KeySpec> sectionSpecs = specs.get(section);
        if (sectionSpecs == null) return null;
        return sectionSpecs.get(key);
    }
    
    Map<String, Map<String, KeySpec>> getAllSpecs() {
        return specs;
    }
}
