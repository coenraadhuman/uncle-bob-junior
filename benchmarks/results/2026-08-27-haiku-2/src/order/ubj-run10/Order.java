import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = new ArrayList<>(lineItems);
    }

    public double getSubtotalBeforeDiscount() {
        return lineItems.stream()
                .mapToDouble(LineItem::getLineTotal)
                .sum();
    }

    public boolean isEligibleForDiscount() {
        return getSubtotalBeforeDiscount() > DISCOUNT_THRESHOLD;
    }

    public double getDiscountAmount() {
        return isEligibleForDiscount() ? getSubtotalBeforeDiscount() * DISCOUNT_RATE : 0.0;
    }

    public double getSubtotalAfterDiscount() {
        return getSubtotalBeforeDiscount() - getDiscountAmount();
    }

    public double getVatAmount() {
        return getSubtotalAfterDiscount() * VAT_RATE;
    }

    public double getTotalWithVat() {
        return getSubtotalAfterDiscount() + getVatAmount();
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        appendTotals(receipt);
        receipt.append("====================\n");
        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPricePerUnit(),
                    item.getLineTotal()));
        }
    }

    private void appendTotals(StringBuilder receipt) {
        receipt.append(String.format("Subtotal:     €%.2f\n", getSubtotalBeforeDiscount()));

        if (isEligibleForDiscount()) {
            receipt.append(String.format("Discount 10%: -€%.2f\n", getDiscountAmount()));
        }

        receipt.append(String.format("After disc.:  €%.2f\n", getSubtotalAfterDiscount()));
        receipt.append(String.format("VAT 21%%:      €%.2f\n", getVatAmount()));
        receipt.append(String.format("TOTAL:        €%.2f\n", getTotalWithVat()));
    }
}
