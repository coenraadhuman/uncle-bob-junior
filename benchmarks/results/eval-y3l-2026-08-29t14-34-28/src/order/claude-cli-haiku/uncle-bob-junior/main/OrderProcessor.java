import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static class LineItem {
        private final String description;
        private final BigDecimal price;
        private final int quantity;

        public LineItem(String description, BigDecimal price, int quantity) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            if (price == null || price.signum() < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            this.description = description;
            this.price = price;
            this.quantity = quantity;
        }

        public BigDecimal lineTotal() {
            return price.multiply(new BigDecimal(quantity));
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class OrderResult {
        private final List<LineItem> items;
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal vat;
        private final BigDecimal total;

        private OrderResult(List<LineItem> items, BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
            this.items = items;
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.total = total;
        }

        public String getReceipt() {
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== RECEIPT ===\n");
            for (LineItem item : items) {
                receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getDescription(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.lineTotal()));
            }
            receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
            if (discount.signum() > 0) {
                receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
            }
            receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
            receipt.append(String.format("TOTAL: €%.2f\n", total));
            return receipt.toString();
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public BigDecimal getVat() {
            return vat;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    public static OrderResult process(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        BigDecimal subtotal = items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderResult(items, subtotal, discountAmount, vat, total);
    }
}
