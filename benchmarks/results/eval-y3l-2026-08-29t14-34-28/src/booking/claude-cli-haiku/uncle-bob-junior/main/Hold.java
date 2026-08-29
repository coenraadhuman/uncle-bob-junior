import java.time.*;
import java.util.*;

class Hold {
    private final String id;
    private final Seat seat;
    private final LocalDateTime expiresAt;

    Hold(String id, Seat seat, LocalDateTime expiresAt) {
        this.id = id;
        this.seat = seat;
        this.expiresAt = expiresAt;
    }

    String id() {
        return id;
    }

    Seat seat() {
        return seat;
    }

    boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }
}
