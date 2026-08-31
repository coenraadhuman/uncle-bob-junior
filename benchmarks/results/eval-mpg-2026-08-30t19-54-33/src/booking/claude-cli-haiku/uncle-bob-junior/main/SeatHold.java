// SeatHold.java
public final class SeatHold {
    private final String id;
    private final String eventId;
    private final java.util.List<String> seatIds;
    private final long expiresAtMillis;
    private final String customerId;

    public SeatHold(String id, String eventId, java.util.List<String> seatIds,
                    long expiresAtMillis, String customerId) {
        this.id = id;
        this.eventId = eventId;
        this.seatIds = java.util.List.copyOf(seatIds);
        this.expiresAtMillis = expiresAtMillis;
        this.customerId = customerId;
    }

    public String id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public java.util.List<String> seatIds() {
        return seatIds;
    }

    public String customerId() {
        return customerId;
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis > expiresAtMillis;
    }

    public int seatCount() {
        return seatIds.size();
    }
}
