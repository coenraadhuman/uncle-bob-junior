import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Receipt(
        List<LineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {
}
