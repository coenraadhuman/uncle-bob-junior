import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public record LineItem(String description, BigDecimal unitPrice, int quantity) {

        public LineItem {
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING);
        }
    }

    public record Receipt(BigDecimal subtotal,
                          BigDecimal discount,
                          BigDecimal vat,
                          BigDecimal total,
                          String text) {
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public Receipt process(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING);
        }

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = netAfterDiscount.add(vat).setScale(SCALE, ROUNDING);

        String text = buildReceiptText(items, subtotal, discount, vat, total);
        return new Receipt(subtotal, discount, vat, total, text);
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new InvalidOrderException("Line item " + (i + 1) + " is null.");
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has no description.");
            }
            if (item.quantity() <= 0) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has a non-positive quantity.");
            }
            if (item.unitPrice().signum() < 0) {
                throw new InvalidOrderException("Line item " + (i + 1) + " has a negative unit price.");
            }
        }
    }

    private String buildReceiptText(List<LineItem> items,
                                    BigDecimal subtotal,
                                    BigDecimal discount,
                                    BigDecimal vat,
                                    BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT").append(System.lineSeparator());
        sb.append("--------------------------------------------").append(System.lineSeparator());
        for (LineItem item : items) {
            sb.append(String.format("%-20s %3d x EUR %8s = EUR %9s%n",
                    truncate(item.description(), 20),
                    item.quantity(),
                    item.unitPrice().setScale(SCALE, ROUNDING).toPlainString(),
                    item.lineTotal().toPlainString()));
        }
        sb.append("--------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("Subtotal (excl. VAT):        EUR %9s%n", subtotal.toPlainString()));
        if (discount.signum() > 0) {
            sb.append(String.format("Discount (10%%):             -EUR %9s%n", discount.toPlainString()));
        }
        sb.append(String.format("VAT (21%%):                   EUR %9s%n", vat.toPlainString()));
        sb.append(String.format("Total (incl. VAT):           EUR %9s%n", total.toPlainString()));
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> order = List.of(
                new LineItem("Notebook", new BigDecimal("12.50"), 4),
                new LineItem("Desk lamp", new BigDecimal("34.99"), 2)
        );
        Receipt receipt = processor.process(order);
        System.out.print(receipt.text());
    }
}
