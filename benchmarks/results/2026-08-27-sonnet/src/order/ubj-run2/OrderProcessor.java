// OrderProcessor.java
import java.util.List;

public final class OrderProcessor {

    public String processOrder(List<OrderItem> items) {
        OrderItemValidator.validate(items);
        OrderPricing pricing = OrderPricing.of(items);
        return ReceiptFormatter.format(items, pricing);
    }
}
