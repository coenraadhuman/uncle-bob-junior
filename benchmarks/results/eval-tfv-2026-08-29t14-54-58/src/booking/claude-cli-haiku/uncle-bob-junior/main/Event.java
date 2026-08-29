import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Event {
    private final String eventId;
    private final String name;
    private final LocalDateTime date;
    private final List<Seat> seats;
    private final ConcurrentHashMap<String, SeatHold> holds;
    private final List<Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    
    public Event(String eventId, String name, LocalDateTime date, List<Seat> seats) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.seats = new ArrayList<>(seats);
        this.holds = new ConcurrentHashMap<>();
        this.bookings = Collections.synchronizedList(new ArrayList<>());
        this.waitlist = new ConcurrentLinkedQueue<>();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public ConcurrentHashMap<String, SeatHold> getHolds() {
        return holds;
    }
    
    public List<Booking> getBookings() {
        return bookings;
    }
    
    public Queue<WaitlistEntry> getWaitlist() {
        return waitlist;
    }
    
    public int countAvailableSeats() {
        return (int) seats.stream()
            .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE)
            .count();
    }
}
