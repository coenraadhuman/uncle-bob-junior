import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    public static String receiptFor(List<LineItem> items) {
        OrderTotals totals = OrderCalculator.totalsFor(items);
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(lineFor(item)));
        receipt.append(totalsBlockFor(totals));
        return receipt.toString();
    }

    private static String lineFor(LineItem item) {
        return String.format(Locale.ROOT, "%-20s %3d x %9.2f = %10.2f%n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal());
    }

    private static String totalsBlockFor(OrderTotals totals) {
        StringBuilder block = new StringBuilder();
        block.append(amountRow("Subtotal (excl. VAT)", totals.subtotal()));
        if (totals.hasDiscount()) {
            block.append(amountRow("Discount (10%)", totals.discount().negate()));
            block.append(amountRow("Net (excl. VAT)", totals.netAmount()));
        }
        block.append(amountRow("VAT (21%)", totals.vat()));
        block.append(amountRow("TOTAL (EUR)", totals.grandTotal()));
        return block.toString();
    }

    private static String amountRow(String label, java.math.BigDecimal amount) {
        return String.format(Locale.ROOT, "%-25s %12.2f%n", label, amount);
    }
}
