package com.plg.orders;

import java.math.BigDecimal;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SUFFIX = " EUR";

    public String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private String formatLine(LineItem item) {
        return "%dx %s @ %s%s = %s%s".formatted(
                item.quantity(), item.description(),
                money(item.unitPrice()), CURRENCY_SUFFIX,
                money(item.lineTotal()), CURRENCY_SUFFIX);
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("Subtotal: ").append(money(totals.subtotalBeforeDiscount()))
                .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        if (totals.discountAmount().signum() > 0) {
            receipt.append("Discount: -").append(money(totals.discountAmount()))
                    .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        }
        receipt.append("VAT (21%): ").append(money(totals.vatAmount()))
                .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        receipt.append("Total: ").append(money(totals.total())).append(CURRENCY_SUFFIX);
    }

    private String money(BigDecimal amount) {
        return amount.toPlainString();
    }
}
