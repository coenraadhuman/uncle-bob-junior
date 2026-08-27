public class LineItem {
    private final String description;
    private final int quantityInCents;
    private final int pricePerUnitInCents;

    public LineItem(String description, int quantityInCents, int pricePerUnitInCents) {
        if (quantityInCents <= 0 || pricePerUnitInCents <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = description;
        this.quantityInCents = quantityInCents;
        this.pricePerUnitInCents = pricePerUnitInCents;
    }

    public int subtotalInCents() {
        return quantityInCents * pricePerUnitInCents;
    }

    public String description() {
        return description;
    }
}

public class Order {
    private static final int VAT_PERCENTAGE = 21;
    private static final int DISCOUNT_PERCENTAGE = 10;
    private static final int DISCOUNT_THRESHOLD_IN_CENTS = 10000; // €100.00
    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = new ArrayList<>(items);
    }

    public int subtotalInCents() {
        return items.stream().mapToInt(LineItem::subtotalInCents).sum();
    }

    public int discountInCents() {
        int subtotal = subtotalInCents();
        if (subtotal > DISCOUNT_THRESHOLD_IN_CENTS) {
            return subtotal * DISCOUNT_PERCENTAGE / 100;
        }
        return 0;
    }

    public int subtotalAfterDiscountInCents() {
        return subtotalInCents() - discountInCents();
    }

    public int vatInCents() {
        return subtotalAfterDiscountInCents() * VAT_PERCENTAGE / 100;
    }

    public int totalInCents() {
        return subtotalAfterDiscountInCents() + vatInCents();
    }

    public String receipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            receipt.append(formatLineItem(item)).append("\n");
        }
        
        receipt.append("--------------\n");
        receipt.append(formatAmount("Subtotal", subtotalInCents())).append("\n");
        
        if (discountInCents() > 0) {
            receipt.append(formatAmount("Discount (10%)", -discountInCents())).append("\n");
        }
        
        receipt.append(formatAmount("VAT (21%)", vatInCents())).append("\n");
        receipt.append("--------------\n");
        receipt.append(formatAmount("TOTAL", totalInCents())).append("\n");
        
        return receipt.toString();
    }

    private String formatLineItem(LineItem item) {
        return String.format("%s: €%.2f", item.description(), item.subtotalInCents() / 100.0);
    }

    private String formatAmount(String label, int amountInCents) {
        return String.format("%-20s €%.2f", label, amountInCents / 100.0);
    }
}
