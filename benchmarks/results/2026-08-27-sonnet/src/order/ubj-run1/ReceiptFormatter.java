// ReceiptFormatter.java
package com.plg.orders;

import java.math.BigDecimal;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SYMBOL = "EUR";

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
            receipt.append(String.format("%-30s %2d x %8s = %10s%n",
                    item.description(), item.quantity(),
                    formatAmount(item.unitPrice()), formatAmount(item.lineTotal())));
        }
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-".repeat(60)).append(System.lineSeparator());
        receipt.append(String.format("%-47s %10s%n", "Subtotal", formatAmount(totals.subtotalBeforeDiscount())));
        if (totals.discount().signum() > 0) {
            receipt.append(String.format("%-47s -%9s%n", "Discount (10%)", formatAmount(totals.discount())));
        }
        receipt.append(String.format("%-47s %10s%n", "VAT (21%)", formatAmount(totals.vat())));
        receipt.append(String.format("%-47s %10s%n", "Total", formatAmount(totals.total())));
    }

    private static String formatAmount(BigDecimal amount) {
        return CURRENCY_SYMBOL + " " + amount.toPlainString();
    }
}
