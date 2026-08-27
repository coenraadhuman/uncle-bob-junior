import java.util.*;
import java.text.DecimalFormat;

class LineItem {
    private String productName;
    private int quantity;
    private double unitPrice;

    public LineItem(String productName, int quantity, double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
    }

    public void validateLineItems() {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (int i = 0; i < lineItems.size(); i++) {
            LineItem item = lineItems.get(i);
            if (item == null) {
                throw new IllegalArgumentException("Line item " + i + " is null");
            }
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Line item " + i + " has invalid product name");
            }
            if (item.getQuantity() < 0) {
                throw new IllegalArgumentException("Line item " + i + " has negative quantity");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Line item " + i + " has negative unit price");
            }
        }
    }

    public double getSubtotal() {
        return lineItems.stream()
                .mapToDouble(LineItem::getLineTotal)
                .sum();
    }

    public double getDiscount() {
        double subtotal = getSubtotal();
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }

    public double getDiscountedSubtotal() {
        return getSubtotal() - getDiscount();
    }

    public double getVat() {
        return getDiscountedSubtotal() * VAT_RATE;
    }

    public double getTotal() {
        return getDiscountedSubtotal() + getVat();
    }

    public String produceReceipt() {
        validateLineItems();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("═════════════════════════════════\n");
        receipt.append("               RECEIPT\n");
        receipt.append("═════════════════════════════════\n\n");
        
        receipt.append("Item                    Qty    Price\n");
        receipt.append("─────────────────────────────────\n");
        for (LineItem item : lineItems) {
            String name = item.getProductName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            receipt.append(String.format("%-20s %3d  %s\n", 
                    name, 
                    item.getQuantity(), 
                    currencyFormat.format(item.getLineTotal())));
        }
        
        receipt.append("─────────────────────────────────\n");
        receipt.append(String.format("Subtotal:                    %s\n", 
                currencyFormat.format(getSubtotal())));
        
        double discount = getDiscount();
        if (discount > 0) {
            receipt.append(String.format("Discount (10%):             -%s\n", 
                    currencyFormat.format(discount)));
        }
        
        receipt.append(String.format("Subtotal after discount:     %s\n", 
                currencyFormat.format(getDiscountedSubtotal())));
        receipt.append(String.format("VAT (21%):                   %s\n", 
                currencyFormat.format(getVat())));
        
        receipt.append("═════════════════════════════════\n");
        receipt.append(String.format("TOTAL:                       %s\n", 
                currencyFormat.format(getTotal())));
        receipt.append("═════════════════════════════════\n");
        
        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = Arrays.asList(
                new LineItem("Laptop", 1, 80.00),
                new LineItem("Mouse", 2, 15.00),
                new LineItem("Keyboard", 1, 45.00)
        );
        
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        
        // Example with discount threshold exceeded
        System.out.println("\n\nSecond order (with discount):\n");
        List<LineItem> items2 = Arrays.asList(
                new LineItem("Monitor", 1, 300.00),
                new LineItem("HDMI Cable", 2, 10.00)
        );
        
        Order order2 = new Order(items2);
        System.out.println(order2.produceReceipt());
    }
}
