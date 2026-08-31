import java.time.LocalDateTime;

class Event {
    private final String id;
    private final LocalDateTime eventTime;
    private final int totalSeats;
    
    public Event(String id, LocalDateTime eventTime, int totalSeats) {
        this.id = id;
        this.eventTime = eventTime;
        this.totalSeats = totalSeats;
    }
    
    public String id() { return id; }
    public LocalDateTime eventTime() { return eventTime; }
    public int totalSeats() { return totalSeats; }
}
