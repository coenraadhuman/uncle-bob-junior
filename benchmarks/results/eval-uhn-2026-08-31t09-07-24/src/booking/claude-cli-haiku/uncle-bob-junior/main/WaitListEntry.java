import java.time.LocalDateTime;
import java.util.*;

class WaitlistEntry {
    private final String id;
    private final int seatsRequested;
    private final List<TicketTier> tiers;
    private final LocalDateTime requestedAt;
    
    public WaitlistEntry(String id, int seatsRequested, List<TicketTier> tiers, LocalDateTime requestedAt) {
        this.id = id;
        this.seatsRequested = seatsRequested;
        this.tiers = tiers;
        this.requestedAt = requestedAt;
    }
    
    public String id() { return id; }
    public int seatsRequested() { return seatsRequested; }
    public List<TicketTier> tiers() { return tiers; }
    public LocalDateTime requestedAt() { return requestedAt; }
}
