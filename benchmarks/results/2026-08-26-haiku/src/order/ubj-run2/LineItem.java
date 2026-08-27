public class LineItem {
    private final String name;
    private final int quantity;
    private final double unitPrice;
    
    public LineItem(String name, int quantity, double unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for " + name);
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative for " + name);
        }
    }
    
    public double getLineTotal() {
        return quantity * unitPrice;
    }
    
    public String getName() {
        return name;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
}
