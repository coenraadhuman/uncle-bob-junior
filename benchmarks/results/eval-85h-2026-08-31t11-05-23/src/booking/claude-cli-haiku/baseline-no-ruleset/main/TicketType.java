enum TicketType {
    ADULT(100.00),
    CHILD(50.00),
    SENIOR(75.00),
    STUDENT(60.00);
    
    private final double basePrice;
    
    TicketType(double basePrice) {
        this.basePrice = basePrice;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
}
