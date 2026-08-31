import java.math.BigDecimal;
import java.util.List;

public class Receipt {
    private final BigDecimal preTaxTotal;
    private final BigDecimal discountAmount;
    private final BigDecimal vatAmount;
    private final BigDecimal finalTotal;
    private final List<LineItem> lineItems;

    public Receipt(BigDecimal preTaxTotal, BigDecimal discountAmount,
                   BigDecimal vatAmount, BigDecimal finalTotal,
                   List<LineItem> lineItems) {
        this.preTaxTotal = preTaxTotal;
        this.discountAmount = discountAmount;
        this.vatAmount = vatAmount;
        this.finalTotal = finalTotal;
        this.lineItems = lineItems;
    }

    public BigDecimal getPreTaxTotal() {
        return preTaxTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    @Override
    public String toString() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("%s: €%.2f\n", item.getDescription(), item.getTotal()));
        }

        receipt.append(String.format("Subtotal: €%.2f\n", preTaxTotal));
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discountAmount));
        }
        receipt.append(String.format("VAT (21%%): €%.2f\n", vatAmount));
        receipt.append(String.format("Total: €%.2f\n", finalTotal));

        return receipt.toString();
    }
}
