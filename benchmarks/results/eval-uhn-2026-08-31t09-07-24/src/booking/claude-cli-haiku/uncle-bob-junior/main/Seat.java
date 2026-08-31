import java.time.LocalDateTime;

class Seat {
    enum State { AVAILABLE, HELD, BOOKED }
    
    private final int number;
    private State state;
    private String holdId;
    private String bookingId;
    private LocalDateTime holdExpiry;
    
    public Seat(int number) {
        this.number = number;
        this.state = State.AVAILABLE;
    }
    
    public int number() { return number; }
    public State state() { return state; }
    
    public void holdFor(String holdId, LocalDateTime expiryTime) {
        this.state = State.HELD;
        this.holdId = holdId;
        this.holdExpiry = expiryTime;
    }
    
    public void confirmHold(String bookingId) {
        this.state = State.BOOKED;
        this.bookingId = bookingId;
        this.holdId = null;
        this.holdExpiry = null;
    }
    
    public void release() {
        this.state = State.AVAILABLE;
        this.holdId = null;
        this.bookingId = null;
        this.holdExpiry = null;
    }
    
    public boolean isExpired(LocalDateTime now) {
        return state == State.HELD && holdExpiry.isBefore(now);
    }
}
