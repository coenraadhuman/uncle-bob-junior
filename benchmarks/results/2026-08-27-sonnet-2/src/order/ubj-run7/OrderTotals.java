// filename: OrderTotals.java
import java.math.BigDecimal;

record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
