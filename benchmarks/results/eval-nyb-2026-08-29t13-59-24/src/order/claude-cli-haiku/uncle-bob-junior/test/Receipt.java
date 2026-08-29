import java.math.BigDecimal;
import java.math.RoundingMode;

class Receipt {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;
    
    Receipt(BigDecimal subtotal) {
        this.subtotal = subtotal;
        this.discount = computeDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        this.vat = computeVat(discountedSubtotal);
        this.total = discountedSubtotal.add(vat);
    }
    
    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal computeVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    String format() {
        return String.format(
            "Subtotal: €%.2f\nDiscount: -€%.2f\nVAT (21%%): €%.2f\nTotal: €%.2f",
            subtotal, discount, vat, total
        );
    }
    
    BigDecimal getTotal() {
        return total;
    }
    
    BigDecimal getSubtotal() {
        return subtotal;
    }
    
    BigDecimal getDiscount() {
        return discount;
    }
    
    BigDecimal getVat() {
        return vat;
    }
}
