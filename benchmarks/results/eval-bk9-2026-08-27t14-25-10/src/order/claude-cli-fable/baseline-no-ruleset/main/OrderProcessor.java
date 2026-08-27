import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String name, int quantity, BigDecimal unitPrice) {}

    public record Receipt(BigDecimal subtotal, BigDecimal discount,
                          BigDecimal vat, BigDecimal total, String text) {}

    public Receipt process(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = netAfterDiscount.add(vat);

        return new Receipt(subtotal, discount, vat, total,
                buildReceiptText(items, subtotal, discount, vat, total));
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Line item must not be null");
            }
            if (item.name() == null || item.name().isBlank()) {
                throw new IllegalArgumentException("Line item name must not be blank");
            }
            if (item.quantity() < 1) {
                throw new IllegalArgumentException(
                        "Quantity must be at least 1 for item: " + item.name());
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "Unit price must be zero or positive for item: " + item.name());
            }
        }
    }

    private String buildReceiptText(List<LineItem> items, BigDecimal subtotal,
                                    BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("-".repeat(40)).append('\n');
        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            sb.append(String.format("%-20s %3d x %8s  %9s%n",
                    item.name(), item.quantity(),
                    "EUR " + item.unitPrice().setScale(2, RoundingMode.HALF_UP),
                    "EUR " + lineTotal));
        }
        sb.append("-".repeat(40)).append('\n');
        sb.append(String.format("%-25s %14s%n", "Subtotal (excl. VAT)", "EUR " + subtotal));
        if (discount.signum() > 0) {
            sb.append(String.format("%-25s %14s%n", "Discount (10%)", "-EUR " + discount));
        }
        sb.append(String.format("%-25s %14s%n", "VAT (21%)", "EUR " + vat));
        sb.append(String.format("%-25s %14s%n", "Total", "EUR " + total));
        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        Receipt receipt = processor.process(List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 2, new BigDecimal("45.00"))
        ));
        System.out.print(receipt.text());
    }
}
