import java.time.*;
import java.util.*;

public class SeatHold {
    private static final int HOLD_DURATION_MINUTES = 15;
    
    private final String holdId;
    private final String email;
    private final List<Seat> seats;
    private final Instant expiresAt;
    
    public SeatHold(String holdId, String email, List<Seat> seats) {
        this.holdId = holdId;
        this.email = email;
        this.seats = List.copyOf(seats);
        this.expiresAt = Instant.now().plus(Duration.ofMinutes(HOLD_DURATION_MINUTES));
    }
    
    public String getHoldId() {
        return holdId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
