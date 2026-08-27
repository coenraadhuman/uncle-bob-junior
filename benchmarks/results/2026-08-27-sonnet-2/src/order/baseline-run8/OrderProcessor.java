import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static final class LineItem {
        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;

        public LineItem(String description, int quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class OrderResult {
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal vat;
        private final BigDecimal total;
        private final String receipt;

        public OrderResult(BigDecimal subtotal, BigDecimal discount, BigDecimal vat,
                            BigDecimal total, String receipt) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.total = total;
            this.receipt = receipt;
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

        public String getReceipt() {
            return receipt;
        }
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new IllegalArgumentException("Line item must not be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.getDescription() + "' must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format("%-20s %3d x %8.2f = %10.2f%n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.lineTotal()));
        }
        sb.append("-------\n");
        sb.append(String.format("%-20s %21.2f%n", "Subtotal", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-20s %21.2f%n", "Discount (10%)", discount.negate()));
        }
        sb.append(String.format("%-20s %21.2f%n", "VAT (21%)", vat));
        sb.append(String.format("%-20s %21.2f%n", "Total", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Handling fee", 1, new BigDecimal("5.00"))
        );

        OrderResult result = processor.processOrder(items);
        System.out.println(result.getReceipt());
    }
}
