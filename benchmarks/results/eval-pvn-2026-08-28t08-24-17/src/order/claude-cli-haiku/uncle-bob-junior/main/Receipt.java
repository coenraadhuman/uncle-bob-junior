import java.util.Collections;
import java.util.List;

public final class Receipt {
    private final List<LineItem> lineItems;
    private final OrderCalculation calculation;

    public Receipt(List<LineItem> lineItems, OrderCalculation calculation) {
        this.lineItems = Collections.unmodifiableList(lineItems);
        this.calculation = calculation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");

        for (LineItem item : lineItems) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description(), item.quantity(), item.pricePerUnit(), item.subtotal()));
        }

        sb.append(String.format("\nSubtotal: €%.2f\n", calculation.subtotal()));
        if (calculation.discount().signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", calculation.discount()));
            sb.append(String.format("After discount: €%.2f\n", calculation.discountedTotal()));
        }
        sb.append(String.format("VAT (21%%): €%.2f\n", calculation.vat()));
        sb.append(String.format("\nTOTAL: €%.2f\n", calculation.total()));

        return sb.toString();
    }
}
