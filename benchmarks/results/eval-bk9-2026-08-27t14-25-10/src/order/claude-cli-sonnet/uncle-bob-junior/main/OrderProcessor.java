public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        OrderTotals totals = OrderCalculator.computeTotals(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
