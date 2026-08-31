// LineItem.java
import java.math.BigDecimal;

/**
 * A line item whose invariants (non-blank description, positive price and
 * quantity) have been checked. Only {@link LineItemValidator} constructs these.
 */
public record LineItem(String description, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
