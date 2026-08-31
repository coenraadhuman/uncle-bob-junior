public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        requireNonBlankDescription(description);
        requireNonNegativePrice(unitPrice);
        requirePositiveQuantity(quantity);
    }

    private static void requireNonBlankDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Line item description must not be blank");
        }
    }

    private static void requireNonNegativePrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLineItemException("Line item unit price must not be negative");
        }
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidLineItemException("Line item quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

// ---- OrderTotals.java ----
import java.math.BigDecimal;
