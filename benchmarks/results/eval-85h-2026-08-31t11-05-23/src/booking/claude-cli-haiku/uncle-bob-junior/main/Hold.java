import java.time.LocalDateTime;
import java.util.*;

class Hold {
    private final String id;
    private final List<String> seatIds;
    private final List<TicketType> ticketTypes;
    private final LocalDateTime expiresAt;

    Hold(String id, List<String> seatIds, List<TicketType> ticketTypes, LocalDateTime expiresAt) {
        this.id = id;
        this.seatIds = new ArrayList<>(seatIds);
        this.ticketTypes = new ArrayList<>(ticketTypes);
        this.expiresAt = expiresAt;
    }

    String id() { return id; }
    List<String> seatIds() { return new ArrayList<>(seatIds); }
    List<TicketType> ticketTypes() { return new ArrayList<>(ticketTypes); }
    boolean isExpired(LocalDateTime now) { return now.isAfter(expiresAt); }
}
