import java.util.*;

public class ConfigValidationException extends Exception {
    private final List<String> errors;
    
    public ConfigValidationException(List<String> errors) {
        super(formatErrors(errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }
    
    private static String formatErrors(List<String> errors) {
        return "Configuration validation failed with " + errors.size() + " error(s):\n" + 
               String.join("\n", errors);
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
