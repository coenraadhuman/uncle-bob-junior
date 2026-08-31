public class Section {
    private final Map<String, ConfigValue> values;
    
    Section(Map<String, ConfigValue> values) {
        this.values = Map.copyOf(values);
    }
    
    public int getInt(String key) {
        var value = values.get(key);
        if (value instanceof IntValue iv) return iv.value();
        throw new ClassCastException("Key '" + key + "' is not an integer");
    }
    
    public boolean getBoolean(String key) {
        var value = values.get(key);
        if (value instanceof BoolValue bv) return bv.value();
        throw new ClassCastException("Key '" + key + "' is not a boolean");
    }
    
    public long getDurationSeconds(String key) {
        var value = values.get(key);
        if (value instanceof DurationValue dv) return dv.seconds();
        throw new ClassCastException("Key '" + key + "' is not a duration");
    }
}
