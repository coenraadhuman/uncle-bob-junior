import java.util.*;

class Configuration {
  private final Map<String, Map<String, ConfigValue>> sections;
  private final Map<String, ConfigValue> defaults;
  
  Configuration(Map<String, Map<String, ConfigValue>> sections, Map<String, ConfigValue> defaults) {
    this.sections = sections;
    this.defaults = defaults;
  }
  
  private ConfigValue resolve(String section, String key) {
    ConfigValue value = sections.getOrDefault(section, Map.of()).get(key);
    return value != null ? value : defaults.get(key);
  }
  
  int getInt(String section, String key) {
    ConfigValue v = resolve(section, key);
    if (v instanceof ConfigValue.IntValue iv) return iv.value();
    throw new IllegalStateException("Key " + key + " is not an integer");
  }
  
  boolean getBoolean(String section, String key) {
    ConfigValue v = resolve(section, key);
    if (v instanceof ConfigValue.BoolValue bv) return bv.value();
    throw new IllegalStateException("Key " + key + " is not a boolean");
  }
  
  long getDurationMs(String section, String key) {
    ConfigValue v = resolve(section, key);
    if (v instanceof ConfigValue.DurationValue dv) return dv.milliseconds();
    throw new IllegalStateException("Key " + key + " is not a duration");
  }
}
