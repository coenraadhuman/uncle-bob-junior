public enum TicketTier {
    ADULT(50.00),
    CHILD(25.00),
    SENIOR(35.00),
    STUDENT(30.00);
    
    private final double price;
    
    TicketTier(double price) {
        this.price = price;
    }
    
    public double price() {
        return price;
    }
}
