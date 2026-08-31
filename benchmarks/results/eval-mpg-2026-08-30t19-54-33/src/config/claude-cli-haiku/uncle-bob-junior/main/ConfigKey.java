record ConfigKey(String name, Class<?> type, Object defaultValue) {
  ConfigKey {
    if (!isValidType(type)) throw new IllegalArgumentException("Unsupported type: " + type);
  }

  static boolean isValidType(Class<?> t) {
    return t == Integer.class || t == Boolean.class || t == Duration.class;
  }
}
