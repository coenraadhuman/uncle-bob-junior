// RawLineItem.java
import java.math.BigDecimal;

/** Unvalidated input for a single order line, as supplied by the caller. */
public record RawLineItem(String description, BigDecimal unitPrice, int quantity) {
}
