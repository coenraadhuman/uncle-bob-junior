// File: ReceiptFormatter.java
import java.util.List;

public final class ReceiptFormatter {

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
        return String.format("%-20s x%-3d %8.2f EUR",
                item.description(), item.quantity(), item.lineTotal());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append(String.format("%-24s %8.2f EUR%n", "Subtotal", totals.subtotal()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "Discount", totals.discount().negate()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "VAT (21%)", totals.vat()));
        receipt.append(String.format("%-24s %8.2f EUR%n", "Total", totals.total()));
    }
}
