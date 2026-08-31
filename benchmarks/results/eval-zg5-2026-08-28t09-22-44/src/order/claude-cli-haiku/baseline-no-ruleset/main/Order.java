import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    
    public Order(List<LineItem> lineItems) {
        this.lineItems = validateLineItems(lineItems);
    }
    
    private List<LineItem> validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Line items cannot be null or empty");
        }
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.getDescription());
            }
            if (item.getPrice() < 0) {
                throw new IllegalArgumentException("Price cannot be negative: " + item.getDescription());
            }
        }
        return items;
    }
    
    public double getPreVatTotal() {
        return lineItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getPrice())
            .sum();
    }
    
    public double getDiscount() {
        return getPreVatTotal() > DISCOUNT_THRESHOLD ? getPreVatTotal() * DISCOUNT_RATE : 0.0;
    }
    
    public double getVat() {
        return (getPreVatTotal() - getDiscount()) * VAT_RATE;
    }
    
    public double getTotal() {
        return getPreVatTotal() - getDiscount() + getVat();
    }
    
    public String getReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append(String.format("%-30s %8s %12s\n", "Description", "Qty", "Price"));
        receipt.append("-".repeat(52)).append("\n");
        
        for (LineItem item : lineItems) {
            double lineTotal = item.getQuantity() * item.getPrice();
            receipt.append(String.format("%-30s %8d €%11.2f\n", 
                item.getDescription(), item.getQuantity(), lineTotal));
        }
        
        receipt.append("-".repeat(52)).append("\n");
        receipt.append(String.format("Pre-VAT Total:        %31.2f EUR\n", getPreVatTotal()));
        
        double discount = getDiscount();
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):      -%30.2f EUR\n", discount));
        }
        
        receipt.append(String.format("VAT (21%%):            %31.2f EUR\n", getVat()));
        receipt.append("=".repeat(52)).append("\n");
        receipt.append(String.format("TOTAL:                %31.2f EUR\n", getTotal()));
        
        return receipt.toString();
    }
    
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget A", 5, 15.00));
        items.add(new LineItem("Widget B", 3, 20.00));
        items.add(new LineItem("Service Fee", 1, 25.50));
        
        Order order = new Order(items);
        System.out.println(order.getReceipt());
        System.out.println("Pre-VAT: " + order.getPreVatTotal());
        System.out.println("Discount: " + order.getDiscount());
        System.out.println("Final Total: " + order.getTotal());
    }
}
