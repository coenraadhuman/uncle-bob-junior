import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class OrderProcessor {
    Receipt processOrder(List<LineItem> items) {
        validateItems(items);
        BigDecimal subtotal = computeSubtotal(items);
        return new Receipt(subtotal);
    }
    
    private void validateItems(List<LineItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (!item.isValid()) {
                throw new IllegalArgumentException("Invalid item: " + item.getDescription());
            }
        }
    }
    
    private BigDecimal computeSubtotal(List<LineItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.getLineTotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
