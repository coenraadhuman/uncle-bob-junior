public class Seat {
    private final String id;
    private SeatStatus status;
    
    public enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }
    
    public Seat(String id) {
        this.id = id;
        this.status = SeatStatus.AVAILABLE;
    }
    
    public String getId() {
        return id;
    }
    
    public synchronized SeatStatus getStatus() {
        return status;
    }
    
    public synchronized void setStatus(SeatStatus status) {
        this.status = status;
    }
}
