import java.math.BigDecimal;

/** Immutable breakdown of an order's money amounts, all rounded to cents. */
public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
