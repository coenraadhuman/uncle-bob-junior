import java.time.LocalDateTime;

public class WaitlistEntry {
    private final String waitlistId;
    private final int seatsRequested;
    private final TicketTier ticketTier;
    private final LocalDateTime joinedAt;

    public WaitlistEntry(String waitlistId, int seatsRequested, TicketTier ticketTier) {
        this.waitlistId = waitlistId;
        this.seatsRequested = seatsRequested;
        this.ticketTier = ticketTier;
        this.joinedAt = LocalDateTime.now();
    }

    public String getWaitlistId() { return waitlistId; }
    public int getSeatsRequested() { return seatsRequested; }
    public TicketTier getTicketTier() { return ticketTier; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
