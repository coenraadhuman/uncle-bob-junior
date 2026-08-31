public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}

// ---- OrderCalculator.java ----
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
