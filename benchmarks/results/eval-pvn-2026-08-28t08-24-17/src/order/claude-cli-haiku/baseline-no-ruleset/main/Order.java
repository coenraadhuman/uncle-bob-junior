import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    private List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        this.lineItems = validateLineItems(lineItems);
    }

    private List<LineItem> validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item.getDescription() == null || item.getDescription().isEmpty()) {
                throw new IllegalArgumentException("Line item description cannot be empty");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative");
            }
            if (item.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
        }
        return items;
    }

    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = subtotalAfterDiscount.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAfterDiscount.add(vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("================= RECEIPT =================\n");
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-30s %5d x €%8.2f = €%8.2f\n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("Subtotal:                             €%8.2f\n", subtotal));
        if (discount.signum() > 0) {
            receipt.append(String.format("Discount (10%%):                      -€%8.2f\n", discount));
            receipt.append(String.format("Subtotal after discount:             €%8.2f\n", subtotalAfterDiscount));
        }
        receipt.append(String.format("VAT (21%%):                            €%8.2f\n", vat));
        receipt.append("-----------------------------------------\n");
        receipt.append(String.format("TOTAL:                                €%8.2f\n", total));
        receipt.append("==========================================\n");
        return receipt.toString();
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
                .map(LineItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
