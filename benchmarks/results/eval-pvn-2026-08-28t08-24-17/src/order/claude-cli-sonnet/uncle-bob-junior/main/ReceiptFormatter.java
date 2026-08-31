// ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private static final String SEPARATOR = "--------------";
    private static final String LINE_BREAK = System.lineSeparator();

    public String format(List<LineItem> lineItems, OrderSummary summary) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendSummary(receipt, summary);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Order Receipt").append(LINE_BREAK);
        receipt.append(SEPARATOR).append(LINE_BREAK);
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append(LINE_BREAK);
        }
        receipt.append(SEPARATOR).append(LINE_BREAK);
    }

    private String formatLineItem(LineItem item) {
        return "%s x%d @ %s = %s".formatted(
            item.description(), item.quantity(), formatMoney(item.unitPrice()), formatMoney(item.lineTotal()));
    }

    private void appendSummary(StringBuilder receipt, OrderSummary summary) {
        String vatLabel = "VAT (%s%%): ".formatted(
            OrderCalculator.VAT_RATE.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());
        receipt.append("Subtotal: ").append(formatMoney(summary.subtotal())).append(LINE_BREAK);
        receipt.append("Discount: -").append(formatMoney(summary.discount())).append(LINE_BREAK);
        receipt.append(vatLabel).append(formatMoney(summary.vatAmount())).append(LINE_BREAK);
        receipt.append("Total: ").append(formatMoney(summary.total())).append(LINE_BREAK);
    }

    private String formatMoney(BigDecimal amount) {
        return String.format(Locale.ROOT, "\u20AC%,.2f", amount);
    }
}
