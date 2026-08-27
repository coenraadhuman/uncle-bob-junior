// filename: OrderProcessor.java
import java.util.List;

class OrderProcessor {

    private final OrderCalculator calculator;
    private final ReceiptFormatter formatter;

    OrderProcessor() {
        this(new OrderCalculator(), new ReceiptFormatter());
    }

    OrderProcessor(OrderCalculator calculator, ReceiptFormatter formatter) {
        this.calculator = calculator;
        this.formatter = formatter;
    }

    String process(List<LineItem> lineItems) {
        OrderTotals totals = calculator.calculate(lineItems);
        return formatter.format(lineItems, totals);
    }
}
