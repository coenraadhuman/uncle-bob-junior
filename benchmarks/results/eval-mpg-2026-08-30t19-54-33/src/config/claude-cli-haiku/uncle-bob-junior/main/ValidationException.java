import java.util.*;

public class ValidationException extends Exception {
    private final List<ValidationError> errors;
    
    public ValidationException(List<ValidationError> errors) {
        super(formatErrors(errors));
        this.errors = errors;
    }
    
    public ValidationException(String message) {
        super(message);
        this.errors = List.of();
    }
    
    private static String formatErrors(List<ValidationError> errors) {
        StringBuilder sb = new StringBuilder();
        for (ValidationError err : errors) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Line ").append(err.lineNumber()).append(": ").append(err.message());
        }
        return sb.toString();
    }
    
    public List<ValidationError> errors() {
        return errors;
    }
}
