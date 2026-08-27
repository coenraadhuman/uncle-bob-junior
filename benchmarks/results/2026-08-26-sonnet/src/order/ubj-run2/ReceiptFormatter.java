import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SUFFIX = " EUR";
    private static final String SEPARATOR = "-".repeat(32);
    private static final String ITEM_ROW_FORMAT = "%dx %-20s %8s";
    private static final String AMOUNT_ROW_FORMAT = "%-22s %8s";

    public String format(Receipt receipt) {
        StringBuilder receiptText = new StringBuilder();
        appendLineItems(receiptText, receipt.lineItems());
        receiptText.append(SEPARATOR).append(System.lineSeparator());
        appendAmountRow(receiptText, "Subtotal", receipt.subtotal());
        appendDiscountRowIfApplied(receiptText, receipt.discount());
        appendAmountRow(receiptText, "VAT (21%)", receipt.vat());
        appendAmountRow(receiptText, "Total", receipt.total());
        return receiptText.toString();
    }

    private void appendLineItems(StringBuilder receiptText, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receiptText.append(formatLineItem(item)).append(System.lineSeparator());
        }
    }

    private String formatLineItem(LineItem item) {
        return ITEM_ROW_FORMAT.formatted(
                item.quantity(), item.description(), formatMoney(item.lineTotal()));
    }

    private void appendDiscountRowIfApplied(StringBuilder receiptText, BigDecimal discount) {
        if (discount.signum() > 0) {
            appendAmountRow(receiptText, "Discount (10%)", discount.negate());
        }
    }

    private void appendAmountRow(StringBuilder receiptText, String label, BigDecimal amount) {
        receiptText.append(AMOUNT_ROW_FORMAT.formatted(label, formatMoney(amount)))
                .append(System.lineSeparator());
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP) + CURRENCY_SUFFIX;
    }
}
