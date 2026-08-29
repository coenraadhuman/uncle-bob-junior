// ReceiptFormatter.java
import java.util.List;

public final class ReceiptFormatter {

    public String format(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, items);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Receipt").append(System.lineSeparator());
        receipt.append("-------").append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> items) {
        for (LineItem item : items) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private String formatLine(LineItem item) {
        return "%s x%d @ %s = %s".formatted(item.description(), item.quantity(), item.unitPrice(), item.lineTotal());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------").append(System.lineSeparator());
        receipt.append("Subtotal: ").append(totals.subtotal()).append(System.lineSeparator());
        receipt.append("Discount: -").append(totals.discountAmount()).append(System.lineSeparator());
        receipt.append("VAT (21%): ").append(totals.vatAmount()).append(System.lineSeparator());
        receipt.append("Total: ").append(totals.total());
    }
}
