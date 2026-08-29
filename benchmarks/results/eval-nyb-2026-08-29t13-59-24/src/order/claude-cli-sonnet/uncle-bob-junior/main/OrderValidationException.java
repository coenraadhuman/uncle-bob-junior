// OrderValidationException.java
import java.util.List;

public final class OrderValidationException extends RuntimeException {

    private final List<String> errors;

    public OrderValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
