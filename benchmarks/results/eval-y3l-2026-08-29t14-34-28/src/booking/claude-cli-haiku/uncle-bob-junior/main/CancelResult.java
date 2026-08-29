import java.time.*;
import java.util.*;

class CancelResult {
    private final boolean success;
    private final Money refund;
    private final String reason;

    private CancelResult(boolean success, Money refund, String reason) {
        this.success = success;
        this.refund = refund;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    Money refund() {
        return refund;
    }

    String reason() {
        return reason;
    }

    static CancelResult refunded(Money amount) {
        return new CancelResult(true, amount, null);
    }

    static CancelResult notFound() {
        return new CancelResult(false, null, "Booking not found");
    }
}
