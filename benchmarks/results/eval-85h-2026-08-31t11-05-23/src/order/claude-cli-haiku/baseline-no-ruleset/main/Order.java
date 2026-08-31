import java.util.*;

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public Order(List<LineItem> lineItems) throws IllegalArgumentException {
        validateLineItems(lineItems);
        this.lineItems = lineItems;
    }

    private void validateLineItems(List<LineItem> items) throws IllegalArgumentException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }
    }

    public double getSubtotal() {
        return lineItems.stream().mapToDouble(LineItem::getLineTotal).sum();
    }

    public double getDiscount() {
        double subtotal = getSubtotal();
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0;
    }

    public double getSubtotalAfterDiscount() {
        return getSubtotal() - getDiscount();
    }

    public double getVat() {
        return getSubtotalAfterDiscount() * VAT_RATE;
    }

    public double getTotal() {
        return getSubtotalAfterDiscount() + getVat();
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("%-30s %8.2f EUR x %2d = %8.2f EUR\n",
                item.getDescription(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()));
        }

        receipt.append("\n" + "-".repeat(50) + "\n");
        receipt.append(String.format("Subtotal:                              %10.2f EUR\n", getSubtotal()));

        if (getDiscount() > 0) {
            receipt.append(String.format("Discount (10%% on total > 100):        -%9.2f EUR\n", getDiscount()));
        }

        receipt.append(String.format("Subtotal after discount:               %10.2f EUR\n", getSubtotalAfterDiscount()));
        receipt.append(String.format("VAT (21%%):                             %10.2f EUR\n", getVat()));
        receipt.append("-".repeat(50) + "\n");
        receipt.append(String.format("TOTAL:                                 %10.2f EUR\n", getTotal()));

        return receipt.toString();
    }
}
