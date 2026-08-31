class Ticket {
    private final TicketTier tier;
    private final double price;
    
    Ticket(TicketTier tier, double price) {
        this.tier = tier;
        this.price = price;
    }
    
    TicketTier tier() {
        return tier;
    }
    
    double price() {
        return price;
    }
}
