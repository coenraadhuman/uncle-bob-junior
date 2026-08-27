import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    public LineItem(String productName, int quantity, BigDecimal unitPrice) {
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    private List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = items;
    }

    public void validate() throws IllegalArgumentException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                    "Quantity must be positive for: " + item.getProductName());
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                    "Price cannot be negative for: " + item.getProductName());
            }
        }
    }

    public String generateReceipt() {
        validate();

        BigDecimal preVatTotal = items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? preVatTotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal subtotalAfterDiscount = preVatTotal.subtract(discount)
            .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal vat = subtotalAfterDiscount.multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal total = subtotalAfterDiscount.add(vat)
            .setScale(SCALE, RoundingMode.HALF_UP);

        StringBuilder receipt = new StringBuilder();
        receipt.append("════════════════ RECEIPT ════════════════\n");
        receipt.append("Items:\n");

        for (LineItem item : items) {
            BigDecimal lineTotal = item.getLineTotal().setScale(SCALE, RoundingMode.HALF_UP);
            receipt.append(String.format("  %-30s %3d × €%7.2f = €%8.2f\n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal));
        }

        receipt.append("\n");
        receipt.append(String.format("Subtotal:                        €%8.2f\n", preVatTotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%% over €100):      -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:         €%8.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):                       €%8.2f\n", vat));
        receipt.append("────────────────────────────────────────\n");
        receipt.append(String.format("TOTAL:                           €%8.2f\n", total));
        receipt.append("═════════════════════════════════════════\n");

        return receipt.toString();
    }
}

public class Main {
    public static void main(String[] args) {
        // Example: Order with 2 items totaling €110 (qualifies for discount)
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, new BigDecimal("80.00")),
            new LineItem("Mouse", 2, new BigDecimal("15.00"))
        );

        Order order = new Order(items);
        System.out.println(order.generateReceipt());

        // Example: Order under threshold (no discount)
        System.out.println("\n");
        List<LineItem> smallOrder = List.of(
            new LineItem("Pencil", 5, new BigDecimal("2.50")),
            new LineItem("Notebook", 2, new BigDecimal("12.00"))
        );

        Order order2 = new Order(smallOrder);
        System.out.println(order2.generateReceipt());
    }
}
