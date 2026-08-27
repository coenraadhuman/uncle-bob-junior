import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.text.NumberFormat;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new OrderValidationException("Line item description must not be blank");
            }
            if (quantity <= 0) {
                throw new OrderValidationException("Line item quantity must be positive: " + description);
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException("Line item unit price must not be negative: " + description);
            }
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

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netTotal,
            BigDecimal vat,
            BigDecimal grandTotal,
            String receipt) {
    }

    public OrderResult processOrder(List<LineItem> items) {
        validateItems(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal netTotal = subtotal.subtract(discount).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal vat = netTotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal grandTotal = netTotal.add(vat).setScale(SCALE, RoundingMode.HALF_UP);

        String receipt = buildReceipt(items, subtotal, discount, netTotal, vat, grandTotal, discountApplies);

        return new OrderResult(subtotal, discount, netTotal, vat, grandTotal, receipt);
    }

    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            Objects.requireNonNull(item, "Line item must not be null");
        }
    }

    private String buildReceipt(
            List<LineItem> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netTotal,
            BigDecimal vat,
            BigDecimal grandTotal,
            boolean discountApplied) {

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nl-NL"));
        StringBuilder sb = new StringBuilder();

        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : items) {
            sb.append(String.format(
                    "%-20s %3d x %10s = %10s%n",
                    item.description(),
                    item.quantity(),
                    currency.format(item.unitPrice()),
                    currency.format(item.lineTotal())));
        }
        sb.append("-------\n");
        sb.append(String.format("%-20s %20s%n", "Subtotal (excl. VAT)", currency.format(subtotal)));
        if (discountApplied) {
            sb.append(String.format("%-20s %20s%n", "Discount (10%)", "-" + currency.format(discount)));
        }
        sb.append(String.format("%-20s %20s%n", "Net total", currency.format(netTotal)));
        sb.append(String.format("%-20s %20s%n", "VAT (21%)", currency.format(vat)));
        sb.append(String.format("%-20s %20s%n", "Grand total", currency.format(grandTotal)));

        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Gift voucher", 1, new BigDecimal("30.00")));

        OrderResult result = processor.processOrder(items);
        System.out.print(result.receipt());
    }
}
