import java.util.*;

public class ParseResult<T> {
    public final T config;
    public final List<ValidationError> errors;
    
    public ParseResult(T config, List<ValidationError> errors) {
        this.config = config;
        this.errors = errors;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
