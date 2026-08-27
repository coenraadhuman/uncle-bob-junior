// ReceiptFormatter.java
package com.example.order;

import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private static final int SEPARATOR_WIDTH = 34;

    private ReceiptFormatter() {
    }

    public static String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private static void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private static String formatLine(LineItem item) {
        return String.format(Locale.ROOT, "%2d x %-20s EUR %8.2f",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-".repeat(SEPARATOR_WIDTH)).append(System.lineSeparator());
        receipt.append(formatTotalLine("Subtotal", totals.subtotal())).append(System.lineSeparator());
        if (totals.discount().signum() > 0) {
            receipt.append(formatTotalLine("Discount (10%)", totals.discount().negate()))
                    .append(System.lineSeparator());
        }
        receipt.append(formatTotalLine("VAT (21%)", totals.vat())).append(System.lineSeparator());
        receipt.append(formatTotalLine("Total", totals.total()));
    }

    private static String formatTotalLine(String label, java.math.BigDecimal amount) {
        return String.format(Locale.ROOT, "%-20s EUR %8.2f", label, amount);
    }
}
