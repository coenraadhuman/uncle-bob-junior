// Booking.java
public final class Booking {
    private final String id;
    private final String eventId;
    private final java.util.List<String> seatIds;
    private final java.util.List<TicketType> ticketTypes;
    private final Money totalPrice;
    private final String customerId;
    private final long createdAtMillis;

    public Booking(String id, String eventId, java.util.List<String> seatIds,
                   java.util.List<TicketType> ticketTypes, Money totalPrice,
                   String customerId, long createdAtMillis) {
        this.id = id;
        this.eventId = eventId;
        this.seatIds = java.util.List.copyOf(seatIds);
        this.ticketTypes = java.util.List.copyOf(ticketTypes);
        this.totalPrice = totalPrice;
        this.customerId = customerId;
        this.createdAtMillis = createdAtMillis;
    }

    public String id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public java.util.List<String> seatIds() {
        return seatIds;
    }

    public java.util.List<TicketType> ticketTypes() {
        return ticketTypes;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public String customerId() {
        return customerId;
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }

    public int seatCount() {
        return seatIds.size();
    }
}
