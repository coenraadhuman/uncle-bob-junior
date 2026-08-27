import java.math.BigDecimal;

public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vatAmount, BigDecimal total) {

    public String toReceiptString() {
        return """
                Subtotal:  %s
                Discount:  %s
                VAT (21%%): %s
                Total:     %s
                """.formatted(
                Money.format(subtotal),
                Money.format(discount),
                Money.format(vatAmount),
                Money.format(total));
    }
}
