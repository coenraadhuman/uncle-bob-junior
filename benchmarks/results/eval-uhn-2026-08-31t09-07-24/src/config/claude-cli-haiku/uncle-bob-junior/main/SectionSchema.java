public class SectionSchema {
    private final Map<String, KeyDef> keys;
    
    public SectionSchema(Map<String, KeyDef> keys) {
        this.keys = Map.copyOf(keys);
    }
    
    public KeyDef getKey(String name) { return keys.get(name); }
    public Set<String> keyNames() { return keys.keySet(); }
}
