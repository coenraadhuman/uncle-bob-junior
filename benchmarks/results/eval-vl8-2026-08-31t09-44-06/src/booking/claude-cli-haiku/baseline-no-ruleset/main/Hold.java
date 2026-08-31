import java.time.*;
import java.util.*;

class Hold {
    private final String holdId;
    private final String customerId;
    private final List<Seat> seats;
    private final LocalDateTime expiresAt;
    private final double totalPrice;
    private HoldStatus status;

    enum HoldStatus {
        ACTIVE, EXPIRED, CONFIRMED, RELEASED
    }

    public Hold(String holdId, String customerId, List<Seat> seats, LocalDateTime expiresAt, double totalPrice) {
        this.holdId = holdId;
        this.customerId = customerId;
        this.seats = new ArrayList<>(seats);
        this.expiresAt = expiresAt;
        this.totalPrice = totalPrice;
        this.status = HoldStatus.ACTIVE;
    }

    public String getHoldId() {
        return holdId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public HoldStatus getStatus() {
        return status;
    }

    public void setStatus(HoldStatus status) {
        this.status = status;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt) && status == HoldStatus.ACTIVE;
    }
}
