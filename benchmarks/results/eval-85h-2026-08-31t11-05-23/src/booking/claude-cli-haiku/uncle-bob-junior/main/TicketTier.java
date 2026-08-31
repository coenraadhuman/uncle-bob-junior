public enum TicketTier {
    ADULT(10_00),
    CHILD(6_00),
    SENIOR(8_00),
    STUDENT(7_00);
    
    private final int priceInCents;
    
    TicketTier(int priceInCents) {
        this.priceInCents = priceInCents;
    }
    
    public int getPriceInCents() {
        return priceInCents;
    }
}
