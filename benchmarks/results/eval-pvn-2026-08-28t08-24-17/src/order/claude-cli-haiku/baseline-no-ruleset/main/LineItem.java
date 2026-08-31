import java.math.BigDecimal;

class LineItem {
    private String description;
    private BigDecimal unitPrice;
    private int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
