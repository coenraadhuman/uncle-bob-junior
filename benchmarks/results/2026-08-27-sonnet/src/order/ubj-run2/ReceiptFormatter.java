// ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;

final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    static String format(List<OrderItem> items, OrderPricing pricing) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt").append(System.lineSeparator());
        receipt.append("-------").append(System.lineSeparator());
        items.forEach(item -> receipt.append(formatLine(item)).append(System.lineSeparator()));
        receipt.append("-------").append(System.lineSeparator());
        receipt.append(formatAmount("Subtotal", pricing.subtotal())).append(System.lineSeparator());
        if (pricing.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatAmount("Discount (10%)", pricing.discount().negate())).append(System.lineSeparator());
        }
        receipt.append(formatAmount("VAT (21%)", pricing.vat())).append(System.lineSeparator());
        receipt.append(formatAmount("Total", pricing.total()));
        return receipt.toString();
    }

    private static String formatLine(OrderItem item) {
        return "%2d x %-20s EUR %8.2f".formatted(item.quantity(), item.description(), item.lineTotal());
    }

    private static String formatAmount(String label, BigDecimal amount) {
        return "%-20s EUR %8.2f".formatted(label, amount);
    }
}
