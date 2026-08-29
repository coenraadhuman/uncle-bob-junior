import java.util.*;

public class WaitlistEntry {
    private final String entryId;
    private final String email;
    private final int seatCount;
    private final List<TicketType> ticketTypes;
    
    public WaitlistEntry(String entryId, String email, int seatCount, List<TicketType> ticketTypes) {
        this.entryId = entryId;
        this.email = email;
        this.seatCount = seatCount;
        this.ticketTypes = List.copyOf(ticketTypes);
    }
    
    public String getEntryId() {
        return entryId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public int getSeatCount() {
        return seatCount;
    }
    
    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }
}
