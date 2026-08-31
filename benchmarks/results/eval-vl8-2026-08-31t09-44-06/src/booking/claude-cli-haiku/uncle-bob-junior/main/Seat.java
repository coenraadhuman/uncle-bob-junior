class Seat {
    private final SeatLocation location;
    private final SeatType type;
    private SeatStatus status;
    private String currentHoldId;
    
    Seat(SeatLocation location, SeatType type) {
        this.location = location;
        this.type = type;
        this.status = SeatStatus.AVAILABLE;
    }
    
    synchronized void hold(String holdId) {
        if (status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat not available");
        }
        this.status = SeatStatus.HELD;
        this.currentHoldId = holdId;
    }
    
    synchronized void release() {
        this.status = SeatStatus.AVAILABLE;
        this.currentHoldId = null;
    }
    
    synchronized void book(String bookingId) {
        if (status != SeatStatus.HELD) {
            throw new IllegalStateException("Seat must be held before booking");
        }
        this.status = SeatStatus.BOOKED;
        this.currentHoldId = null;
    }
    
    SeatLocation location() { return location; }
    SeatType type() { return type; }
    synchronized SeatStatus status() { return status; }
}
