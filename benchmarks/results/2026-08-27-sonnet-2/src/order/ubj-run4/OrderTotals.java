// File: OrderTotals.java
import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
