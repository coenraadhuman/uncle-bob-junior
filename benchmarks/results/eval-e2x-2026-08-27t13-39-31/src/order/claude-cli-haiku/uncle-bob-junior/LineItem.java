import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String description() { return description; }
    int quantity() { return quantity; }
    BigDecimal unitPrice() { return unitPrice; }
    
    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int SCALE = 2;

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        validate(items);
        this.items = items;
    }

    private void validate(List<LineItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        }
    }

    private BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal discountAmount() {
        BigDecimal sub = subtotal();
        if (sub.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return sub.multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal taxableAmount() {
        return subtotal().subtract(discountAmount())
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal vatAmount() {
        return taxableAmount().multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return taxableAmount().add(vatAmount())
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("-------\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f%n",
                item.description(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal()));
        }
        
        sb.append("-------\n");
        sb.append(String.format("Subtotal: €%.2f%n", subtotal()));
        
        BigDecimal discount = discountAmount();
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f%n", discount));
        }
        
        sb.append(String.format("VAT (21%%): €%.2f%n", vatAmount()));
        sb.append(String.format("Total: €%.2f%n", total()));
        
        return sb.toString();
    }
}
