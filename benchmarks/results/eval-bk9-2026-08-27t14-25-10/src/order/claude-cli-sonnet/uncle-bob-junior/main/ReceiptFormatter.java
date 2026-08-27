public final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    public static String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private static void appendHeader(StringBuilder receipt) {
        receipt.append("Receipt\n").append("-------\n");
    }

    private static void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append('\n');
        }
    }

    private static String formatLineItem(LineItem item) {
        return String.format(Locale.UK, "%dx %-20s EUR %8.2f",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------\n");
        receipt.append(formatMoneyLine("Subtotal", totals.subtotal()));
        if (totals.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatMoneyLine("Discount (10%)", totals.discount().negate()));
        }
        receipt.append(formatMoneyLine("VAT (21%)", totals.vat()));
        receipt.append(formatMoneyLine("Total", totals.total()));
    }

    private static String formatMoneyLine(String label, BigDecimal amount) {
        return String.format(Locale.UK, "%-20s EUR %8.2f%n", label, amount);
    }
}

// ---- OrderProcessor.java ----
import java.util.List;
