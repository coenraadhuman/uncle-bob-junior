import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static class LineItem {
        private final String description;
        private final BigDecimal unitPrice;
        private final int quantity;

        public LineItem(String description, BigDecimal unitPrice, int quantity) {
            validateDescription(description);
            validatePrice(unitPrice);
            validateQuantity(quantity);
            this.description = description;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public BigDecimal getLineTotal() {
            return unitPrice.multiply(new BigDecimal(quantity));
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        private void validateDescription(String description) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be null or blank");
            }
        }

        private void validatePrice(BigDecimal unitPrice) {
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative");
            }
        }

        private void validateQuantity(int quantity) {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }
    }

    public static class Order {
        private final List<LineItem> items;

        public Order(List<LineItem> items) {
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one item");
            }
            this.items = new ArrayList<>(items);
        }

        public BigDecimal calculateTotal() {
            return summarize().total;
        }

        public String generateReceipt() {
            OrderCalculation calc = summarize();
            return formatReceipt(calc);
        }

        private OrderCalculation summarize() {
            BigDecimal subtotal = calculateSubtotal();
            BigDecimal discount = qualifiesForDiscount(subtotal)
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            BigDecimal discountedSubtotal = subtotal.subtract(discount);
            BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);
            return new OrderCalculation(subtotal, discount, discountedSubtotal, vat, total);
        }

        private BigDecimal calculateSubtotal() {
            return items.stream()
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private boolean qualifiesForDiscount(BigDecimal subtotal) {
            return subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        }

        private String formatReceipt(OrderCalculation calc) {
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== ORDER RECEIPT ===\n");
            for (LineItem item : items) {
                receipt.append(String.format("%-30s %3d x %s = %s\n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatCurrency(item.getUnitPrice()),
                    formatCurrency(item.getLineTotal())));
            }
            receipt.append("---------------------\n");
            receipt.append(String.format("Subtotal:                  %s\n", formatCurrency(calc.subtotal)));
            if (calc.discount.compareTo(BigDecimal.ZERO) > 0) {
                receipt.append(String.format("Discount (10%%):           -%s\n", formatCurrency(calc.discount)));
            }
            receipt.append(String.format("Pre-VAT Total:             %s\n", formatCurrency(calc.discountedSubtotal)));
            receipt.append(String.format("VAT (21%%):                 %s\n", formatCurrency(calc.vat)));
            receipt.append("======================\n");
            receipt.append(String.format("TOTAL:                     %s\n", formatCurrency(calc.total)));
            return receipt.toString();
        }

        private String formatCurrency(BigDecimal amount) {
            return "€ " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
    }

    private static class OrderCalculation {
        final BigDecimal subtotal;
        final BigDecimal discount;
        final BigDecimal discountedSubtotal;
        final BigDecimal vat;
        final BigDecimal total;

        OrderCalculation(BigDecimal subtotal, BigDecimal discount, BigDecimal discountedSubtotal,
                        BigDecimal vat, BigDecimal total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.discountedSubtotal = discountedSubtotal;
            this.vat = vat;
            this.total = total;
        }
    }
}
