// filename: ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;

class ReceiptFormatter {

    private static final String CURRENCY = "EUR";
    private static final String HEADER = "=== Order Receipt ===";
    private static final String VAT_LABEL =
            "VAT (" + PricingRules.VAT_RATE.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString() + "%)";

    String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append(HEADER).append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return "%d x %s @ %s %s = %s %s".formatted(
                item.quantity(), item.productName(),
                CURRENCY, item.unitPrice().toPlainString(),
                CURRENCY, item.lineTotal().toPlainString());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append(formatMoneyLine("Subtotal", totals.subtotal())).append(System.lineSeparator());
        receipt.append(formatMoneyLine("Discount", totals.discount())).append(System.lineSeparator());
        receipt.append(formatMoneyLine(VAT_LABEL, totals.vat())).append(System.lineSeparator());
        receipt.append(formatMoneyLine("Total", totals.total()));
    }

    private String formatMoneyLine(String label, BigDecimal amount) {
        return "%s: %s %s".formatted(label, CURRENCY, amount.toPlainString());
    }
}
