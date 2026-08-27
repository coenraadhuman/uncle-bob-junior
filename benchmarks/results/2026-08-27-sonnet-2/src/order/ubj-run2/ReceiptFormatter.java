package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Renders a {@link Receipt} as a human-readable text receipt.
 */
public final class ReceiptFormatter {

    private static final String CURRENCY_SYMBOL = "\u20AC";
    private static final String SEPARATOR = "-".repeat(40);
    private static final String LINE_ITEM_FORMAT = "%d x %-24s %s%s";
    private static final String TOTAL_LINE_FORMAT = "%-26s %s%s";
    private static final int DISPLAY_SCALE = 2;

    public String format(Receipt receipt) {
        StringBuilder text = new StringBuilder();
        appendHeader(text);
        appendLineItems(text, receipt.lineItems());
        text.append(SEPARATOR).append(System.lineSeparator());
        appendTotals(text, receipt);
        return text.toString();
    }

    private void appendHeader(StringBuilder text) {
        text.append("Order Receipt").append(System.lineSeparator());
        text.append(SEPARATOR).append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder text, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            text.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return LINE_ITEM_FORMAT.formatted(
                item.quantity(), item.description(), CURRENCY_SYMBOL, formatAmount(item.lineTotal()));
    }

    private void appendTotals(StringBuilder text, Receipt receipt) {
        appendAmountLine(text, "Subtotal:", receipt.subtotal());
        if (receipt.discount().compareTo(BigDecimal.ZERO) > 0) {
            appendAmountLine(text, "Discount (10%):", receipt.discount().negate());
        }
        appendAmountLine(text, "VAT (21%):", receipt.vat());
        appendAmountLine(text, "Total:", receipt.total());
    }

    private void appendAmountLine(StringBuilder text, String label, BigDecimal amount) {
        text.append(TOTAL_LINE_FORMAT.formatted(label, CURRENCY_SYMBOL, formatAmount(amount)));
        text.append(System.lineSeparator());
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
