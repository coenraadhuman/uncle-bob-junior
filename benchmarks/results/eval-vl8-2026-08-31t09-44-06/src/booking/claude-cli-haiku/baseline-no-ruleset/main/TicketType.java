enum TicketType {
    ADULT(15.0),
    CHILD(8.0),
    SENIOR(10.0),
    STUDENT(12.0);

    private final double basePrice;

    TicketType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
