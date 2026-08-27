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
