import java.util.*;

public class ParseResult {
    private final Configuration configuration;
    private final List<String> errors;
    
    ParseResult(Configuration configuration, List<String> errors) {
        this.configuration = configuration;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }
    
    public Configuration configuration() {
        return configuration;
    }
    
    public List<String> errors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
