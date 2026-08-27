import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private final String name;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String name, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    String format() {
        return String.format("%s x%d @ €%.2f = €%.2f", 
            name, quantity, unitPrice, lineTotal());
    }
}

public class OrderProcessor {
    private static final int VAT_RATE = 21;
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("10");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static String processOrder(List<LineItem> items) {
        validateItems(items);
        
        BigDecimal subtotal = computeSubtotal(items);
        BigDecimal discountAmount = computeDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal vat = computeVat(afterDiscount);
        BigDecimal total = afterDiscount.add(vat);
        
        return buildReceipt(items, subtotal, discountAmount, afterDiscount, vat, total);
    }

    private static void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    private static BigDecimal computeSubtotal(List<LineItem> items) {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, ROUNDING);
    }

    private static BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .divide(new BigDecimal("100"), 2, ROUNDING);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal computeVat(BigDecimal amount) {
        return amount.multiply(new BigDecimal(VAT_RATE))
            .divide(new BigDecimal("100"), 2, ROUNDING);
    }

    private static String buildReceipt(List<LineItem> items, BigDecimal subtotal,
            BigDecimal discount, BigDecimal afterDiscount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== ORDER RECEIPT =====\n");
        
        for (LineItem item : items) {
            receipt.append(item.format()).append("\n");
        }
        
        receipt.append("\nSubtotal:    €").append(subtotal).append("\n");
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append("Discount:   -€").append(discount).append("\n");
            receipt.append("After disc:   €").append(afterDiscount).append("\n");
        }
        
        receipt.append("VAT (21%):    €").append(vat).append("\n");
        receipt.append("TOTAL:        €").append(total).append("\n");
        
        return receipt.toString();
    }
}
