import java.math.BigDecimal;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    String description() { return description; }
    int quantity() { return quantity; }
    BigDecimal unitPrice() { return unitPrice; }
    
    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
