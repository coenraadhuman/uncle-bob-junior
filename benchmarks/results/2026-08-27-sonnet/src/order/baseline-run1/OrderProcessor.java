import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {
        public LineItem {
            Objects.requireNonNull(description, "description must not be null");
            if (description.isBlank()) {
                throw new IllegalArgumentException("description must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + quantity);
            }
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            if (unitPrice.signum() < 0) {
                throw new IllegalArgumentException("unitPrice must not be negative: " + unitPrice);
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String receipt) {
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one line item");
        }

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

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total, discountApplies);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private String buildReceipt(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(
                    "%-20s %3d x %8.2f = %10.2f%n",
                    item.description(),
                    item.quantity(),
                    item.unitPrice(),
                    item.lineTotal().setScale(2, RoundingMode.HALF_UP)));
        }
        sb.append("-------\n");
        sb.append(String.format("Subtotal (excl. VAT): EUR %10.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format("Discount (10%%):       EUR %10.2f%n", discount.negate()));
        }
        sb.append(String.format("VAT (21%%):            EUR %10.2f%n", vat));
        sb.append(String.format("Total (incl. VAT):    EUR %10.2f%n", total));
        return sb.toString();
    }
}
