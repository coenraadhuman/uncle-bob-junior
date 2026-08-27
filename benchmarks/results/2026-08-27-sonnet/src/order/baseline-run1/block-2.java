List<OrderProcessor.LineItem> items = List.of(
        new OrderProcessor.LineItem("Widget", 3, new BigDecimal("25.00")),
        new OrderProcessor.LineItem("Gadget", 1, new BigDecimal("40.00"))
);

OrderProcessor.OrderResult result = new OrderProcessor().processOrder(items);
System.out.print(result.receipt());
