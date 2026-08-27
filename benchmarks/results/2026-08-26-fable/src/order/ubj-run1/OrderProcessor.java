import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public record LineItem(String name, int quantity, BigDecimal unitPrice) {}

    public String process(List<LineItem> items) {
        validate(items);
        BigDecimal subtotal = subtotalOf(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal netTotal = subtotal.subtract(discount);
        BigDecimal vat = vatOn(netTotal);
        BigDecimal grandTotal = netTotal.add(vat);
        return buildReceipt(items, subtotal, discount, vat, grandTotal);
    }

    private void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        items.forEach(this::validateItem);
    }

    private void validateItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Line item must not be null");
        }
        if (item.name() == null || item.name().isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (item.quantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1: " + item.name());
        }
        if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative: " + item.name());
        }
    }

    private BigDecimal subtotalOf(List<LineItem> items) {
        return items.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineTotal(LineItem item) {
        return item.unitPrice()
                .multiply(BigDecimal.valueOf(item.quantity()))
                .setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal vatOn(BigDecimal netTotal) {
        return netTotal.multiply(VAT_RATE).setScale(CENTS, RoundingMode.HALF_UP);
    }

    private String buildReceipt(List<LineItem> items, BigDecimal subtotal,
                                BigDecimal discount, BigDecimal vat, BigDecimal grandTotal) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        receipt.append(String.format("Subtotal (excl. VAT): EUR %s%n", subtotal));
        if (discount.signum() > 0) {
            receipt.append(String.format("Discount (10%%): -EUR %s%n", discount));
        }
        receipt.append(String.format("VAT (21%%): EUR %s%n", vat));
        receipt.append(String.format("Total: EUR %s%n", grandTotal));
        return receipt.toString();
    }

    private String formatLine(LineItem item) {
        return String.format("%d x %s @ EUR %s = EUR %s%n",
                item.quantity(), item.name(), item.unitPrice(), lineTotal(item));
    }
}
