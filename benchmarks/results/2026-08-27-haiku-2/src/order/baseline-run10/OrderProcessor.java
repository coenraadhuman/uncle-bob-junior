import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private List<LineItem> items;

    public OrderProcessor(List<LineItem> items) {
        this.items = items;
    }

    public void validateLineItems() {
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                    "Invalid quantity for " + item.getProductName() + ": must be positive"
                );
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException(
                    "Invalid price for " + item.getProductName() + ": cannot be negative"
                );
            }
        }
    }

    public String generateReceipt() {
        validateLineItems();

        double subtotal = items.stream()
            .mapToDouble(LineItem::getTotal)
            .sum();

        double discount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");

        for (LineItem item : items) {
            receipt.append(String.format("%-25s %3d x €%7.2f = €%8.2f\n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotal()
            ));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                    €%8.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):             -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:     €%8.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%%):                  €%8.2f\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("TOTAL:                       €%8.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}
