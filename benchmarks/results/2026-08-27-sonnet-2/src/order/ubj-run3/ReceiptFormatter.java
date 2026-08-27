package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ReceiptFormatter {

    private static final String CURRENCY_PREFIX = "EUR ";
    private static final int MONEY_SCALE = 2;

    public String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n-------\n");
        lineItems.forEach(item -> receipt.append(formatLineItem(item)));
        receipt.append("-------\n");
        receipt.append(formatAmountLine("Subtotal", totals.subtotal()));
        appendDiscountLine(receipt, totals.discount());
        receipt.append(formatAmountLine("VAT (21%)", totals.vat()));
        receipt.append(formatAmountLine("Total", totals.total()));
        return receipt.toString();
    }

    private void appendDiscountLine(StringBuilder receipt, BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatAmountLine("Discount (10%)", discount.negate()));
        }
    }

    private String formatLineItem(LineItem item) {
        return String.format("%-20s %2d x %10s = %12s%n",
                item.description(), item.quantity(), formatMoney(item.unitPrice()), formatMoney(item.lineTotal()));
    }

    private String formatAmountLine(String label, BigDecimal amount) {
        return String.format("%-20s %27s%n", label, formatMoney(amount));
    }

    private String formatMoney(BigDecimal amount) {
        return CURRENCY_PREFIX + amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
