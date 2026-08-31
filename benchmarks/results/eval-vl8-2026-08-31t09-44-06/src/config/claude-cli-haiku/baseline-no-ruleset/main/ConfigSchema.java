import java.util.*;

class ConfigSchema {
    enum Type { INT, BOOLEAN, DURATION }
    
    static class FieldDef {
        final String name;
        final Type type;
        final Object defaultValue;
        
        FieldDef(String name, Type type, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }
    
    private final Map<String, FieldDef> fields = new LinkedHashMap<>();
    
    public void addField(String name, Type type, Object defaultValue) {
        fields.put(name, new FieldDef(name, type, defaultValue));
    }
    
    public FieldDef getField(String name) {
        return fields.get(name);
    }
    
    public Collection<FieldDef> getAllFields() {
        return fields.values();
    }
}
