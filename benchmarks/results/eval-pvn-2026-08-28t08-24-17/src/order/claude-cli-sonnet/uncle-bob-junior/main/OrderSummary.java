// OrderSummary.java
import java.math.BigDecimal;

public record OrderSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal vatAmount, BigDecimal total) {
}
