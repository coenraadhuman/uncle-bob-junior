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
        this.unitPrice = unitPrice;
    }

    void validate() {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int SCALE = 2;

    private final List<LineItem> lineItems = new ArrayList<>();

    void addLineItem(LineItem item) {
        item.validate();
        lineItems.add(item);
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateTaxableAmount(BigDecimal subtotal, BigDecimal discount) {
        return subtotal.subtract(discount).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVat(BigDecimal taxableAmount) {
        return taxableAmount.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotal(BigDecimal taxableAmount, BigDecimal vat) {
        return taxableAmount.add(vat).setScale(SCALE, RoundingMode.HALF_UP);
    }

    String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal taxableAmount = calculateTaxableAmount(subtotal, discount);
        BigDecimal vat = calculateVat(taxableAmount);
        BigDecimal total = calculateTotal(taxableAmount, vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal()));
        }

        receipt.append(String.format("Subtotal:       €%.2f\n", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%): -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal:       €%.2f\n", taxableAmount));
        receipt.append(String.format("VAT (21%%):      €%.2f\n", vat));
        receipt.append(String.format("Total:          €%.2f\n", total));

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Widget A", 3, new BigDecimal("25.00")));
        order.addLineItem(new LineItem("Widget B", 2, new BigDecimal("20.00")));
        order.addLineItem(new LineItem("Widget C", 1, new BigDecimal("15.00")));

        System.out.println(order.generateReceipt());
    }
}
