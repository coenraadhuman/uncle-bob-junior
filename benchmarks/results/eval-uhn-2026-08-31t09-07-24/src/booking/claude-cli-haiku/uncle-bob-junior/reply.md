```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class TicketTier {
    private final String name;
    private final double price;
    
    public TicketTier(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String name() { return name; }
    public double price() { return price; }
}

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

class SeatHold {
    private final String id;
    private final List<Integer> seatNumbers;
    private final LocalDateTime expiryTime;
    private final double totalPrice;
    
    public SeatHold(String id, List<Integer> seatNumbers, LocalDateTime expiryTime, double totalPrice) {
        this.id = id;
        this.seatNumbers = seatNumbers;
        this.expiryTime = expiryTime;
        this.totalPrice = totalPrice;
    }
    
    public String id() { return id; }
    public List<Integer> seatNumbers() { return seatNumbers; }
    public LocalDateTime expiryTime() { return expiryTime; }
    public double totalPrice() { return totalPrice; }
}

class Booking {
    private final String id;
    private final List<Integer> seatNumbers;
    private final double totalPrice;
    private final LocalDateTime bookedAt;
    
    public Booking(String id, List<Integer> seatNumbers, double totalPrice, LocalDateTime bookedAt) {
        this.id = id;
        this.seatNumbers = seatNumbers;
        this.totalPrice = totalPrice;
        this.bookedAt = bookedAt;
    }
    
    public String id() { return id; }
    public List<Integer> seatNumbers() { return seatNumbers; }
    public double totalPrice() { return totalPrice; }
    public LocalDateTime bookedAt() { return bookedAt; }
}

class WaitlistEntry {
    private final String id;
    private final int seatsRequested;
    private final List<TicketTier> tiers;
    private final LocalDateTime requestedAt;
    
    public WaitlistEntry(String id, int seatsRequested, List<TicketTier> tiers, LocalDateTime requestedAt) {
        this.id = id;
        this.seatsRequested = seatsRequested;
        this.tiers = tiers;
        this.requestedAt = requestedAt;
    }
    
    public String id() { return id; }
    public int seatsRequested() { return seatsRequested; }
    public List<TicketTier> tiers() { return tiers; }
    public LocalDateTime requestedAt() { return requestedAt; }
}

class RefundPolicy {
    private static final int FULL_REFUND_DAYS = 30;
    private static final int PARTIAL_REFUND_DAYS = 7;
    
    public double calculateRefund(double ticketPrice, LocalDateTime eventTime, LocalDateTime now) {
        long daysUntilEvent = ChronoUnit.DAYS.between(now, eventTime);
        
        if (daysUntilEvent > FULL_REFUND_DAYS) {
            return ticketPrice;
        }
        if (daysUntilEvent > PARTIAL_REFUND_DAYS) {
            return ticketPrice * 0.5;
        }
        return 0;
    }
}

class PricingEngine {
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_RATE = 0.05;
    
    public double calculateTotal(List<TicketTier> tiers) {
        double subtotal = tiers.stream()
            .mapToDouble(TicketTier::price)
            .sum();
        
        if (tiers.size() >= GROUP_DISCOUNT_THRESHOLD) {
            return subtotal * (1 - GROUP_DISCOUNT_RATE);
        }
        return subtotal;
    }
}

class BookingEngine {
    private final Event event;
    private final Map<Integer, Seat> seats;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    private final RefundPolicy refundPolicy;
    private final PricingEngine pricingEngine;
    
    public BookingEngine(Event event) {
        this.event = event;
        this.seats = initializeSeats(event.totalSeats());
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();
        this.refundPolicy = new RefundPolicy();
        this.pricingEngine = new PricingEngine();
    }
    
    private Map<Integer, Seat> initializeSeats(int totalSeats) {
        Map<Integer, Seat> map = new LinkedHashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            map.put(i, new Seat(i));
        }
        return map;
    }
    
    public SeatHold holdSeats(int count, List<TicketTier> tiers, LocalDateTime now) {
        expireOldHolds(now);
        List<Integer> available = findAvailableSeats(count);
        
        if (available.isEmpty()) {
            addToWaitlist(count, tiers, now);
            return null;
        }
        
        return createHold(available, tiers, now);
    }
    
    private SeatHold createHold(List<Integer> seatNumbers, List<TicketTier> tiers, LocalDateTime now) {
        String holdId = UUID.randomUUID().toString();
        LocalDateTime expiry = now.plusMinutes(15);
        double price = pricingEngine.calculateTotal(tiers);
        
        for (int seatNumber : seatNumbers) {
            seats.get(seatNumber).holdFor(holdId, expiry);
        }
        
        SeatHold hold = new SeatHold(holdId, seatNumbers, expiry, price);
        holds.put(holdId, hold);
        return hold;
    }
    
    public Booking confirmHold(String holdId, LocalDateTime now) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        if (hold.expiryTime().isBefore(now)) {
            releaseHold(holdId);
            throw new IllegalArgumentException("Hold expired: " + holdId);
        }
        
        String bookingId = UUID.randomUUID().toString();
        for (int seatNumber : hold.seatNumbers()) {
            seats.get(seatNumber).confirmHold(bookingId);
        }
        
        Booking booking = new Booking(bookingId, hold.seatNumbers(), hold.totalPrice(), now);
        bookings.put(bookingId, booking);
        holds.remove(holdId);
        
        processWaitlist(now);
        return booking;
    }
    
    public void releaseHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            return;
        }
        
        for (int seatNumber : hold.seatNumbers()) {
            seats.get(seatNumber).release();
        }
        holds.remove(holdId);
    }
    
    public double cancelBooking(String bookingId, LocalDateTime now) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        
        releaseSeatNumbers(booking.seatNumbers());
        double refund = calculateTotalRefund(booking, now);
        bookings.remove(bookingId);
        
        processWaitlist(now);
        return refund;
    }
    
    private void releaseSeatNumbers(List<Integer> seatNumbers) {
        for (int seatNumber : seatNumbers) {
            seats.get(seatNumber).release();
        }
    }
    
    private double calculateTotalRefund(Booking booking, LocalDateTime now) {
        return booking.seatNumbers().stream()
            .mapToDouble(unused -> refundPolicy.calculateRefund(
                pricePerSeat(booking),
                event.eventTime(),
                now
            ))
            .sum();
    }
    
    private double pricePerSeat(Booking booking) {
        return booking.totalPrice() / booking.seatNumbers().size();
    }
    
    private void expireOldHolds(LocalDateTime now) {
        List<String> expiredHoldIds = holds.values().stream()
            .filter(hold -> hold.expiryTime().isBefore(now))
            .map(SeatHold::id)
            .collect(Collectors.toList());
        
        expiredHoldIds.forEach(this::releaseHold);
    }
    
    private List<Integer> findAvailableSeats(int count) {
        return seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.AVAILABLE)
            .limit(count)
            .map(Seat::number)
            .collect(Collectors.toList());
    }
    
    private void addToWaitlist(int count, List<TicketTier> tiers, LocalDateTime now) {
        String entryId = UUID.randomUUID().toString();
        WaitlistEntry entry = new WaitlistEntry(entryId, count, tiers, now);
        waitlist.offer(entry);
    }
    
    private void processWaitlist(LocalDateTime now) {
        while (!waitlist.isEmpty()) {
            WaitlistEntry entry = waitlist.peek();
            List<Integer> available = findAvailableSeats(entry.seatsRequested());
            
            if (available.size() < entry.seatsRequested()) {
                break;
            }
            
            waitlist.poll();
            SeatHold hold = holdSeats(entry.seatsRequested(), entry.tiers(), now);
            if (hold != null) {
                confirmHold(hold.id(), now);
            }
        }
    }
    
    public int availableSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.AVAILABLE)
            .count();
    }
    
    public int heldSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.HELD)
            .count();
    }
    
    public int bookedSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.BOOKED)
            .count();
    }
    
    public int waitlistSize() {
        return waitlist.size();
    }
    
    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
}

public class BookingEngineTest {
    
    static void testHoldAndConfirm() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        
        SeatHold hold = engine.holdSeats(2, tiers, now);
        assert hold != null : "Hold should be created";
        assert hold.seatNumbers().size() == 2 : "Hold should have 2 seats";
        assert engine.heldSeatCount() == 2 : "Should have 2 held seats";
        
        Booking booking = engine.confirmHold(hold.id(), now);
        assert booking != null : "Booking should be created";
        assert engine.bookedSeatCount() == 2 : "Should have 2 booked seats";
        assert engine.heldSeatCount() == 0 : "Should have 0 held seats";
    }
    
    static void testHoldExpiry() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 50.0));
        SeatHold hold = engine.holdSeats(1, tiers, now);
        assert engine.heldSeatCount() == 1 : "Should have 1 held seat";
        
        LocalDateTime later = now.plusMinutes(16);
        engine.holdSeats(1, tiers, later);
        
        assert engine.heldSeatCount() == 1 : "Expired hold should be released";
        assert engine.availableSeatCount() == 99 : "First seat should be available again";
    }
    
    static void testReleaseHold() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 50.0));
        SeatHold hold = engine.holdSeats(1, tiers, now);
        assert engine.heldSeatCount() == 1 : "Should have 1 held seat";
        
        engine.releaseHold(hold.id());
        assert engine.heldSeatCount() == 0 : "Should release held seat";
        assert engine.availableSeatCount() == 100 : "Seat should be available";
    }
    
    static void testGroupDiscount() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> smallGroup = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        SeatHold smallHold = engine.holdSeats(2, smallGroup, now);
        assert smallHold.totalPrice() == 100.0 : "Small group no discount: " + smallHold.totalPrice();
        
        List<TicketTier> largeGroup = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            largeGroup.add(new TicketTier("Adult", 50.0));
        }
        SeatHold largeHold = engine.holdSeats(10, largeGroup, now);
        assert largeHold.totalPrice() == 475.0 : "Large group 5% discount: " + largeHold.totalPrice();
    }
    
    static void testMixedTierPricing() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Child", 25.0),
            new TicketTier("Senior", 30.0)
        );
        
        SeatHold hold = engine.holdSeats(3, tiers, now);
        assert hold.totalPrice() == 105.0 : "Sum mixed tiers: " + hold.totalPrice();
    }
    
    static void testCancellationRefund() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime bookingTime = LocalDateTime.of(2026, 8, 10, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 100.0));
        
        SeatHold hold = engine.holdSeats(1, tiers, bookingTime);
        Booking booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime earlyCancel = LocalDateTime.of(2026, 8, 14, 10, 0);
        double refund = engine.cancelBooking(booking.id(), earlyCancel);
        assert refund == 100.0 : "Full refund >30 days: " + refund;
        
        hold = engine.holdSeats(1, tiers, bookingTime);
        booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime midCancel = LocalDateTime.of(2026, 8, 31, 10, 0);
        refund = engine.cancelBooking(booking.id(), midCancel);
        assert refund == 50.0 : "Half refund 7-30 days: " + refund;
        
        hold = engine.holdSeats(1, tiers, bookingTime);
        booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime lateCancel = LocalDateTime.of(2026, 9, 10, 10, 0);
        refund = engine.cancelBooking(booking.id(), lateCancel);
        assert refund == 0.0 : "No refund <7 days: " + refund;
    }
    
    static void testWaitlistProcessing() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 2);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiersSingle = List.of(new TicketTier("Adult", 50.0));
        List<TicketTier> tiersPair = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        
        SeatHold hold1 = engine.holdSeats(2, tiersPair, now);
        Booking booking1 = engine.confirmHold(hold1.id(), now);
        assert engine.waitlistSize() == 0 : "No waitlist when not full";
        
        SeatHold hold2 = engine.holdSeats(1, tiersSingle, now);
        assert hold2 == null : "No hold when full";
        assert engine.waitlistSize() == 1 : "Entry added to waitlist";
        
        engine.cancelBooking(booking1.id(), now);
        assert engine.waitlistSize() == 0 : "Waitlist processed after cancellation";
        assert engine.bookedSeatCount() == 1 : "Waitlist entry booked";
    }
    
    public static void main(String[] args) {
        testHoldAndConfirm();
        testHoldExpiry();
        testReleaseHold();
        testGroupDiscount();
        testMixedTierPricing();
        testCancellationRefund();
        testWaitlistProcessing();
        System.out.println("All tests passed!");
    }
}
```

**Assumptions:**
- Holds expire after exactly 15 minutes; confirmation after expiry fails.
- Group discount (5% off) applies when 10+ seats are booked in a single hold.
- Refund tiers: >30 days = 100%, 7–30 days = 50%, <7 days = 0%.
- Waitlist is FIFO; when seats free up, the next entry auto-holds then auto-confirms.
- Per-seat refund is calculated as `totalPrice / seatCount`.
- All ticket tiers in a hold are priced independently (e.g., "Adult $50, Child $25").

**Code:** `BookingEngine` orchestrates seat availability, holds, bookings, and waitlist. `Seat` tracks state (AVAILABLE, HELD, BOOKED); `SeatHold` is a 15-minute temporary reservation; `Booking` is confirmed. `RefundPolicy` applies cancellation rules by days-until-event. `PricingEngine` sums tiers and applies group discount. Tests cover hold/confirm, expiry, mixed pricing, all refund tiers, and waitlist FIFO processing.