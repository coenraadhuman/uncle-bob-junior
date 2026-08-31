import java.util.*;
import java.util.regex.*;

public class Configuration {
    private final Map<String, Object> values;
    
    public Configuration(Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }
    
    public int getInt(String key) {
        Object val = values.get(key);
        if (val == null) throw new NoSuchElementException("key not found: " + key);
        return (Integer) val;
    }
    
    public boolean getBoolean(String key) {
        Object val = values.get(key);
        if (val == null) throw new NoSuchElementException("key not found: " + key);
        return (Boolean) val;
    }
    
    public long getDuration(String key) {
        Object val = values.get(key);
        if (val == null) throw new NoSuchElementException("key not found: " + key);
        return (Long) val;
    }
}
