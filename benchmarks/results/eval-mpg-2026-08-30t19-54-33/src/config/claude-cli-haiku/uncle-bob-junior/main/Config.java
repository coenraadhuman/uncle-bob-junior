import java.util.*;

class Config {
  private final Map<String, Object> values;

  Config(Map<String, Object> values) {
    this.values = Map.copyOf(values);
  }

  public int getInt(String key) {
    return (Integer) values.get(key);
  }

  public boolean getBoolean(String key) {
    return (Boolean) values.get(key);
  }

  public Duration getDuration(String key) {
    return (Duration) values.get(key);
  }
}
