import java.util.*;

public class ConfigValidationException extends Exception {
    private final List<String> errors;
    
    public ConfigValidationException(List<String> errors) {
        super("Validation failed:\n" + String.join("\n", errors));
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
