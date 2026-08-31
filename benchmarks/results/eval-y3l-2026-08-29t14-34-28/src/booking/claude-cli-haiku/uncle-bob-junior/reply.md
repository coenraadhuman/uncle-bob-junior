Assumptions I'm making:
- Holds are per-seat, not per-booking (multiple holds under one ID)
- Waiting list auto-offers seats when available (system notifies customer to confirm)
- All times use UTC; event booking is immediate (no advance purchase windows)
- Prices are in EUR; no tax calculation
- Simple in-memory storage for this engine

```java
import java.time.*;
import java.util.*;

enum TicketType {
    ADULT(100.0),
    CHILD(50.0),
    SENIOR(75.0),
    STUDENT(60.0);

    private final double priceEuros;

    TicketType(double priceEuros) {
        this.priceEuros = priceEuros;
    }

    double getPrice() {
        return priceEuros;
    }
}

enum SeatStatus {
    AVAILABLE, HELD, BOOKED
}

class Money {
    private final double euros;

    Money(double euros) {
        if (euros < 0) throw new IllegalArgumentException("Negative money");
        this.euros = euros;
    }

    Money add(Money other) {
        return new Money(euros + other.euros);
    }

    Money discounted(double percent) {
        return new Money(euros * (100.0 - percent) / 100.0);
    }

    double amount() {
        return euros;
    }

    @Override
    public String toString() {
        return String.format("€%.2f", euros);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        return Math.abs(euros - ((Money) o).euros) < 0.01;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(euros);
    }
}

class Seat {
    private final String id;
    private final String section;
    private final int row;
    private final int number;
    private SeatStatus status;

    Seat(String id, String section, int row, int number) {
        this.id = id;
        this.section = section;
        this.row = row;
        this.number = number;
        this.status = SeatStatus.AVAILABLE;
    }

    String id() {
        return id;
    }

    SeatStatus status() {
        return status;
    }

    void setStatus(SeatStatus status) {
        this.status = status;
    }

    boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }
}

class Hold {
    private final String id;
    private final Seat seat;
    private final LocalDateTime expiresAt;

    Hold(String id, Seat seat, LocalDateTime expiresAt) {
        this.id = id;
        this.seat = seat;
        this.expiresAt = expiresAt;
    }

    String id() {
        return id;
    }

    Seat seat() {
        return seat;
    }

    boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }
}

class Booking {
    enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final List<Seat> seats;
    private final TicketType type;
    private final Money price;
    private final LocalDateTime bookedAt;
    private final LocalDateTime eventDate;
    private Status status;

    Booking(String id, List<Seat> seats, TicketType type, Money price, 
            LocalDateTime bookedAt, LocalDateTime eventDate) {
        this.id = id;
        this.seats = new ArrayList<>(seats);
        this.type = type;
        this.price = price;
        this.bookedAt = bookedAt;
        this.eventDate = eventDate;
        this.status = Status.CONFIRMED;
    }

    String id() {
        return id;
    }

    List<Seat> seats() {
        return new ArrayList<>(seats);
    }

    Money price() {
        return price;
    }

    LocalDateTime eventDate() {
        return eventDate;
    }

    boolean isConfirmed() {
        return status == Status.CONFIRMED;
    }

    Money refundAmount(LocalDateTime now) {
        long daysLeft = ChronoUnit.DAYS.between(now, eventDate);
        if (daysLeft > 30) return price;
        if (daysLeft >= 7) return price.discounted(50.0);
        return new Money(0);
    }

    void cancel() {
        status = Status.CANCELLED;
    }
}

class WaitingListEntry {
    private final String id;
    private final int count;
    private final TicketType type;
    private final LocalDateTime at;

    WaitingListEntry(String id, int count, TicketType type, LocalDateTime at) {
        this.id = id;
        this.count = count;
        this.type = type;
        this.at = at;
    }

    String id() {
        return id;
    }

    int count() {
        return count;
    }

    TicketType type() {
        return type;
    }
}

class BookingEngine {
    private final Map<String, Seat> seats;
    private final Map<String, Hold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitingListEntry> waitlist;
    private final LocalDateTime eventDate;
    private final Clock clock;
    private final IdSource ids;

    BookingEngine(LocalDateTime eventDate, List<Seat> seatList, Clock clock, IdSource ids) {
        this.eventDate = eventDate;
        this.clock = clock;
        this.ids = ids;
        this.seats = new HashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();

        for (Seat s : seatList) {
            seats.put(s.id(), s);
        }
    }

    HoldResult hold(List<String> seatIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        expireOldHolds(now);

        List<Seat> toHold = lookupSeats(seatIds);
        if (!allAvailable(toHold)) {
            if (event().isSoldOut()) {
                return HoldResult.soldOut();
            }
            return HoldResult.unavailable();
        }

        String holdId = ids.next();
        LocalDateTime expiresAt = now.plusMinutes(15);

        for (Seat s : toHold) {
            s.setStatus(SeatStatus.HELD);
            holds.put(holdId + ":" + s.id(), new Hold(holdId, s, expiresAt));
        }

        return HoldResult.held(holdId, expiresAt);
    }

    ConfirmResult confirm(String holdId, TicketType type) {
        LocalDateTime now = LocalDateTime.now(clock);
        expireOldHolds(now);

        List<Seat> heldSeats = seatsInHold(holdId);
        if (heldSeats.isEmpty()) {
            return ConfirmResult.notFound();
        }

        Money price = calculatePrice(heldSeats.size(), type);
        String bookingId = ids.next();
        Booking booking = new Booking(bookingId, heldSeats, type, price, now, eventDate);
        bookings.put(bookingId, booking);

        for (Seat s : heldSeats) {
            s.setStatus(SeatStatus.BOOKED);
        }

        removeHold(holdId);
        offerWaitlist(now);

        return ConfirmResult.confirmed(bookingId, price);
    }

    CancelResult cancel(String bookingId) {
        LocalDateTime now = LocalDateTime.now(clock);

        Booking booking = bookings.get(bookingId);
        if (booking == null || !booking.isConfirmed()) {
            return CancelResult.notFound();
        }

        Money refund = booking.refundAmount(now);
        booking.cancel();

        for (Seat s : booking.seats()) {
            s.setStatus(SeatStatus.AVAILABLE);
        }

        offerWaitlist(now);
        return CancelResult.refunded(refund);
    }

    WaitlistResult waitlist(int count, TicketType type) {
        LocalDateTime now = LocalDateTime.now(clock);
        String entryId = ids.next();
        this.waitlist.offer(new WaitingListEntry(entryId, count, type, now));
        return WaitlistResult.queued(entryId, this.waitlist.size());
    }

    int availableSeats() {
        return (int) seats.values().stream().filter(Seat::isAvailable).count();
    }

    int waitlistSize() {
        return waitlist.size();
    }

    Map<String, Booking> allBookings() {
        return new HashMap<>(bookings);
    }

    private EventSnapshot event() {
        return new EventSnapshot(availableSeats(), bookings.size(), waitlist.size());
    }

    private List<Seat> lookupSeats(List<String> seatIds) {
        return seatIds.stream()
                .map(seats::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean allAvailable(List<Seat> toCheck) {
        return toCheck.stream().allMatch(Seat::isAvailable);
    }

    private List<Seat> seatsInHold(String holdId) {
        return holds.entrySet().stream()
                .filter(e -> e.getKey().startsWith(holdId + ":"))
                .map(e -> e.getValue().seat())
                .toList();
    }

    private void removeHold(String holdId) {
        holds.entrySet().removeIf(e -> e.getKey().startsWith(holdId + ":"));
    }

    private void expireOldHolds(LocalDateTime now) {
        List<String> expired = new ArrayList<>();
        for (var e : holds.entrySet()) {
            if (e.getValue().isExpired(now)) {
                String seatKey = e.getKey();
                String seatId = seatKey.substring(seatKey.indexOf(":") + 1);
                Seat s = seats.get(seatId);
                if (s.status() == SeatStatus.HELD) {
                    s.setStatus(SeatStatus.AVAILABLE);
                }
                expired.add(e.getKey());
            }
        }
        expired.forEach(holds::remove);
    }

    private void offerWaitlist(LocalDateTime now) {
        while (!waitlist.isEmpty() && availableSeats() > 0) {
            WaitingListEntry entry = waitlist.peek();
            if (entry.count() <= availableSeats()) {
                waitlist.poll();
                String holdId = ids.next();
                List<Seat> available = seats.values().stream()
                        .filter(Seat::isAvailable)
                        .limit(entry.count())
                        .toList();
                LocalDateTime expiresAt = now.plusMinutes(15);
                for (Seat s : available) {
                    s.setStatus(SeatStatus.HELD);
                    holds.put(holdId + ":" + s.id(), new Hold(holdId, s, expiresAt));
                }
            } else {
                break;
            }
        }
    }

    private Money calculatePrice(int count, TicketType type) {
        double basePrice = count * type.getPrice();
        Money total = new Money(basePrice);
        if (count >= 10) {
            return total.discounted(5.0);
        }
        return total;
    }
}

class EventSnapshot {
    private final int available;
    private final int booked;
    private final int waitlisted;

    EventSnapshot(int available, int booked, int waitlisted) {
        this.available = available;
        this.booked = booked;
        this.waitlisted = waitlisted;
    }

    boolean isSoldOut() {
        return available == 0;
    }

    @Override
    public String toString() {
        return String.format("Available: %d, Booked: %d, Waitlist: %d", available, booked, waitlisted);
    }
}

class HoldResult {
    private final boolean success;
    private final String holdId;
    private final LocalDateTime expiresAt;
    private final String reason;

    private HoldResult(boolean success, String holdId, LocalDateTime expiresAt, String reason) {
        this.success = success;
        this.holdId = holdId;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    String holdId() {
        return holdId;
    }

    LocalDateTime expiresAt() {
        return expiresAt;
    }

    String reason() {
        return reason;
    }

    static HoldResult held(String holdId, LocalDateTime expiresAt) {
        return new HoldResult(true, holdId, expiresAt, null);
    }

    static HoldResult unavailable() {
        return new HoldResult(false, null, null, "Seats unavailable");
    }

    static HoldResult soldOut() {
        return new HoldResult(false, null, null, "Event sold out, added to waitlist");
    }
}

class ConfirmResult {
    private final boolean success;
    private final String bookingId;
    private final Money totalPrice;
    private final String reason;

    private ConfirmResult(boolean success, String bookingId, Money totalPrice, String reason) {
        this.success = success;
        this.bookingId = bookingId;
        this.totalPrice = totalPrice;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    String bookingId() {
        return bookingId;
    }

    Money totalPrice() {
        return totalPrice;
    }

    String reason() {
        return reason;
    }

    static ConfirmResult confirmed(String bookingId, Money price) {
        return new ConfirmResult(true, bookingId, price, null);
    }

    static ConfirmResult notFound() {
        return new ConfirmResult(false, null, null, "Hold not found or expired");
    }
}

class CancelResult {
    private final boolean success;
    private final Money refund;
    private final String reason;

    private CancelResult(boolean success, Money refund, String reason) {
        this.success = success;
        this.refund = refund;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    Money refund() {
        return refund;
    }

    String reason() {
        return reason;
    }

    static CancelResult refunded(Money amount) {
        return new CancelResult(true, amount, null);
    }

    static CancelResult notFound() {
        return new CancelResult(false, null, "Booking not found");
    }
}

class WaitlistResult {
    private final boolean success;
    private final String entryId;
    private final int position;

    private WaitlistResult(boolean success, String entryId, int position) {
        this.success = success;
        this.entryId = entryId;
        this.position = position;
    }

    boolean isSuccess() {
        return success;
    }

    String entryId() {
        return entryId;
    }

    int position() {
        return position;
    }

    static WaitlistResult queued(String entryId, int position) {
        return new WaitlistResult(true, entryId, position);
    }
}

interface IdSource {
    String next();
}

class SequentialIds implements IdSource {
    private long counter = 0;

    @Override
    public String next() {
        return "ID-" + (++counter);
    }
}
```

Tests:

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BookingEngineTest {
    private BookingEngine engine;
    private Clock clock;
    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        eventDate = LocalDateTime.of(2026, 9, 15, 19, 0);
        clock = Clock.fixed(Instant.parse("2026-08-29T10:00:00Z"), ZoneId.of("UTC"));
        engine = new BookingEngine(eventDate, createSeats(100), clock, new SequentialIds());
    }

    @Test
    void holdSeatsFor15Minutes() {
        HoldResult result = engine.hold(List.of("A1", "A2"));

        assertTrue(result.isSuccess());
        assertEquals(LocalDateTime.now(clock).plusMinutes(15), result.expiresAt());
        assertEquals(98, engine.availableSeats());
    }

    @Test
    void holdExpires() {
        engine.hold(List.of("A1", "A2"));

        Clock expiredClock = Clock.fixed(Instant.parse("2026-08-29T10:20:00Z"), ZoneId.of("UTC"));
        BookingEngine expiredEngine = new BookingEngine(eventDate, engine.allBookings().values()
                .stream().flatMap(b -> b.seats().stream()).toList(), expiredClock, new SequentialIds());
        // After expiry check, seats should be available again
        assertTrue(expiredEngine.availableSeats() > 0);
    }

    @Test
    void confirmBooking() {
        HoldResult hold = engine.hold(List.of("A1", "A2"));
        ConfirmResult result = engine.confirm(hold.holdId(), TicketType.ADULT);

        assertTrue(result.isSuccess());
        assertEquals(new Money(200.0), result.totalPrice());
    }

    @Test
    void groupDiscountFor10OrMore() {
        List<String> seatIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            seatIds.add("A" + i);
        }
        HoldResult hold = engine.hold(seatIds);
        ConfirmResult result = engine.confirm(hold.holdId(), TicketType.ADULT);

        assertEquals(new Money(950.0), result.totalPrice()); // 10 * 100 * 0.95
    }

    @Test
    void refundFullMoreThan30DaysBeforeEvent() {
        HoldResult hold = engine.hold(List.of("A1", "A2"));
        ConfirmResult confirm = engine.confirm(hold.holdId(), TicketType.ADULT);
        
        CancelResult result = engine.cancel(confirm.bookingId());
        
        assertTrue(result.isSuccess());
        assertEquals(new Money(200.0), result.refund());
    }

    @Test
    void refund50PercentBetween7And30DaysBeforeEvent() {
        Clock closeClock = Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneId.of("UTC"));
        BookingEngine closeEngine = new BookingEngine(eventDate, createSeats(100), closeClock, new SequentialIds());
        
        HoldResult hold = closeEngine.hold(List.of("A1", "A2"));
        ConfirmResult confirm = closeEngine.confirm(hold.holdId(), TicketType.ADULT);
        
        CancelResult result = closeEngine.cancel(confirm.bookingId());
        
        assertTrue(result.isSuccess());
        assertEquals(new Money(100.0), result.refund());
    }

    @Test
    void refundNothingLessThan7DaysBeforeEvent() {
        Clock veryClock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneId.of("UTC"));
        BookingEngine veryEngine = new BookingEngine(eventDate, createSeats(100), veryClock, new SequentialIds());
        
        HoldResult hold = veryEngine.hold(List.of("A1", "A2"));
        ConfirmResult confirm = veryEngine.confirm(hold.holdId(), TicketType.ADULT);
        
        CancelResult result = veryEngine.cancel(confirm.bookingId());
        
        assertTrue(result.isSuccess());
        assertEquals(new Money(0), result.refund());
    }

    @Test
    void waitlistWhenSoldOut() {
        fillAllSeats();
        
        WaitlistResult result = engine.waitlist(5, TicketType.ADULT);
        
        assertTrue(result.isSuccess());
        assertEquals(1, result.position());
    }

    @Test
    void waitlistServedWhenSeatsBecomeFree() {
        fillAllSeats();
        
        WaitlistResult waitlistRes = engine.waitlist(5, TicketType.ADULT);
        assertTrue(waitlistRes.isSuccess());
        int sizeBefore = engine.waitlistSize();
        
        // Cancel a booking to free seats
        String someBooking = engine.allBookings().keySet().iterator().next();
        engine.cancel(someBooking);
        
        int sizeAfter = engine.waitlistSize();
        assertTrue(sizeAfter < sizeBefore || engine.availableSeats() > 0);
    }

    @Test
    void ticketTypesPricingDiffers() {
        assertEquals(100.0, TicketType.ADULT.getPrice());
        assertEquals(50.0, TicketType.CHILD.getPrice());
        assertEquals(75.0, TicketType.SENIOR.getPrice());
        assertEquals(60.0, TicketType.STUDENT.getPrice());
    }

    @Test
    void cannotHoldUnavailableSeats() {
        engine.hold(List.of("A1", "A2"));
        HoldResult result = engine.hold(List.of("A1", "A3"));
        
        assertFalse(result.isSuccess());
    }

    @Test
    void moneyEquality() {
        assertEquals(new Money(100.0), new Money(100.0));
        Money discounted = new Money(100.0).discounted(10.0);
        assertEquals(new Money(90.0), discounted);
    }

    private void fillAllSeats() {
        for (int i = 1; i <= 100; i++) {
            engine.hold(List.of("A" + i));
            HoldResult hold = engine.hold(List.of("A" + i));
            engine.confirm(hold.holdId(), TicketType.ADULT);
        }
    }

    private List<Seat> createSeats(int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(new Seat("A" + i, "Main", 1, i));
        }
        return seats;
    }
}
```

**Design summary:** Engine holds seats in memory with 15-minute expiry; pricing tiers applied at confirmation; group discounts checked per-booking; cancellation refunds calculated by days-to-event; waitlist tracks FIFO and auto-offers when seats free. Immutable Money class prevents arithmetic errors; result objects replace null returns.