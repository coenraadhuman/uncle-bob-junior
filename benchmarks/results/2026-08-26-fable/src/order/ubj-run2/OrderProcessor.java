import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Processes an order: validates line items, applies the volume discount,
 * adds VAT, and renders a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EX_VAT = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public String processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = subtotalExVat(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(items, subtotal, discount, vat, total);
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
        if (item.productName() == null || item.productName().isBlank()) {
            throw new IllegalArgumentException("Line item must have a product name");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive for product: " + item.productName());
        }
        if (item.unitPriceExVat() == null
                || item.unitPriceExVat().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Unit price must be zero or more for product: " + item.productName());
        }
    }

    private BigDecimal subtotalExVat(List<LineItem> items) {
        return items.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CENTS, RoundingMode.HALF_UP);
    }

    private BigDecimal lineTotal(LineItem item) {
        return item.unitPriceExVat().multiply(BigDecimal.valueOf(item.quantity()));
    }

    private BigDecimal discountFor(BigDecimal subtotalExVat) {
        if (subtotalExVat.compareTo(DISCOUNT_THRESHOLD_EX_VAT) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS, RoundingMode.HALF_UP);
        }
        return roundToCents(subtotalExVat.multiply(DISCOUNT_RATE));
    }

    private BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }

    private String buildReceipt(List<LineItem> items, BigDecimal subtotal,
                                BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        receipt.append(formatAmount("Subtotal (ex VAT)", subtotal));
        if (discount.signum() > 0) {
            receipt.append(formatAmount("Discount (10%)", discount.negate()));
        }
        receipt.append(formatAmount("VAT (21%)", vat));
        receipt.append(formatAmount("Total", total));
        return receipt.toString();
    }

    private String formatLine(LineItem item) {
        return String.format("%-20s %3d x EUR %10.2f = EUR %10.2f%n",
                item.productName(), item.quantity(),
                item.unitPriceExVat(), roundToCents(lineTotal(item)));
    }

    private String formatAmount(String label, BigDecimal amount) {
        return String.format("%-30s EUR %10.2f%n", label, amount);
    }
}
