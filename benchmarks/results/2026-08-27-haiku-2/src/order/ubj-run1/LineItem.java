import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final String description;
    private final BigDecimal pricePerUnit;
    private final int quantity;

    public LineItem(String description, BigDecimal pricePerUnit, int quantity) {
        validate(description, pricePerUnit, quantity);
        this.description = description;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    private void validate(String description, BigDecimal pricePerUnit, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    private final List<LineItem> lineItems = new ArrayList<>();

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public String generateReceipt() {
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot generate receipt for empty order");
        }

        BigDecimal preTaxTotal = calculatePreTaxTotal();
        BigDecimal discount = calculateDiscount(preTaxTotal);
        BigDecimal discountedTotal = preTaxTotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedTotal);
        BigDecimal total = discountedTotal.add(vat);

        return formatReceipt(preTaxTotal, discount, vat, total);
    }

    private BigDecimal calculatePreTaxTotal() {
        return lineItems.stream()
            .map(LineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal preTaxTotal) {
        if (preTaxTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return preTaxTotal.multiply(DISCOUNT_RATE);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private String formatReceipt(BigDecimal preTaxTotal, BigDecimal discount, 
                                 BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        receipt.append(String.format("Pre-tax total:  €%.2f\n", preTaxTotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%): -€%.2f\n", discount));
        }

        receipt.append(String.format("VAT (21%%):     €%.2f\n", vat));
        receipt.append("-------------------\n");
        receipt.append(String.format("Total:          €%.2f\n", total));

        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            receipt.append(String.format("%s x%d: €%.2f\n", 
                item.getDescription(), 
                item.getQuantity(), 
                item.getSubtotal()));
        }
    }
}
