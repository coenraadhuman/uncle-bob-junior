public class ParseException extends Exception {
  private final int lineNumber;

  public ParseException(String message) {
    this(message, -1);
  }

  public ParseException(String message, int lineNumber) {
    super(message + (lineNumber > 0 ? " at line " + lineNumber : ""));
    this.lineNumber = lineNumber;
  }

  public int lineNumber() {
    return lineNumber;
  }
}
