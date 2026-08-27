import java.util.ArrayList;
import java.util.List;

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private List<LineItem> items;

    Order() {
        this.items = new ArrayList<>();
    }

    void addItem(LineItem item) {
        validateLineItem(item);
        items.add(item);
    }

    private void validateLineItem(LineItem item) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (item.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    double calculatePreVatTotal() {
        return items.stream().mapToDouble(LineItem::getTotal).sum();
    }

    double calculateDiscount() {
        double preVatTotal = calculatePreVatTotal();
        return preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0;
    }

    double calculateSubtotalAfterDiscount() {
        return calculatePreVatTotal() - calculateDiscount();
    }

    double calculateVat() {
        return calculateSubtotalAfterDiscount() * VAT_RATE;
    }

    double calculateTotal() {
        return calculateSubtotalAfterDiscount() + calculateVat();
    }

    String generateReceipt() {
        double preVatTotal = calculatePreVatTotal();
        double discount = calculateDiscount();
        double subtotalAfterDiscount = calculateSubtotalAfterDiscount();
        double vat = calculateVat();
        double total = calculateTotal();

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%-20s %2d x €%6.2f = €%7.2f\n",
                item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }
        
        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                    €%7.2f\n", preVatTotal));
        
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):             -€%7.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount:     €%7.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):                   €%7.2f\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("Total:                       €%7.2f\n", total));
        
        return receipt.toString();
    }
}
