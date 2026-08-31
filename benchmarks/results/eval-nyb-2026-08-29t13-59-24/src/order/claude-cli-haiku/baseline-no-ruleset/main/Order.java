class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final java.util.List<LineItem> items = new java.util.ArrayList<>();

    public void addItem(LineItem item) {
        items.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.getDescription());
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.getDescription());
            }
        }
    }

    public String generateReceipt() {
        validate();

        double subtotal = items.stream().mapToDouble(LineItem::getLineTotal).sum();
        double discountAmount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        double subtotalAfterDiscount = subtotal - discountAmount;
        double vat = subtotalAfterDiscount * VAT_RATE;
        double total = subtotalAfterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-25s %5d x EUR %7.2f = EUR %8.2f\n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }
        receipt.append("-----------------------------\n");
        receipt.append(String.format("%-40s EUR %8.2f\n", "Subtotal:", subtotal));
        if (discountAmount > 0) {
            receipt.append(String.format("%-40s EUR %8.2f\n", "Discount (10%):", discountAmount));
        }
        receipt.append(String.format("%-40s EUR %8.2f\n", "Subtotal after discount:", subtotalAfterDiscount));
        receipt.append(String.format("%-40s EUR %8.2f\n", "VAT (21%):", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("%-40s EUR %8.2f\n", "TOTAL:", total));

        return receipt.toString();
    }
}
