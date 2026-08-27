// OrderReceipt.java
import java.math.BigDecimal;
import java.util.List;

public final class OrderReceipt {

    private final List<LineItem> lineItems;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;

    public OrderReceipt(List<LineItem> lineItems, BigDecimal subtotal, BigDecimal discount,
                         BigDecimal vat, BigDecimal total) {
        this.lineItems = List.copyOf(lineItems);
        this.subtotal = subtotal;
        this.discount = discount;
        this.vat = vat;
        this.total = total;
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal discount() {
        return discount;
    }

    public BigDecimal vat() {
        return vat;
    }

    public BigDecimal total() {
        return total;
    }
}
