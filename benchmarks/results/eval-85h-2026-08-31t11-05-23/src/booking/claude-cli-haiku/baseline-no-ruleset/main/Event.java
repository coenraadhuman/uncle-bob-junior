import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Event {
    private final String eventId;
    private final LocalDateTime eventDate;
    private final Map<String, Seat> seats;

    public Event(String eventId, LocalDateTime eventDate, int totalSeats) {
        this.eventId = eventId;
        this.eventDate = eventDate;
        this.seats = new ConcurrentHashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            this.seats.put("SEAT_" + i, new Seat("SEAT_" + i));
        }
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getEventDate() { return eventDate; }
    public Map<String, Seat> getSeats() { return seats; }
    public int getTotalSeats() { return seats.size(); }
}
