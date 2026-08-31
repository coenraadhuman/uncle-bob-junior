import java.time.*;
import java.util.*;

class WaitListEntry {
    private final String waitListId;
    private final String customerId;
    private final List<TicketType> ticketTypes;
    private final LocalDateTime requestedAt;

    public WaitListEntry(String waitListId, String customerId, List<TicketType> ticketTypes, LocalDateTime requestedAt) {
        this.waitListId = waitListId;
        this.customerId = customerId;
        this.ticketTypes = new ArrayList<>(ticketTypes);
        this.requestedAt = requestedAt;
    }

    public String getWaitListId() {
        return waitListId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<TicketType> getTicketTypes() {
        return new ArrayList<>(ticketTypes);
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
