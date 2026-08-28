// File: ReceiptFormatter.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";
    private static final String NEWLINE = System.lineSeparator();
    private static final String RECEIPT_TITLE = "Receipt";

    public String format(Iterable<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        receipt.append(RECEIPT_TITLE).append(NEWLINE);
        lineItems.forEach(item -> appendLineItem(receipt, item));
        appendTotalsSection(receipt, totals);
        return receipt.toString();
    }

    private void appendLineItem(StringBuilder receipt, LineItem item) {
        receipt.append(item.description())
                .append(" x").append(item.quantity())
                .append(": ").append(formatAmount(item.lineTotal())).append(" ").append(CURRENCY)
                .append(NEWLINE);
    }

    private void appendTotalsSection(StringBuilder receipt, OrderTotals totals) {
        receipt.append("Subtotal: ").append(formatAmount(totals.preDiscountSubtotal())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("Discount: -").append(formatAmount(totals.discountAmount())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("VAT (21%): ").append(formatAmount(totals.vatAmount())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("Total: ").append(formatAmount(totals.grandTotal())).append(" ").append(CURRENCY).append(NEWLINE);
    }

    private String formatAmount(BigDecimal amount) {
        return amount.toPlainString();
    }
}
