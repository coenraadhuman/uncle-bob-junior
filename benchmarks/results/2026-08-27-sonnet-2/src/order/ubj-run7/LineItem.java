// filename: LineItem.java
import java.math.BigDecimal;
import java.math.RoundingMode;

record LineItem(String productName, int quantity, BigDecimal unitPrice) {

    LineItem {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must not be negative: " + unitPrice);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(PricingRules.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
