import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

class SeatHold {
    private final String holdId;
    private final List<Seat> seats;
    private final LocalDateTime expiresAt;
    private final Map<TicketType, Integer> ticketCounts;
    private final double totalPrice;
    
    public SeatHold(String holdId, List<Seat> seats, Map<TicketType, Integer> ticketCounts, double totalPrice) {
        this.holdId = holdId;
        this.seats = seats;
        this.ticketCounts = new HashMap<>(ticketCounts);
        this.totalPrice = totalPrice;
        this.expiresAt = LocalDateTime.now().plus(15, ChronoUnit.MINUTES);
    }
    
    public String getHoldId() {
        return holdId;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public Map<TicketType, Integer> getTicketCounts() {
        return ticketCounts;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
}
