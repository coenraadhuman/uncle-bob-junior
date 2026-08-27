import java.util.List;

/** Single entry point: validated items in, receipt string out. */
public final class OrderProcessor {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<LineItem> items) {
        OrderTotals totals = calculator.totalsFor(items);
        return formatter.format(items, totals);
    }
}
