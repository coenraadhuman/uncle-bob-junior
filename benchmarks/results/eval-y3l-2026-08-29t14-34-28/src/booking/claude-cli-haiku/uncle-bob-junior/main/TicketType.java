enum TicketType {
    ADULT(100.0),
    CHILD(50.0),
    SENIOR(75.0),
    STUDENT(60.0);

    private final double priceEuros;

    TicketType(double priceEuros) {
        this.priceEuros = priceEuros;
    }

    double getPrice() {
        return priceEuros;
    }
}
