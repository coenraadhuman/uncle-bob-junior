public record ConfigField(String type, Object defaultValue) {
  public static ConfigField integer(int defaultValue) {
    return new ConfigField("int", defaultValue);
  }

  public static ConfigField bool(boolean defaultValue) {
    return new ConfigField("bool", defaultValue);
  }

  public static ConfigField duration(Duration defaultValue) {
    return new ConfigField("duration", defaultValue);
  }
}
