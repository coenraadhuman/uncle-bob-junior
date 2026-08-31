import java.util.*;

class SectionSpec {
    private final String name;
    private final Map<String, KeySpec> keys = new LinkedHashMap<>();
    
    SectionSpec(String name) {
        this.name = name;
    }
    
    void addKey(KeySpec spec) {
        keys.put(spec.name, spec);
    }
    
    String name() { return name; }
    KeySpec key(String name) { return keys.get(name); }
    boolean hasKey(String name) { return keys.containsKey(name); }
    Map<String, KeySpec> keys() { return keys; }
}
