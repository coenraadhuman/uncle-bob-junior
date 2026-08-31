public enum TicketType {
    ADULT(100),
    CHILD(50),
    SENIOR(75),
    STUDENT(60);

    private final int priceInCents;

    TicketType(int priceInCents) {
        this.priceInCents = priceInCents;
    }

    public int priceInCents() {
        return priceInCents;
    }
}
