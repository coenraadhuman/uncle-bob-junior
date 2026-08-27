import java.util.List;
import java.util.Locale;

/** Turns validated line items and computed totals into a human-readable receipt. */
public final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";

    public String format(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(lineFor(item)));
        receipt.append(row("Subtotal:", totals.subtotal()));
        if (totals.discount().signum() > 0) {
            receipt.append(row("Discount (10%):", totals.discount().negate()));
        }
        receipt.append(row("VAT (21%):", totals.vat()));
        receipt.append(row("Total:", totals.total()));
        return receipt.toString();
    }

    private String lineFor(LineItem item) {
        return String.format(Locale.ROOT, "%s x %d @ %s %s = %s %s%n",
                item.description(), item.quantity(),
                CURRENCY, item.unitPrice(),
                CURRENCY, item.lineTotal());
    }

    private String row(String label, Object amount) {
        return String.format(Locale.ROOT, "%-16s %s %s%n", label, CURRENCY, amount);
    }
}
