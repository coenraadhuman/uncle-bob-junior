import java.util.*;

public class Config {
  private final Map<String, Map<String, Object>> data;

  public Config(Map<String, Map<String, Object>> data) {
    this.data = new HashMap<>(data);
  }

  public int getInt(String section, String key) throws KeyNotFoundException {
    Object value = get(section, key);
    if (value instanceof Integer i) return i;
    throw new KeyNotFoundException("Not an integer: " + section + "." + key);
  }

  public boolean getBoolean(String section, String key) throws KeyNotFoundException {
    Object value = get(section, key);
    if (value instanceof Boolean b) return b;
    throw new KeyNotFoundException("Not a boolean: " + section + "." + key);
  }

  public Duration getDuration(String section, String key) throws KeyNotFoundException {
    Object value = get(section, key);
    if (value instanceof Duration d) return d;
    throw new KeyNotFoundException("Not a duration: " + section + "." + key);
  }

  private Object get(String section, String key) throws KeyNotFoundException {
    var sectionData = data.get(section);
    if (sectionData == null) throw new KeyNotFoundException("Section not found: " + section);
    if (!sectionData.containsKey(key)) throw new KeyNotFoundException("Key not found: " + key);
    return sectionData.get(key);
  }
}
