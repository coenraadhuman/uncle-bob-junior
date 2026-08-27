public class LineItem {
    private final String productName;
    private final double pricePerUnit;
    private final int quantity;

    public LineItem(String productName, double pricePerUnit, int quantity) {
        validateInputs(productName, pricePerUnit, quantity);
        this.productName = productName;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    private void validateInputs(String productName, double pricePerUnit, int quantity) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("Price per unit cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public double getLineTotal() {
        return pricePerUnit * quantity;
    }

    public String getProductName() {
        return productName;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }
}
