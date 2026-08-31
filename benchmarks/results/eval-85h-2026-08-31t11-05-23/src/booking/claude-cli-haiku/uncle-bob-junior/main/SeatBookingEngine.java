import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class SeatBookingEngine {
    private static final int HOLD_MINUTES = 15;
    private static final int GROUP_THRESHOLD = 10;
    
    private final Map<String, Event> events;
    private final Clock clock;
    
    public SeatBookingEngine() {
        this(Clock.systemDefaultZone());
    }
    
    public SeatBookingEngine(Clock clock) {
        this.events = new ConcurrentHashMap<>();
        this.clock = clock;
    }
    
    public void createEvent(String eventId, int capacity, LocalDateTime eventDate) {
        events.put(eventId, new Event(eventId, capacity, eventDate));
    }
    
    public BookingResult bookSeats(String eventId, BookingRequest request) {
        Event event = events.get(eventId);
        if (event == null) {
            return BookingResult.failed("Event not found");
        }
        
        if (event.availableCount() >= request.getTotalSeats()) {
            return createBooking(event, request);
        }
        
        return addToWaitlist(event, request);
    }
    
    public boolean confirmBooking(String eventId, String bookingId) {
        Event event = events.get(eventId);
        if (event == null) return false;
        
        Optional<Booking> booking = event.getBooking(bookingId);
        if (booking.isEmpty()) return false;
        
        booking.get().confirm();
        return true;
    }
    
    public int cancelBooking(String eventId, String bookingId) {
        Event event = events.get(eventId);
        if (event == null) return 0;
        
        Optional<Booking> booking = event.getBooking(bookingId);
        if (booking.isEmpty()) return 0;
        
        Booking b = booking.get();
        LocalDateTime now = LocalDateTime.now(clock);
        int refund = b.getRefund(now);
        
        b.cancel();
        event.release(b.getSeats());
        processWaitlist(eventId);
        
        return refund;
    }
    
    public void releaseHold(String eventId, String bookingId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        Optional<Booking> booking = event.getBooking(bookingId);
        if (booking.isEmpty()) return;
        
        event.release(booking.get().getSeats());
        processWaitlist(eventId);
    }
    
    public void expireHolds(String eventId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        LocalDateTime now = LocalDateTime.now(clock);
        event.bookings.values().stream()
            .filter(b -> b.isHoldExpired(now))
            .forEach(b -> {
                b.expire();
                event.release(b.getSeats());
            });
        
        processWaitlist(eventId);
    }
    
    private BookingResult createBooking(Event event, BookingRequest request) {
        List<Seat> allocated = event.allocate(request.getTotalSeats());
        if (allocated.isEmpty()) {
            return BookingResult.failed("Could not allocate seats");
        }
        
        String bookingId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);
        
        Map<Seat, TicketTier> tierMap = mapSeatsToTiers(allocated, request.getQuantities());
        Booking booking = new Booking(bookingId, allocated, tierMap, now, expiresAt, event.getEventDate());
        
        event.addBooking(booking);
        return BookingResult.confirmed(bookingId, booking.getTotalPrice());
    }
    
    private BookingResult addToWaitlist(Event event, BookingRequest request) {
        String entryId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(clock);
        WaitlistEntry entry = new WaitlistEntry(entryId, request.getQuantities(), now);
        event.addToWaitlist(entry);
        return BookingResult.waitlisted(entryId);
    }
    
    private void processWaitlist(String eventId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        while (event.getWaitlist().peek() != null) {
            WaitlistEntry entry = event.getWaitlist().peek();
            if (event.availableCount() < entry.getTotalRequested()) break;
            
            event.getWaitlist().poll();
            BookingRequest request = new BookingRequest(entry.getQuantities());
            createBooking(event, request);
        }
    }
    
    private Map<Seat, TicketTier> mapSeatsToTiers(List<Seat> seats, Map<TicketTier, Integer> quantities) {
        Map<Seat, TicketTier> tierMap = new HashMap<>();
        List<Seat> seatList = new ArrayList<>(seats);
        int index = 0;
        
        for (Map.Entry<TicketTier, Integer> entry : quantities.entrySet()) {
            TicketTier tier = entry.getKey();
            int qty = entry.getValue();
            
            for (int i = 0; i < qty && index < seatList.size(); i++) {
                tierMap.put(seatList.get(index++), tier);
            }
        }
        
        return tierMap;
    }
}
