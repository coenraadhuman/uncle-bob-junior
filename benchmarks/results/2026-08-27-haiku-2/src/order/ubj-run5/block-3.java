List<LineItem> items = List.of(
    new LineItem("Laptop", 1, 100000),  // €1000.00
    new LineItem("Mouse", 2, 2500)      // €25.00 × 2
);
Order order = new Order(items);
System.out.println(order.receipt());
