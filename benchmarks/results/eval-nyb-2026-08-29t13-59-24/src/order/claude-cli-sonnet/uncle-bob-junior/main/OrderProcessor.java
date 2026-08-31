// OrderProcessor.java
import java.util.List;

public final class OrderProcessor {

    private final LineItemValidator validator = new LineItemValidator();
    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<RawLineItem> rawItems) {
        List<LineItem> items = validator.validate(rawItems);
        OrderTotals totals = calculator.calculate(items);
        return formatter.format(items, totals);
    }
}
