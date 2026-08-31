import java.util.*;

class ParsingException extends Exception {
  private final List<ValidationError> errors;
  
  ParsingException(List<ValidationError> errors) {
    super(formatMessage(errors));
    this.errors = errors;
  }
  
  private static String formatMessage(List<ValidationError> errors) {
    return errors.stream()
      .map(e -> "Line " + e.lineNumber + ": " + e.message)
      .collect(java.util.stream.Collectors.joining("\n"));
  }
  
  List<ValidationError> errors() {
    return errors;
  }
}
