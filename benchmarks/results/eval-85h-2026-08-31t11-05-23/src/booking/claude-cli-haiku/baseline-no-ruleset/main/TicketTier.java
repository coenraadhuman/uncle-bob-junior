public enum TicketTier {
    ADULT(100.0),
    CHILD(50.0),
    SENIOR(75.0),
    STUDENT(60.0);

    private final double basePrice;

    TicketTier(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
