import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class Order {
    private List<LineItem> lineItems;
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public void validate() throws IllegalArgumentException {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        for (LineItem item : lineItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.getDescription());
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.getDescription());
            }
        }
    }

    private BigDecimal getSubtotal() {
        return lineItems.stream()
            .map(LineItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public String generateReceipt() {
        validate();

        BigDecimal subtotal = getSubtotal();
        BigDecimal discount = getDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = getVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== ORDER RECEIPT =====\n");
        receipt.append("\nLine Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %-30s %3d x €%8.2f = €%8.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotal()));
        }

        receipt.append("\n" + "-".repeat(50) + "\n");
        receipt.append(String.format("Subtotal:                           €%8.2f\n", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):                    -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:            €%8.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):                          €%8.2f\n", vat));
        receipt.append("-".repeat(50) + "\n");
        receipt.append(String.format("TOTAL:                              €%8.2f\n", total));

        return receipt.toString();
    }
}
