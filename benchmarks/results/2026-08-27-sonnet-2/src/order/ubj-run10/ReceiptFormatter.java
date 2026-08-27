// ReceiptFormatter.java
import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String SEPARATOR = "---------------------------------------------";

    private ReceiptFormatter() {
    }

    public static String format(Order order, BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder("Receipt").append(System.lineSeparator());
        order.items().forEach(item -> appendLineItem(receipt, item));
        appendSummary(receipt, subtotal, discount, vat, total);
        return receipt.toString();
    }

    private static void appendLineItem(StringBuilder receipt, LineItem item) {
        receipt.append(String.format("%-20s %2d x %8s = %10s%n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
    }

    private static void appendSummary(StringBuilder receipt, BigDecimal subtotal, BigDecimal discount,
                                       BigDecimal vat, BigDecimal total) {
        receipt.append(SEPARATOR).append(System.lineSeparator());
        receipt.append(String.format("Subtotal (excl. VAT): %10s EUR%n", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):       %10s EUR%n", discount));
        }
        receipt.append(String.format("VAT (21%%):            %10s EUR%n", vat));
        receipt.append(String.format("Total:                %10s EUR%n", total));
    }
}
