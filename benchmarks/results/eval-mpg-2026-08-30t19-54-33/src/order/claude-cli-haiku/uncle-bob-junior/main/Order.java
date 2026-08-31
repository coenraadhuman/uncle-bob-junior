import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CURRENCY_SCALE = 2;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        appendItemsSection(receipt);
        appendTotalsSection(receipt);
        return receipt.toString();
    }

    private void appendItemsSection(StringBuilder receipt) {
        receipt.append("RECEIPT\n");
        receipt.append("-------\n");
        for (LineItem item : items) {
            receipt.append(formatLineItem(item)).append("\n");
        }
    }

    private String formatLineItem(LineItem item) {
        BigDecimal lineTotal = item.lineTotal();
        return String.format("%s x%d @ €%.2f = €%.2f",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal);
    }

    private void appendTotalsSection(StringBuilder receipt) {
        receipt.append("-------\n");
        BigDecimal subtotal = computeSubtotal();
        receipt.append(String.format("Subtotal: €%.2f\n", subtotal));

        if (shouldApplyDiscount(subtotal)) {
            BigDecimal discount = computeDiscount(subtotal);
            subtotal = subtotal.subtract(discount);
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
            receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotal));
        }

        BigDecimal vat = computeVat(subtotal);
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        BigDecimal total = subtotal.add(vat);
        receipt.append(String.format("TOTAL: €%.2f\n", total));
    }

    private BigDecimal computeSubtotal() {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean shouldApplyDiscount(BigDecimal subtotal) {
        return subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        return subtotal.multiply(DISCOUNT_RATE)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVat(BigDecimal taxableAmount) {
        return taxableAmount.multiply(VAT_RATE)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal getFinalTotal() {
        BigDecimal subtotal = computeSubtotal();
        if (shouldApplyDiscount(subtotal)) {
            subtotal = subtotal.subtract(computeDiscount(subtotal));
        }
        BigDecimal vat = computeVat(subtotal);
        return subtotal.add(vat).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }
}
