// OrderTotals.java
import java.math.BigDecimal;

/** Result of totalling an order: subtotal, discount, VAT and the final total. */
public record OrderTotals(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal vatAmount, BigDecimal total) {
}
