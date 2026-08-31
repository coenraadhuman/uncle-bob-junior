import java.time.LocalDateTime;
import java.util.*;

class WaitlistEntry {
    private final String entryId;
    private final Map<TicketType, Integer> ticketCounts;
    private final LocalDateTime requestTime;
    
    public WaitlistEntry(String entryId, Map<TicketType, Integer> ticketCounts) {
        this.entryId = entryId;
        this.ticketCounts = new HashMap<>(ticketCounts);
        this.requestTime = LocalDateTime.now();
    }
    
    public String getEntryId() {
        return entryId;
    }
    
    public Map<TicketType, Integer> getTicketCounts() {
        return ticketCounts;
    }
    
    public int getTotalSeatsRequested() {
        return ticketCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
}
