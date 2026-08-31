record ConfigError(int line, String key, String message) {
  @Override
  public String toString() {
    return String.format("Line %d [%s]: %s", line, key, message);
  }
}
