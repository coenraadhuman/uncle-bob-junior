// ReceiptFormatter.java
package com.plg.order;

import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY_PREFIX = "EUR ";

    public String format(Receipt receipt) {
        StringBuilder text = new StringBuilder();
        appendLineItems(text, receipt);
        appendTotals(text, receipt);
        return text.toString();
    }

    private void appendLineItems(StringBuilder text, Receipt receipt) {
        text.append("Receipt\n-------\n");
        for (OrderLineItem item : receipt.lineItems()) {
            text.append("%-20s %2d x %10s = %10s%n".formatted(
                    item.description(), item.quantity(), euro(item.unitPrice()), euro(item.lineTotal())));
        }
        text.append("-------\n");
    }

    private void appendTotals(StringBuilder text, Receipt receipt) {
        text.append("Subtotal:      %10s%n".formatted(euro(receipt.subtotal())));
        if (receipt.hasDiscount()) {
            text.append("Discount (10%%): -%9s%n".formatted(euro(receipt.discount())));
        }
        text.append("VAT (21%%):     %10s%n".formatted(euro(receipt.vat())));
        text.append("Total:         %10s%n".formatted(euro(receipt.total())));
    }

    private String euro(BigDecimal amount) {
        return CURRENCY_PREFIX + amount;
    }
}
