import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

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

    public static final class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }

    public static final class OrderResult {
        public final BigDecimal subtotalExVat;
        public final BigDecimal discount;
        public final BigDecimal vatAmount;
        public final BigDecimal total;
        public final String receipt;

        public OrderResult(BigDecimal subtotalExVat, BigDecimal discount, BigDecimal vatAmount,
                            BigDecimal total, String receipt) {
            this.subtotalExVat = subtotalExVat;
            this.discount = discount;
            this.vatAmount = vatAmount;
            this.total = total;
            this.receipt = receipt;
        }
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        validate(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vatAmount).setScale(SCALE, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, discountedSubtotal, vatAmount, total);

        return new OrderResult(subtotal, discount, vatAmount, total, receipt);
    }

    private void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new OrderValidationException("Line item cannot be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description cannot be empty.");
            }
            if (item.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + item.getDescription() + "' must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item '" + item.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal, BigDecimal discount,
                                 BigDecimal discountedSubtotal, BigDecimal vatAmount, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt - ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .append(System.lineSeparator());
        sb.append("--------------------------------------------------").append(System.lineSeparator());

        for (LineItem item : lineItems) {
            sb.append(String.format("%-30s %3d x %8s = %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatAmount(item.getUnitPrice()),
                    formatAmount(item.lineTotal())));
        }

        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-46s %10s%n", "Subtotal (ex. VAT):", formatAmount(subtotal)));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-46s %10s%n", "Discount (10%):", "-" + formatAmount(discount)));
            sb.append(String.format("%-46s %10s%n", "Subtotal after discount:", formatAmount(discountedSubtotal)));
        }

        sb.append(String.format("%-46s %10s%n", "VAT (21%):", formatAmount(vatAmount)));
        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-46s %10s%n", "Total:", formatAmount(total)));

        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "EUR " + amount.setScale(SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        List<LineItem> items = List.of(
                new LineItem("Lottery subscription - monthly", 2, new BigDecimal("45.00")),
                new LineItem("Charity donation add-on", 1, new BigDecimal("15.00"))
        );

        OrderProcessor processor = new OrderProcessor();
        OrderResult result = processor.processOrder(items);
        System.out.println(result.receipt);
    }
}
