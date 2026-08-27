class LineItem {
    private final String name;
    private final double price;
    private final int quantity;
    
    LineItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        validate();
    }
    
    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    double subtotal() {
        return price * quantity;
    }
    
    String name() { return name; }
    double price() { return price; }
    int quantity() { return quantity; }
}

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        validateItems(items);
        this.items = new ArrayList<>(items);
    }
    
    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Line items cannot be null");
            }
        }
    }
    
    private double subtotal() {
        return items.stream().mapToDouble(LineItem::subtotal).sum();
    }
    
    private double discountAmount() {
        if (subtotal() > DISCOUNT_THRESHOLD) {
            return subtotal() * DISCOUNT_RATE;
        }
        return 0.0;
    }
    
    private double subtotalAfterDiscount() {
        return subtotal() - discountAmount();
    }
    
    private double vatAmount() {
        return subtotalAfterDiscount() * VAT_RATE;
    }
    
    double total() {
        return subtotalAfterDiscount() + vatAmount();
    }
    
    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.name(), item.quantity(), item.price(), item.subtotal()));
        }
        
        sb.append("-".repeat(30)).append("\n");
        sb.append(String.format("Subtotal:              €%.2f\n", subtotal()));
        
        if (discountAmount() > 0) {
            sb.append(String.format("Discount (10%%):        -€%.2f\n", discountAmount()));
        }
        
        sb.append(String.format("Subtotal (discount):   €%.2f\n", subtotalAfterDiscount()));
        sb.append(String.format("VAT (21%%):             €%.2f\n", vatAmount()));
        sb.append("-".repeat(30)).append("\n");
        sb.append(String.format("Total:                 €%.2f\n", total()));
        
        return sb.toString();
    }
}

// Example usage and test
class OrderProcessor {
    public static void main(String[] args) {
        // Order under 100 euros (no discount)
        List<LineItem> items1 = List.of(
            new LineItem("Coffee", 2.50, 2),
            new LineItem("Croissant", 3.00, 3)
        );
        Order order1 = new Order(items1);
        System.out.println(order1.receipt());
        
        // Order over 100 euros (applies 10% discount)
        List<LineItem> items2 = List.of(
            new LineItem("Monitor", 250.00, 1),
            new LineItem("Keyboard", 75.00, 2),
            new LineItem("Mouse", 25.00, 1)
        );
        Order order2 = new Order(items2);
        System.out.println(order2.receipt());
    }
}
