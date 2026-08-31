import java.math.BigDecimal;
import java.util.List;

/**
 * Priced order result. All amounts are in EUR, rounded to cents.
 * Invariant: total = subtotal - discount + vat.
 */
public record Receipt(List<LineItem> items,
                      BigDecimal subtotal,
                      BigDecimal discount,
                      BigDecimal vat,
                      BigDecimal total) {

    public Receipt {
        items = List.copyOf(items);
    }

    public String format() {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        appendAmount(receipt, "Subtotal", subtotal);
        if (discount.signum() > 0) {
            appendAmount(receipt, "Discount", discount.negate());
        }
        appendAmount(receipt, "VAT (21%)", vat);
        appendAmount(receipt, "Total", total);
        return receipt.toString();
    }

    private static String formatLine(LineItem item) {
        return String.format("%s x%d @ EUR %s = EUR %s%n",
                item.name(), item.quantity(),
                item.unitPrice().toPlainString(),
                item.lineTotal().toPlainString());
    }

    private static void appendAmount(StringBuilder receipt, String label, BigDecimal amount) {
        receipt.append(String.format("%s: EUR %s%n", label, amount.toPlainString()));
    }
}
