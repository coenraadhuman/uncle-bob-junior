record Duration(long milliseconds) {
  static Duration of(long value, String unit) {
    long ms = switch (unit.toLowerCase()) {
      case "ms" -> value;
      case "s" -> value * 1000;
      case "m" -> value * 60 * 1000;
      case "h" -> value * 60 * 60 * 1000;
      case "d" -> value * 24 * 60 * 60 * 1000;
      default -> throw new IllegalArgumentException("Unknown unit: " + unit);
    };
    return new Duration(ms);
  }
}
