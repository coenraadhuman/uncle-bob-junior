public class Duration {
  private final int seconds;

  private Duration(int seconds) {
    this.seconds = seconds;
  }

  public static Duration ofSeconds(int seconds) {
    if (seconds < 0) throw new IllegalArgumentException("Duration cannot be negative");
    return new Duration(seconds);
  }

  public static Duration parse(String text) throws ParseException {
    text = text.trim();
    if (text.endsWith("s")) {
      int value = Integer.parseInt(text.substring(0, text.length() - 1));
      return ofSeconds(value);
    }
    if (text.endsWith("m")) {
      int value = Integer.parseInt(text.substring(0, text.length() - 1));
      return ofSeconds(value * 60);
    }
    throw new ParseException("Invalid duration format: " + text);
  }

  public int seconds() {
    return seconds;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Duration d && d.seconds == seconds;
  }

  @Override
  public int hashCode() {
    return seconds;
  }

  @Override
  public String toString() {
    return seconds + "s";
  }
}
