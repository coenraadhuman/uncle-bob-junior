import java.time.*;
import java.util.*;

public class WaitlistEntry {
    private final String id;
    private final Map<TicketTier, Integer> quantities;
    private final LocalDateTime requestedAt;
    
    public WaitlistEntry(String id, Map<TicketTier, Integer> quantities, LocalDateTime requestedAt) {
        this.id = id;
        this.quantities = Map.copyOf(quantities);
        this.requestedAt = requestedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public Map<TicketTier, Integer> getQuantities() {
        return quantities;
    }
    
    public int getTotalRequested() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }
}
