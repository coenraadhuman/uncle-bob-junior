import java.time.*;
import java.util.*;

class SeatHold {
    private final String holdId;
    private final List<Seat> seats;
    private final Map<Seat, TicketTier> seatTiers;
    private final LocalDateTime expiresAt;
    
    SeatHold(String holdId, List<Seat> seats, Map<Seat, TicketTier> seatTiers, LocalDateTime expiresAt) {
        this.holdId = holdId;
        this.seats = new ArrayList<>(seats);
        this.seatTiers = new HashMap<>(seatTiers);
        this.expiresAt = expiresAt;
    }
    
    String getHoldId() { return holdId; }
    List<Seat> getSeats() { return seats; }
    Map<Seat, TicketTier> getSeatTiers() { return seatTiers; }
    LocalDateTime getExpiresAt() { return expiresAt; }
    boolean isExpired(LocalDateTime now) { return now.isAfter(expiresAt); }
}
