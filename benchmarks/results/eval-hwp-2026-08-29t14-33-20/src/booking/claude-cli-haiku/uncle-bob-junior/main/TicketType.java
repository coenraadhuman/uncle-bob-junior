public enum TicketType {
    ADULT(25.00),
    CHILD(12.50),
    SENIOR(15.00),
    STUDENT(18.00);
    
    private final double basePrice;
    
    TicketType(double basePrice) {
        this.basePrice = basePrice;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
}
