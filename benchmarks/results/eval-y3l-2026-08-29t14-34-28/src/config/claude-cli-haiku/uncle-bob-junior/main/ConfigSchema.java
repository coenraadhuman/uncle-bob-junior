import java.util.*;

class ConfigSchema {
    record KeyDef(Class<?> type, Object defaultValue) {}
    
    private final Map<String, Map<String, KeyDef>> sections = new HashMap<>();
    
    public void addSection(String section) {
        sections.putIfAbsent(section, new HashMap<>());
    }
    
    public void addKey(String section, String key, Class<?> type, Object defaultValue) {
        sections.computeIfAbsent(section, k -> new HashMap<>())
                .put(key, new KeyDef(type, defaultValue));
    }
    
    public boolean hasSection(String section) {
        return sections.containsKey(section);
    }
    
    public KeyDef getKey(String section, String key) {
        Map<String, KeyDef> sectionKeys = sections.get(section);
        return sectionKeys != null ? sectionKeys.get(key) : null;
    }
    
    public Map<String, Map<String, KeyDef>> getAllSections() {
        return sections;
    }
}
