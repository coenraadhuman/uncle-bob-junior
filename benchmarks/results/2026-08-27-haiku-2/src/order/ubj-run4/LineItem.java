import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

final class LineItem {
    final String description;
    final int quantity;
    final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

final class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        this.items = new ArrayList<>(items);
    }

    BigDecimal preVatTotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    boolean isDiscountEligible() {
        return preVatTotal().compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    BigDecimal applyDiscount(BigDecimal amount) {
        if (!isDiscountEligible()) return amount;
        return amount.multiply(BigDecimal.ONE.subtract(DISCOUNT_RATE));
    }

    BigDecimal discountedSubtotal() {
        return applyDiscount(preVatTotal());
    }

    BigDecimal vat() {
        return discountedSubtotal().multiply(VAT_RATE)
            .setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return discountedSubtotal().add(vat())
            .setScale(2, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description, item.quantity, item.unitPrice, item.subtotal()));
        }
        
        sb.append("\nSubtotal: €").append(String.format("%.2f", preVatTotal())).append("\n");
        
        if (isDiscountEligible()) {
            BigDecimal discountAmount = preVatTotal()
                .multiply(DISCOUNT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            sb.append("Discount (10%): -€").append(String.format("%.2f", discountAmount)).append("\n");
        }
        
        sb.append("Subtotal after discount: €").append(String.format("%.2f", discountedSubtotal())).append("\n");
        sb.append("VAT (21%): €").append(String.format("%.2f", vat())).append("\n");
        sb.append("TOTAL: €").append(String.format("%.2f", total())).append("\n");
        
        return sb.toString();
    }
}

final class OrderProcessor {
    static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Widget", 10, new BigDecimal("5.50")),
            new LineItem("Gadget", 3, new BigDecimal("20.00")),
            new LineItem("Doohickey", 5, new BigDecimal("8.75"))
        );

        Order order = new Order(items);
        System.out.println(order.receipt());
    }
}
