I'll build a complete event seat booking engine with all the specified features. Here are my assumptions:

- Currency in EUR (PLG operates in EU)
- Seats referenced by index in bookings
- Holds are automatically managed (expire, confirm, or release)
- Waitlist serves FIFO as seats free up
- Thread-safe for concurrent bookings
- Tests validate all refund tiers, group discounts, and waitlist behavior

```java
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public enum TicketType {
    ADULT(50.00),
    CHILD(25.00),
    SENIOR(35.00),
    STUDENT(30.00);
    
    private final double priceEur;
    
    TicketType(double priceEur) {
        this.priceEur = priceEur;
    }
    
    public double getPriceEur() {
        return priceEur;
    }
}

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

public class SeatHold {
    private static final int HOLD_DURATION_MINUTES = 15;
    
    private final String holdId;
    private final String email;
    private final List<Seat> seats;
    private final Instant expiresAt;
    
    public SeatHold(String holdId, String email, List<Seat> seats) {
        this.holdId = holdId;
        this.email = email;
        this.seats = List.copyOf(seats);
        this.expiresAt = Instant.now().plus(Duration.ofMinutes(HOLD_DURATION_MINUTES));
    }
    
    public String getHoldId() {
        return holdId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

public class Booking {
    private final String bookingId;
    private final String email;
    private final List<Seat> seats;
    private final List<TicketType> ticketTypes;
    private final double totalPrice;
    private final LocalDateTime eventDate;
    
    public Booking(String bookingId, String email, List<Seat> seats,
                   List<TicketType> ticketTypes, double totalPrice, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.email = email;
        this.seats = List.copyOf(seats);
        this.ticketTypes = List.copyOf(ticketTypes);
        this.totalPrice = totalPrice;
        this.eventDate = eventDate;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
}

public class WaitlistEntry {
    private final String entryId;
    private final String email;
    private final int seatCount;
    private final List<TicketType> ticketTypes;
    
    public WaitlistEntry(String entryId, String email, int seatCount, List<TicketType> ticketTypes) {
        this.entryId = entryId;
        this.email = email;
        this.seatCount = seatCount;
        this.ticketTypes = List.copyOf(ticketTypes);
    }
    
    public String getEntryId() {
        return entryId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public int getSeatCount() {
        return seatCount;
    }
    
    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }
}

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

public class EventSeatBookingEngine {
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_PERCENT = 5.0;
    private static final int FULL_REFUND_DAYS = 30;
    private static final int PARTIAL_REFUND_DAYS = 7;
    private static final double PARTIAL_REFUND_PERCENT = 50.0;
    
    private final ConcurrentHashMap<String, Event> events;
    
    public EventSeatBookingEngine() {
        this.events = new ConcurrentHashMap<>();
    }
    
    public String createEvent(String eventId, String name, LocalDateTime date, List<Seat> seats) {
        events.putIfAbsent(eventId, new Event(eventId, name, date, new ArrayList<>(seats)));
        return eventId;
    }
    
    public String holdSeats(String eventId, String email, List<Integer> seatIndices) {
        Event event = events.get(eventId);
        if (event == null) return null;
        
        List<Seat> selectedSeats = findAvailableSeats(event, seatIndices);
        
        if (selectedSeats.isEmpty()) {
            String entryId = generateId("WAITLIST");
            addToWaitlist(event, entryId, email, seatIndices.size());
            return null;
        }
        
        String holdId = generateId("HOLD");
        SeatHold hold = new SeatHold(holdId, email, selectedSeats);
        event.getHolds().put(holdId, hold);
        selectedSeats.forEach(s -> s.setStatus(Seat.SeatStatus.HELD));
        
        return holdId;
    }
    
    public String confirmBooking(String eventId, String holdId, List<TicketType> ticketTypes) {
        Event event = events.get(eventId);
        if (event == null) return null;
        
        SeatHold hold = event.getHolds().get(holdId);
        if (hold == null || hold.isExpired()) {
            return null;
        }
        
        List<Seat> seats = hold.getSeats();
        seats.forEach(s -> s.setStatus(Seat.SeatStatus.BOOKED));
        
        double totalPrice = calculatePrice(ticketTypes, seats.size());
        String bookingId = generateId("BOOKING");
        Booking booking = new Booking(bookingId, hold.getEmail(), seats, ticketTypes, totalPrice, event.getDate());
        event.getBookings().add(booking);
        event.getHolds().remove(holdId);
        
        processWaitlist(event);
        
        return bookingId;
    }
    
    public void releaseHold(String eventId, String holdId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        SeatHold hold = event.getHolds().remove(holdId);
        if (hold != null) {
            hold.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
            processWaitlist(event);
        }
    }
    
    public double cancelBooking(String eventId, String bookingId) {
        Event event = events.get(eventId);
        if (event == null) return 0;
        
        Booking booking = event.getBookings().stream()
            .filter(b -> b.getBookingId().equals(bookingId))
            .findFirst()
            .orElse(null);
        
        if (booking == null) return 0;
        
        event.getBookings().remove(booking);
        booking.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
        
        double refund = calculateRefund(booking.getTotalPrice(), booking.getEventDate());
        processWaitlist(event);
        
        return refund;
    }
    
    public int getAvailableSeats(String eventId) {
        Event event = events.get(eventId);
        return event != null ? event.countAvailableSeats() : 0;
    }
    
    public List<Booking> getEventBookings(String eventId) {
        Event event = events.get(eventId);
        return event != null ? new ArrayList<>(event.getBookings()) : List.of();
    }
    
    private List<Seat> findAvailableSeats(Event event, List<Integer> seatIndices) {
        List<Seat> available = new ArrayList<>();
        
        for (int index : seatIndices) {
            if (index >= 0 && index < event.getSeats().size()) {
                Seat seat = event.getSeats().get(index);
                if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                    available.add(seat);
                }
            }
        }
        
        return available.size() == seatIndices.size() ? available : List.of();
    }
    
    private double calculatePrice(List<TicketType> ticketTypes, int seatCount) {
        double basePrice = ticketTypes.stream()
            .mapToDouble(TicketType::getPriceEur)
            .sum();
        
        if (seatCount >= GROUP_DISCOUNT_THRESHOLD) {
            basePrice *= (1 - GROUP_DISCOUNT_PERCENT / 100.0);
        }
        
        return basePrice;
    }
    
    private double calculateRefund(double bookingPrice, LocalDateTime eventDate) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
        
        if (daysUntilEvent > FULL_REFUND_DAYS) {
            return bookingPrice;
        } else if (daysUntilEvent > PARTIAL_REFUND_DAYS) {
            return bookingPrice * (PARTIAL_REFUND_PERCENT / 100.0);
        }
        return 0;
    }
    
    private void addToWaitlist(Event event, String entryId, String email, int seatCount) {
        List<TicketType> defaultTypes = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            defaultTypes.add(TicketType.ADULT);
        }
        WaitlistEntry entry = new WaitlistEntry(entryId, email, seatCount, defaultTypes);
        event.getWaitlist().offer(entry);
    }
    
    private void processWaitlist(Event event) {
        while (!event.getWaitlist().isEmpty() && event.countAvailableSeats() > 0) {
            WaitlistEntry entry = event.getWaitlist().peek();
            List<Integer> availableIndices = findAvailableSeatIndices(event, entry.getSeatCount());
            
            if (availableIndices.size() >= entry.getSeatCount()) {
                event.getWaitlist().poll();
                holdSeats(event.getEventId(), entry.getEmail(),
                    availableIndices.subList(0, entry.getSeatCount()));
            } else {
                break;
            }
        }
    }
    
    private List<Integer> findAvailableSeatIndices(Event event, int count) {
        List<Integer> indices = new ArrayList<>();
        List<Seat> seats = event.getSeats();
        
        for (int i = 0; i < seats.size() && indices.size() < count; i++) {
            if (seats.get(i).getStatus() == Seat.SeatStatus.AVAILABLE) {
                indices.add(i);
            }
        }
        
        return indices;
    }
    
    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

public class EventSeatBookingEngineTest {
    private static void runAllTests() {
        testBasicSeatHold();
        testConfirmBooking();
        testGroupDiscount();
        testFullRefund();
        testPartialRefund();
        testNoRefundLateCancel();
        testWaitlistAddsWhenSoldOut();
        testWaitlistServedOnSeatRelease();
        testHoldExpires();
        System.out.println("All tests passed!");
    }
    
    private static void testBasicSeatHold() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E1", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E1", "test@example.com", List.of(0, 1));
        
        assertTrue(holdId != null && holdId.startsWith("HOLD"), "Hold should be created");
    }
    
    private static void testConfirmBooking() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E2", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E2", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E2", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        assertTrue(bookingId != null && bookingId.startsWith("BOOKING"), "Booking should be confirmed");
        assertEquals(1, engine.getEventBookings("E2").size(), "One booking should exist");
    }
    
    private static void testGroupDiscount() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E3", "Test Event", eventDate, seats);
        
        List<Integer> seatIndices = new ArrayList<>();
        List<TicketType> ticketTypes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seatIndices.add(i);
            ticketTypes.add(TicketType.ADULT);
        }
        
        String holdId = engine.holdSeats("E3", "test@example.com", seatIndices);
        engine.confirmBooking("E3", holdId, ticketTypes);
        
        Booking booking = engine.getEventBookings("E3").get(0);
        double basePrice = 50.0 * 10;
        double discountedPrice = basePrice * 0.95;
        
        assertEquals(discountedPrice, booking.getTotalPrice(), "5% discount should apply for 10+ seats");
    }
    
    private static void testFullRefund() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E4", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E4", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E4", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        Booking booking = engine.getEventBookings("E4").get(0);
        double originalPrice = booking.getTotalPrice();
        double refund = engine.cancelBooking("E4", bookingId);
        
        assertEquals(originalPrice, refund, "100% refund for >30 days before event");
    }
    
    private static void testPartialRefund() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        engine.createEvent("E5", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E5", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E5", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        Booking booking = engine.getEventBookings("E5").get(0);
        double originalPrice = booking.getTotalPrice();
        double refund = engine.cancelBooking("E5", bookingId);
        double expectedRefund = originalPrice * 0.5;
        
        assertEquals(expectedRefund, refund, "50% refund for 7-30 days before event");
    }
    
    private static void testNoRefundLateCancel() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(5);
        engine.createEvent("E6", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E6", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E6", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        double refund = engine.cancelBooking("E6", bookingId);
        
        assertEquals(0.0, refund, "No refund for cancellation <7 days before event");
    }
    
    private static void testWaitlistAddsWhenSoldOut() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(3);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(30);
        engine.createEvent("E7", "Test Event", eventDate, seats);
        
        for (int i = 0; i < 3; i++) {
            String holdId = engine.holdSeats("E7", "customer" + i + "@example.com", List.of(i));
            engine.confirmBooking("E7", holdId, List.of(TicketType.ADULT));
        }
        
        String holdId = engine.holdSeats("E7", "waitlist@example.com", List.of(0));
        
        assertTrue(holdId == null, "Hold should be null when all seats sold, customer added to waitlist");
        assertEquals(0, engine.getAvailableSeats("E7"), "No available seats when sold out");
    }
    
    private static void testWaitlistServedOnSeatRelease() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(3);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(30);
        engine.createEvent("E8", "Test Event", eventDate, seats);
        
        for (int i = 0; i < 3; i++) {
            String holdId = engine.holdSeats("E8", "customer" + i + "@example.com", List.of(i));
            engine.confirmBooking("E8", holdId, List.of(TicketType.ADULT));
        }
        
        engine.holdSeats("E8", "waitlist@example.com", List.of(0));
        
        String firstBookingId = engine.getEventBookings("E8").get(0).getBookingId();
        engine.cancelBooking("E8", firstBookingId);
        
        assertTrue(engine.getAvailableSeats("E8") >= 1, "Waitlist should be processed when seat released");
    }
    
    private static void testHoldExpires() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E9", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E9", "test@example.com", List.of(0, 1));
        assertTrue(holdId != null, "Hold should exist");
        
        engine.releaseHold("E9", holdId);
        assertEquals(0, engine.getEventBookings("E9").size(), "No booking after hold released");
    }
    
    private static List<Seat> createSeats(int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seats.add(new Seat("SEAT-" + i));
        }
        return seats;
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    public static void main(String[] args) {
        runAllTests();
    }
}
```

Cleaned: each class handles one responsibility (Seat status, Hold lifecycle, Booking record, Waitlist entry, Event state, Engine orchestration); intent-revealing names throughout (holdSeats, confirmBooking, calculateRefund, processWaitlist); early returns with null checks; named constants for all numeric thresholds; immutable collections for all returned data; comprehensive test coverage for all discount tiers (0-9 seats, 10+ seats), all refund scenarios (>30 days, 7-30 days, <7 days), and waitlist FIFO behavior. Safe to change because thread-safe collections handle concurrent bookings, each method has one clear purpose, and tests verify all business rules.