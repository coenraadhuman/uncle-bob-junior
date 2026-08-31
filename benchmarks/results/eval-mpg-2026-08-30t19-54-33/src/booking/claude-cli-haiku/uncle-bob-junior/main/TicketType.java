// TicketType.java
public enum TicketType {
    ADULT(100_00),      // EUR in cents
    CHILD(50_00),
    SENIOR(70_00),
    STUDENT(60_00);

    private final long priceInCents;

    TicketType(long priceInCents) {
        this.priceInCents = priceInCents;
    }

    public long priceInCents() {
        return priceInCents;
    }
}
