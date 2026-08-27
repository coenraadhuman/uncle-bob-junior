import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public String processOrder(List<LineItem> items) {
        validateItems(items);

        BigDecimal subtotal = computeSubtotal(items);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal vat = afterDiscount.multiply(VAT_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vat);

        return formatReceipt(items, subtotal, discount, vat, total);
    }

    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items.forEach(LineItem::validate);
    }

    private BigDecimal computeSubtotal(List<LineItem> items) {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private String formatReceipt(List<LineItem> items, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");

        items.forEach(item -> receipt.append(formatLineItem(item)));

        receipt.append("-----------\n");
        receipt.append(formatLine("Subtotal", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatLine("Discount (10%)", discount.negate()));
        }

        receipt.append(formatLine("VAT (21%)", vat));
        receipt.append("-----------\n");
        receipt.append(formatLine("TOTAL", total));

        return receipt.toString();
    }

    private String formatLineItem(LineItem item) {
        return String.format("%s x%d @ €%.2f = €%.2f%n",
            item.getDescription(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.lineTotal());
    }

    private String formatLine(String label, BigDecimal amount) {
        return String.format("%-20s €%.2f%n", label, amount);
    }
}

// Usage example:
// OrderProcessor processor = new OrderProcessor();
// List<LineItem> items = List.of(
//     new LineItem("Laptop", 1, new BigDecimal("600.00")),
//     new LineItem("Mouse", 2, new BigDecimal("25.00"))
// );
// String receipt = processor.processOrder(items);
// System.out.println(receipt);
