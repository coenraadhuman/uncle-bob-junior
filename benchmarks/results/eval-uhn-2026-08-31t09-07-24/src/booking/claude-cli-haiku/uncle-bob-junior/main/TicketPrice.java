class TicketPrice {
    final double adultPrice;
    final double childPrice;
    final double seniorPrice;
    final double studentPrice;
    
    TicketPrice(double adult, double child, double senior, double student) {
        this.adultPrice = adult;
        this.childPrice = child;
        this.seniorPrice = senior;
        this.studentPrice = student;
    }
    
    double priceFor(TicketTier tier) {
        return switch(tier) {
            case ADULT -> adultPrice;
            case CHILD -> childPrice;
            case SENIOR -> seniorPrice;
            case STUDENT -> studentPrice;
        };
    }
}
