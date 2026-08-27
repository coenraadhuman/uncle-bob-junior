// ---- InvalidLineItemException.java ----
public class InvalidLineItemException extends RuntimeException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}

// ---- LineItem.java ----
import java.math.BigDecimal;
