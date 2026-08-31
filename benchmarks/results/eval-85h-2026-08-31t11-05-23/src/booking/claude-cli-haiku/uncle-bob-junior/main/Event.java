import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Event {
    private final String id;
    private final int capacity;
    private final LocalDateTime eventDate;
    private final Set<Seat> available;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    
    public Event(String id, int capacity, LocalDateTime eventDate) {
        this.id = id;
        this.capacity = capacity;
        this.eventDate = eventDate;
        this.available = Collections.synchronizedSet(new HashSet<>());
        this.bookings = new ConcurrentHashMap<>();
        this.waitlist = new ConcurrentLinkedQueue<>();
        
        for (int i = 0; i < capacity; i++) {
            available.add(new Seat("SEAT_" + i));
        }
    }
    
    public int availableCount() {
        return available.size();
    }
    
    public List<Seat> allocate(int count) {
        if (available.size() < count) return List.of();
        
        List<Seat> allocated = available.stream()
            .limit(count)
            .toList();
        allocated.forEach(available::remove);
        return allocated;
    }
    
    public void release(List<Seat> seats) {
        available.addAll(seats);
    }
    
    public void addBooking(Booking booking) {
        bookings.put(booking.getId(), booking);
    }
    
    public Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }
    
    public void addToWaitlist(WaitlistEntry entry) {
        waitlist.offer(entry);
    }
    
    public Queue<WaitlistEntry> getWaitlist() {
        return waitlist;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
}
