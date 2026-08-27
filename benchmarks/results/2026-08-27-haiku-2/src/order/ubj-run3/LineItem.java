import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
    }

    String description() {
        return description;
    }

    int quantity() {
        return quantity;
    }

    BigDecimal unitPrice() {
        return unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
        validateItems();
    }

    private void validateItems() {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            validateLineItem(item);
        }
    }

    private void validateLineItem(LineItem item) {
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + item.description());
        }
        if (item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive: " + item.description());
        }
        if (item.description().isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
    }

    BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal discountAmount() {
        BigDecimal subtotal = subtotal();
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    BigDecimal discountedSubtotal() {
        return subtotal().subtract(discountAmount());
    }

    BigDecimal vat() {
        return discountedSubtotal().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return discountedSubtotal().add(vat()).setScale(2, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description(),
                item.quantity(),
                item.unitPrice(),
                item.lineTotal()));
        }

        sb.append("\n");
        sb.append(String.format("Subtotal: €%.2f\n", subtotal()));
        
        if (discountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
        }
        
        sb.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal()));
        sb.append(String.format("VAT (21%%): €%.2f\n", vat()));
        sb.append(String.format("Total: €%.2f\n", total()));
        
        return sb.toString();
    }
}

class OrderTest {
    static void testValidation() {
        try {
            new Order(new ArrayList<>());
            throw new AssertionError("Should reject empty order");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("at least one item");
        }

        try {
            List<LineItem> items = new ArrayList<>();
            items.add(new LineItem("Widget", 0, new BigDecimal("10.00")));
            new Order(items);
            throw new AssertionError("Should reject zero quantity");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity must be positive");
        }

        try {
            List<LineItem> items = new ArrayList<>();
            items.add(new LineItem("Widget", 1, new BigDecimal("-10.00")));
            new Order(items);
            throw new AssertionError("Should reject negative price");
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Unit price must be positive");
        }
    }

    static void testCalculationsNoDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("3.50")));
        items.add(new LineItem("Cake", 1, new BigDecimal("5.00")));
        
        Order order = new Order(items);
        
        assert order.subtotal().equals(new BigDecimal("12.00"));
        assert order.discountAmount().equals(BigDecimal.ZERO);
        assert order.vat().compareTo(new BigDecimal("2.52")) == 0;
        assert order.total().compareTo(new BigDecimal("14.52")) == 0;
    }

    static void testCalculationsWithDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Expensive Widget", 1, new BigDecimal("150.00")));
        
        Order order = new Order(items);
        
        assert order.subtotal().compareTo(new BigDecimal("150.00")) == 0;
        assert order.discountAmount().compareTo(new BigDecimal("15.00")) == 0;
        assert order.discountedSubtotal().compareTo(new BigDecimal("135.00")) == 0;
        assert order.vat().compareTo(new BigDecimal("28.35")) == 0;
        assert order.total().compareTo(new BigDecimal("163.35")) == 0;
    }

    static void runAll() {
        testValidation();
        testCalculationsNoDiscount();
        testCalculationsWithDiscount();
        System.out.println("All tests passed!");
    }
}

class Main {
    public static void main(String[] args) {
        OrderTest.runAll();
        
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("3.50")));
        items.add(new LineItem("Croissant", 3, new BigDecimal("2.75")));
        items.add(new LineItem("Sandwich", 1, new BigDecimal("8.50")));
        
        Order order = new Order(items);
        System.out.println(order.receipt());
    }
}
