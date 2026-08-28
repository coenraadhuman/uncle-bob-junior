public class LineItem {
    private String description;
    private int quantity;
    private double price;
    
    public LineItem(String description, int quantity, double price) {
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
