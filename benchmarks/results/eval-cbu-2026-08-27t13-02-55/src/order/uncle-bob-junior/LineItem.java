public class LineItem {
    private final String product;
    private final double pricePerUnit;
    private final int quantity;

    public LineItem(String product, double pricePerUnit, int quantity) {
        this.product = product;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    public void validate() throws IllegalArgumentException {
        if (product == null || product.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public double subtotal() {
        return pricePerUnit * quantity;
    }

    public String getProduct() {
        return product;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }
}

public class Order {
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double VAT_RATE = 0.21;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
    }

    public void validateItems() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            item.validate();
        }
    }

    public double computePreVatTotal() {
        return items.stream()
            .mapToDouble(LineItem::subtotal)
            .sum();
    }

    public double computeDiscount() {
        double preVatTotal = computePreVatTotal();
        return preVatTotal > DISCOUNT_THRESHOLD 
            ? preVatTotal * DISCOUNT_RATE 
            : 0.0;
    }

    public double computeSubtotalAfterDiscount() {
        return computePreVatTotal() - computeDiscount();
    }

    public double computeVat() {
        return computeSubtotalAfterDiscount() * VAT_RATE;
    }

    public double computeTotal() {
        return computeSubtotalAfterDiscount() + computeVat();
    }

    public String produceReceipt() {
        validateItems();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append(String.format("%-30s %10s %8s %12s\n", "Product", "Unit Price", "Qty", "Subtotal"));
        receipt.append("-".repeat(62)).append("\n");

        for (LineItem item : items) {
            receipt.append(String.format("%-30s €%9.2f %8d €%11.2f\n",
                truncate(item.getProduct(), 30),
                item.getPricePerUnit(),
                item.getQuantity(),
                item.subtotal()));
        }

        receipt.append("-".repeat(62)).append("\n");
        double preVatTotal = computePreVatTotal();
        double discount = computeDiscount();
        double subtotalAfterDiscount = computeSubtotalAfterDiscount();
        double vat = computeVat();
        double total = computeTotal();

        receipt.append(String.format("%-42s €%11.2f\n", "Subtotal", preVatTotal));
        
        if (discount > 0) {
            receipt.append(String.format("%-42s -€%10.2f\n", "Discount (10%)", discount));
            receipt.append(String.format("%-42s €%11.2f\n", "After Discount", subtotalAfterDiscount));
        }
        
        receipt.append(String.format("%-42s €%11.2f\n", "VAT (21%)", vat));
        receipt.append("=".repeat(62)).append("\n");
        receipt.append(String.format("%-42s €%11.2f\n", "TOTAL", total));

        return receipt.toString();
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
