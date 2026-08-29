public enum TicketType {
    ADULT(50.00),
    CHILD(25.00),
    SENIOR(35.00),
    STUDENT(30.00);
    
    private final double priceEur;
    
    TicketType(double priceEur) {
        this.priceEur = priceEur;
    }
    
    public double getPriceEur() {
        return priceEur;
    }
}
