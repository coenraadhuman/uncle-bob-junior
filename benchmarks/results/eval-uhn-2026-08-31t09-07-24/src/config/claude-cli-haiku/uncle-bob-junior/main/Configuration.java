import java.util.*;

class Configuration {
    private final Map<String, Map<String, ConfigValue>> data;
    
    Configuration(Map<String, Map<String, ConfigValue>> data) {
        this.data = new HashMap<>(data);
    }
    
    int getInt(String section, String key) {
        IntValue v = (IntValue) value(section, key);
        return v.value();
    }
    
    boolean getBoolean(String section, String key) {
        BooleanValue v = (BooleanValue) value(section, key);
        return v.value();
    }
    
    long getDurationMillis(String section, String key) {
        DurationValue v = (DurationValue) value(section, key);
        return v.millis();
    }
    
    private ConfigValue value(String section, String key) {
        Map<String, ConfigValue> sec = data.get(section);
        if (sec == null) throw new NoSuchElementException("section: " + section);
        ConfigValue val = sec.get(key);
        if (val == null) throw new NoSuchElementException("key: " + key);
        return val;
    }
}
