// ReceiptFormatter.java
import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY_LABEL = "EUR";

    public String format(OrderReceipt receipt) {
        StringBuilder builder = new StringBuilder();
        appendLineItems(builder, receipt);
        appendTotals(builder, receipt);
        return builder.toString();
    }

    private void appendLineItems(StringBuilder builder, OrderReceipt receipt) {
        for (LineItem item : receipt.lineItems()) {
            builder.append(item.description())
                    .append(" x")
                    .append(item.quantity())
                    .append(": ")
                    .append(formatAmount(item.lineTotal()))
                    .append(System.lineSeparator());
        }
    }

    private void appendTotals(StringBuilder builder, OrderReceipt receipt) {
        builder.append("Subtotal: ").append(formatAmount(receipt.subtotal())).append(System.lineSeparator());
        builder.append("Discount: -").append(formatAmount(receipt.discount())).append(System.lineSeparator());
        builder.append("VAT (21%): ").append(formatAmount(receipt.vat())).append(System.lineSeparator());
        builder.append("Total: ").append(formatAmount(receipt.total()));
    }

    private String formatAmount(BigDecimal amount) {
        return CURRENCY_LABEL + " " + amount.toPlainString();
    }
}
