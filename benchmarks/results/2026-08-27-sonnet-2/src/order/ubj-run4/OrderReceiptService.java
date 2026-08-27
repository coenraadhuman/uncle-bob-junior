// File: OrderReceiptService.java
import java.util.List;

public final class OrderReceiptService {

    private final OrderProcessor orderProcessor;
    private final ReceiptFormatter receiptFormatter;

    public OrderReceiptService(OrderProcessor orderProcessor, ReceiptFormatter receiptFormatter) {
        this.orderProcessor = orderProcessor;
        this.receiptFormatter = receiptFormatter;
    }

    public String generateReceipt(List<LineItem> lineItems) {
        OrderTotals totals = orderProcessor.calculateTotals(lineItems);
        return receiptFormatter.format(lineItems, totals);
    }
}
