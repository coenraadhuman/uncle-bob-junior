class Snippet2 {
Order order = new Order(List.of(
    new LineItem("Laptop", 1, new BigDecimal("899.99")),
    new LineItem("Mouse", 2, new BigDecimal("25.50"))
));
System.out.println(order.receipt());

}
