import java.time.*;
import java.util.*;

class Hold {
    private static final long HOLD_DURATION_SECONDS = 15 * 60;
    
    private final String id;
    private final String customerId;
    private final List<Seat> seats;
    private final Instant createdAt;
    
    Hold(String id, String customerId, List<Seat> seats) {
        this.id = id;
        this.customerId = customerId;
        this.seats = List.copyOf(seats);
        this.createdAt = Instant.now();
    }
    
    boolean isExpired() {
        return Duration.between(createdAt, Instant.now()).toSeconds() > HOLD_DURATION_SECONDS;
    }
    
    String id() { return id; }
    String customerId() { return customerId; }
    List<Seat> seats() { return seats; }
}
