import java.time.*;
import java.util.*;

class HoldResult {
    private final boolean success;
    private final String holdId;
    private final LocalDateTime expiresAt;
    private final String reason;

    private HoldResult(boolean success, String holdId, LocalDateTime expiresAt, String reason) {
        this.success = success;
        this.holdId = holdId;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    String holdId() {
        return holdId;
    }

    LocalDateTime expiresAt() {
        return expiresAt;
    }

    String reason() {
        return reason;
    }

    static HoldResult held(String holdId, LocalDateTime expiresAt) {
        return new HoldResult(true, holdId, expiresAt, null);
    }

    static HoldResult unavailable() {
        return new HoldResult(false, null, null, "Seats unavailable");
    }

    static HoldResult soldOut() {
        return new HoldResult(false, null, null, "Event sold out, added to waitlist");
    }
}
