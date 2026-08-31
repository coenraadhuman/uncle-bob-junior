import java.util.*;

class ParseException extends Exception {
    private final List<String> errors;
    
    public ParseException(List<String> errors) {
        super("Configuration parse errors:\n" + String.join("\n", errors));
        this.errors = new ArrayList<>(errors);
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
