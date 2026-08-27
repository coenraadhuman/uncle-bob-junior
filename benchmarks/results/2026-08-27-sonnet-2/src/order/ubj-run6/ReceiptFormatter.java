package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";

    String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        lineItems.forEach(item -> appendLine(receipt, item));
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Order Receipt\n");
        receipt.append("-------------\n");
    }

    private void appendLine(StringBuilder receipt, LineItem item) {
        BigDecimal lineTotal = item.unitPrice()
                .multiply(BigDecimal.valueOf(item.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
        receipt.append("%d x %s @ %s %.2f = %s %.2f%n".formatted(
                item.quantity(), item.description(), CURRENCY, item.unitPrice(), CURRENCY, lineTotal));
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------------\n");
        appendMoneyLine(receipt, "Subtotal", totals.subtotal());
        appendMoneyLine(receipt, "Discount (10%)", totals.discount().negate());
        appendMoneyLine(receipt, "VAT (21%)", totals.vat());
        appendMoneyLine(receipt, "Total", totals.total());
    }

    private void appendMoneyLine(StringBuilder receipt, String label, BigDecimal amount) {
        receipt.append("%s: %s %.2f%n".formatted(label, CURRENCY, amount));
    }
}
