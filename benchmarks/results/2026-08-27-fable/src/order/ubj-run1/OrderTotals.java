import java.math.BigDecimal;

/** All amounts in euros, rounded to cents. Discount and VAT are already applied. */
public record OrderTotals(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal netAmount,
        BigDecimal vat,
        BigDecimal grandTotal) {

    public boolean hasDiscount() {
        return discount.signum() > 0;
    }
}
