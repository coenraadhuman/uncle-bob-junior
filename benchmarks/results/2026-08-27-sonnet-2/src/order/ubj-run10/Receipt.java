// Receipt.java
import java.math.BigDecimal;

public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total, String text) {
}
