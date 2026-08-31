import java.util.*;

public class Hold {
    private final String holdId;
    private final List<String> seatIds;
    private final TicketTier ticketTier;
    private final long expiresAt;
    private boolean released;

    public Hold(String holdId, List<String> seatIds, TicketTier ticketTier) {
        this.holdId = holdId;
        this.seatIds = new ArrayList<>(seatIds);
        this.ticketTier = ticketTier;
        this.expiresAt = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
        this.released = false;
    }

    public String getHoldId() { return holdId; }
    public List<String> getSeatIds() { return new ArrayList<>(seatIds); }
    public TicketTier getTicketTier() { return ticketTier; }
    public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    public boolean isReleased() { return released; }
    public void release() { this.released = true; }
}
