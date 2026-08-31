import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class Event {
    private final String id;
    private final LocalDateTime date;
    private final Map<Integer, Seat> seats;
    
    public Event(String id, LocalDateTime date, int capacity) {
        this.id = id;
        this.date = date;
        this.seats = new LinkedHashMap<>();
        for (int i = 1; i <= capacity; i++) {
            seats.put(i, new Seat(i));
        }
    }
    
    public String id() { return id; }
    public LocalDateTime date() { return date; }
    public int capacity() { return seats.size(); }
    
    public Seat seat(int seatId) {
        return seats.get(seatId);
    }
    
    public List<Integer> availableSeatIds() {
        return seats.values().stream()
            .filter(s -> s.status() == Seat.Status.AVAILABLE)
            .map(Seat::id)
            .collect(Collectors.toList());
    }
}
