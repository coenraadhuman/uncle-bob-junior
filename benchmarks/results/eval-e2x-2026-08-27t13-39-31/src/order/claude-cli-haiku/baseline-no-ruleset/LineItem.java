import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private String product;
    private int quantity;
    private double unitPrice;
    
    public LineItem(String product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public boolean isValid() {
        return quantity > 0 && unitPrice >= 0;
    }
    
    public double getTotal() {
        return quantity * unitPrice;
    }
    
    public String getProduct() {
        return product;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
}
