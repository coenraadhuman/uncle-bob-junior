class ConfigKey {
    final String type;
    final Object defaultValue;
    
    public ConfigKey(String type, Object defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    public static ConfigKey of(String type) {
        return new ConfigKey(type, null);
    }
    
    public static ConfigKey of(String type, Object defaultValue) {
        return new ConfigKey(type, defaultValue);
    }
}
