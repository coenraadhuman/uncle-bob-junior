import java.util.*;

class Config {
    private final Map<String, Object> values;
    
    public Config(Map<String, Object> values) {
        this.values = values;
    }
    
    public int getInt(String key) {
        return (Integer) values.get(key);
    }
    
    public boolean getBoolean(String key) {
        return (Boolean) values.get(key);
    }
    
    public long getDuration(String key) {
        return (Long) values.get(key);
    }
    
    public Object get(String key) {
        return values.get(key);
    }
    
    public boolean has(String key) {
        return values.containsKey(key);
    }
}
