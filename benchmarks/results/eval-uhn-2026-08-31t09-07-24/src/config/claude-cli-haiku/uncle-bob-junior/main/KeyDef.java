public class KeyDef {
    private final String type;
    private final ConfigValue defaultValue;
    
    public KeyDef(String type, ConfigValue defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    public String type() { return type; }
    public ConfigValue defaultValue() { return defaultValue; }
}
