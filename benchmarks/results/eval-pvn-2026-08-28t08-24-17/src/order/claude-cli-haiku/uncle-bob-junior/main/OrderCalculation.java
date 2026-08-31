import java.math.BigDecimal;

public final class OrderCalculation {
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal discountedTotal;
    private final BigDecimal vat;
    private final BigDecimal total;

    public OrderCalculation(BigDecimal subtotal, BigDecimal discount, BigDecimal discountedTotal,
                           BigDecimal vat, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.discountedTotal = discountedTotal;
        this.vat = vat;
        this.total = total;
    }

    BigDecimal subtotal() { return subtotal; }
    BigDecimal discount() { return discount; }
    BigDecimal discountedTotal() { return discountedTotal; }
    BigDecimal vat() { return vat; }
    BigDecimal total() { return total; }
}
