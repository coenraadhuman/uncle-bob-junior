import java.time.*;

public class WaitingListEntry {
    private final String customerId;
    private final int seatCount;
    private final TicketType[] ticketTypes;
    private final LocalDateTime requestedAt;

    public WaitingListEntry(String customerId, int seatCount, TicketType[] ticketTypes, LocalDateTime requestedAt) {
        this.customerId = customerId;
        this.seatCount = seatCount;
        this.ticketTypes = ticketTypes.clone();
        this.requestedAt = requestedAt;
    }

    public String customerId() { return customerId; }
    public int seatCount() { return seatCount; }
    public TicketType[] ticketTypes() { return ticketTypes; }
    public LocalDateTime requestedAt() { return requestedAt; }
}
