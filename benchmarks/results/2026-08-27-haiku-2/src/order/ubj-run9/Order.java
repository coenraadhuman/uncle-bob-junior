import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int SCALE = 2;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items required"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        receipt.append(itemLines());
        receipt.append(String.format("Subtotal:      %s\n", formatCurrency(subtotal())));
        if (hasDiscount()) {
            receipt.append(String.format("Discount:      -%s\n", formatCurrency(discountAmount())));
            receipt.append(String.format("After Discount: %s\n", formatCurrency(subtotalAfterDiscount())));
        }
        receipt.append(String.format("VAT (21%%):      %s\n", formatCurrency(vatAmount())));
        receipt.append(String.format("Total:         %s\n", formatCurrency(total())));
        receipt.append("==============================");
        return receipt.toString();
    }

    private String itemLines() {
        StringBuilder items = new StringBuilder();
        for (LineItem item : this.items) {
            items.append(String.format("  %s x%d @ %s = %s\n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatCurrency(item.getUnitPrice()),
                    formatCurrency(item.lineTotal())));
        }
        return items.toString();
    }

    private BigDecimal subtotal() {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private boolean hasDiscount() {
        return subtotal().compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private BigDecimal discountAmount() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }
        return subtotal().multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal subtotalAfterDiscount() {
        return subtotal().subtract(discountAmount())
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal vatAmount() {
        return subtotalAfterDiscount().multiply(VAT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        return subtotalAfterDiscount().add(vatAmount())
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private String formatCurrency(BigDecimal amount) {
        return "€" + String.format("%.2f", amount);
    }
}
