import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** A single order line: a product name, a quantity, and a unit price excluding VAT. */
public record LineItem(String productName, int quantity, BigDecimal unitPriceExVat) {}
