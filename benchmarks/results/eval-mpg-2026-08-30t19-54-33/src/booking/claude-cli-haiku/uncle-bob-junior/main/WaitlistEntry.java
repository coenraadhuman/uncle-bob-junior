// WaitListEntry.java
public final class WaitListEntry {
    private final String customerId;
    private final int requestedSeats;
    private final java.util.List<TicketType> ticketTypes;
    private final long addedAtMillis;

    public WaitListEntry(String customerId, int requestedSeats,
                         java.util.List<TicketType> ticketTypes, long addedAtMillis) {
        this.customerId = customerId;
        this.requestedSeats = requestedSeats;
        this.ticketTypes = java.util.List.copyOf(ticketTypes);
        this.addedAtMillis = addedAtMillis;
    }

    public String customerId() {
        return customerId;
    }

    public int requestedSeats() {
        return requestedSeats;
    }

    public java.util.List<TicketType> ticketTypes() {
        return ticketTypes;
    }

    public long addedAtMillis() {
        return addedAtMillis;
    }
}
