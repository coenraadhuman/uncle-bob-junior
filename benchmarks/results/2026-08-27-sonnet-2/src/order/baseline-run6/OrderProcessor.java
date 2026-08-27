import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, BigDecimal unitPrice, int quantity) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank.");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException("Line item '" + description + "' has an invalid unit price.");
            }
            if (quantity <= 0) {
                throw new InvalidOrderException("Line item '" + description + "' must have a positive quantity.");
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public record Receipt(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vatAmount,
            BigDecimal total,
            String text) {
    }

    public Receipt processOrder(List<LineItem> lineItems) {
        validate(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        String text = buildReceiptText(lineItems, subtotal, discount, vatAmount, total, discountApplies);

        return new Receipt(subtotal, discount, vatAmount, total, text);
    }

    private void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            Objects.requireNonNull(item, "Line item must not be null.");
        }
    }

    private String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vatAmount,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(
                    "%-20s %3d x %8s = %10s%n",
                    item.description(),
                    item.quantity(),
                    formatAmount(item.unitPrice()),
                    formatAmount(item.lineTotal().setScale(2, RoundingMode.HALF_UP))));
        }
        sb.append("-------\n");
        sb.append(String.format("Subtotal:        %10s%n", formatAmount(subtotal)));
        if (discountApplies) {
            sb.append(String.format("Discount (10%%):  -%9s%n", formatAmount(discount)));
        }
        sb.append(String.format("VAT (21%%):        %10s%n", formatAmount(vatAmount)));
        sb.append(String.format("Total:            %10s%n", formatAmount(total)));
        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", new BigDecimal("45.00"), 2),
                new LineItem("Donation add-on", new BigDecimal("15.00"), 1)
        );

        Receipt receipt = processor.processOrder(items);
        System.out.print(receipt.text());
    }
}
