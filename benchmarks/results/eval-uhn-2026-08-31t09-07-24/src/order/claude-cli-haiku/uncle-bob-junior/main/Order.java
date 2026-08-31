import java.util.*;

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = new ArrayList<>(items);
    }

    public double preVatTotal() {
        return items.stream().mapToDouble(LineItem::subtotal).sum();
    }

    public boolean qualifiesForDiscount() {
        return preVatTotal() > DISCOUNT_THRESHOLD;
    }

    public double discountAmount() {
        return qualifiesForDiscount() ? preVatTotal() * DISCOUNT_RATE : 0.0;
    }

    public double subtotalAfterDiscount() {
        return preVatTotal() - discountAmount();
    }

    public double vatAmount() {
        return subtotalAfterDiscount() * VAT_RATE;
    }

    public double total() {
        return subtotalAfterDiscount() + vatAmount();
    }

    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== RECEIPT ==========\n");
        for (LineItem item : items) {
            sb.append(String.format("%-20s %2d x €%7.2f = €%8.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.subtotal()));
        }
        sb.append("-----------------------------\n");
        sb.append(String.format("Subtotal:                 €%8.2f\n", preVatTotal()));
        if (qualifiesForDiscount()) {
            sb.append(String.format("Discount (10%%):          -€%8.2f\n", discountAmount()));
        }
        sb.append(String.format("VAT (21%%):                €%8.2f\n", vatAmount()));
        sb.append("=============================\n");
        sb.append(String.format("TOTAL:                    €%8.2f\n", total()));
        return sb.toString();
    }
}
