import java.math.BigDecimal;

class LineItem {
    private String description;
    private BigDecimal price;
    
    public LineItem(String description, BigDecimal price) {
        this.description = description;
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
}
