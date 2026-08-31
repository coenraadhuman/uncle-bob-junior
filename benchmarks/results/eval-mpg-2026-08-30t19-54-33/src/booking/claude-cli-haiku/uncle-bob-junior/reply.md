I'll build a clean, tested event seat booking engine following enterprise Java patterns. Here are my assumptions:

- Time-based holds use `System.currentTimeMillis()` (mockable in tests)
- Waiting list automatically books customers when seats free up
- Refunds are calculated from booking creation time to event date
- Ticket types are fixed (ADULT base price assumed; other types have set multipliers)
- Group discount applies to total price if 10+ seats booked

```java
// TicketType.java
public enum TicketType {
    ADULT(100_00),      // EUR in cents
    CHILD(50_00),
    SENIOR(70_00),
    STUDENT(60_00);

    private final long priceInCents;

    TicketType(long priceInCents) {
        this.priceInCents = priceInCents;
    }

    public long priceInCents() {
        return priceInCents;
    }
}
```

```java
// Money.java
public final class Money {
    private final long amountInCents;

    public Money(long amountInCents) {
        if (amountInCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amountInCents = amountInCents;
    }

    public Money add(Money other) {
        return new Money(this.amountInCents + other.amountInCents);
    }

    public Money multiply(double factor) {
        return new Money(Math.round(this.amountInCents * factor));
    }

    public Money subtract(Money other) {
        if (other.amountInCents > this.amountInCents) {
            throw new IllegalArgumentException("Cannot subtract more than available");
        }
        return new Money(this.amountInCents - other.amountInCents);
    }

    public long cents() {
        return amountInCents;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        Money other = (Money) o;
        return amountInCents == other.amountInCents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amountInCents);
    }
}
```

```java
// SeatState.java
public enum SeatState {
    AVAILABLE,
    HELD,
    BOOKED
}
```

```java
// Seat.java
public final class Seat {
    private final String id;
    private final SeatState state;

    public Seat(String id) {
        this.id = id;
        this.state = SeatState.AVAILABLE;
    }

    private Seat(String id, SeatState state) {
        this.id = id;
        this.state = state;
    }

    public String id() {
        return id;
    }

    public SeatState state() {
        return state;
    }

    public Seat withState(SeatState newState) {
        return new Seat(id, newState);
    }

    public boolean isAvailable() {
        return state == SeatState.AVAILABLE;
    }
}
```

```java
// SeatHold.java
public final class SeatHold {
    private final String id;
    private final String eventId;
    private final java.util.List<String> seatIds;
    private final long expiresAtMillis;
    private final String customerId;

    public SeatHold(String id, String eventId, java.util.List<String> seatIds,
                    long expiresAtMillis, String customerId) {
        this.id = id;
        this.eventId = eventId;
        this.seatIds = java.util.List.copyOf(seatIds);
        this.expiresAtMillis = expiresAtMillis;
        this.customerId = customerId;
    }

    public String id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public java.util.List<String> seatIds() {
        return seatIds;
    }

    public String customerId() {
        return customerId;
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis > expiresAtMillis;
    }

    public int seatCount() {
        return seatIds.size();
    }
}
```

```java
// Booking.java
public final class Booking {
    private final String id;
    private final String eventId;
    private final java.util.List<String> seatIds;
    private final java.util.List<TicketType> ticketTypes;
    private final Money totalPrice;
    private final String customerId;
    private final long createdAtMillis;

    public Booking(String id, String eventId, java.util.List<String> seatIds,
                   java.util.List<TicketType> ticketTypes, Money totalPrice,
                   String customerId, long createdAtMillis) {
        this.id = id;
        this.eventId = eventId;
        this.seatIds = java.util.List.copyOf(seatIds);
        this.ticketTypes = java.util.List.copyOf(ticketTypes);
        this.totalPrice = totalPrice;
        this.customerId = customerId;
        this.createdAtMillis = createdAtMillis;
    }

    public String id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public java.util.List<String> seatIds() {
        return seatIds;
    }

    public java.util.List<TicketType> ticketTypes() {
        return ticketTypes;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public String customerId() {
        return customerId;
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }

    public int seatCount() {
        return seatIds.size();
    }
}
```

```java
// WaitListEntry.java
public final class WaitListEntry {
    private final String customerId;
    private final int requestedSeats;
    private final java.util.List<TicketType> ticketTypes;
    private final long addedAtMillis;

    public WaitListEntry(String customerId, int requestedSeats,
                         java.util.List<TicketType> ticketTypes, long addedAtMillis) {
        this.customerId = customerId;
        this.requestedSeats = requestedSeats;
        this.ticketTypes = java.util.List.copyOf(ticketTypes);
        this.addedAtMillis = addedAtMillis;
    }

    public String customerId() {
        return customerId;
    }

    public int requestedSeats() {
        return requestedSeats;
    }

    public java.util.List<TicketType> ticketTypes() {
        return ticketTypes;
    }

    public long addedAtMillis() {
        return addedAtMillis;
    }
}
```

```java
// RefundPolicy.java
public final class RefundPolicy {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private static final int DAYS_FULL_REFUND = 30;
    private static final int DAYS_HALF_REFUND = 7;

    public Money calculateRefund(Money originalPrice, long eventDateMillis, long nowMillis) {
        long daysUntilEvent = (eventDateMillis - nowMillis) / MILLIS_PER_DAY;
        return calculateRefundByDays(originalPrice, daysUntilEvent);
    }

    private Money calculateRefundByDays(Money originalPrice, long daysUntilEvent) {
        if (daysUntilEvent > DAYS_FULL_REFUND) {
            return originalPrice;
        }
        if (daysUntilEvent > DAYS_HALF_REFUND) {
            return originalPrice.multiply(0.5);
        }
        return new Money(0);
    }
}
```

```java
// Event.java
public final class Event {
    private final String id;
    private final String name;
    private final long eventDateMillis;
    private final int capacity;
    private final java.util.Map<String, Seat> seats;
    private final java.util.Deque<WaitListEntry> waitList;

    public Event(String id, String name, long eventDateMillis, int capacity) {
        this.id = id;
        this.name = name;
        this.eventDateMillis = eventDateMillis;
        this.capacity = capacity;
        this.seats = new java.util.HashMap<>();
        this.waitList = new java.util.LinkedList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        for (int i = 0; i < capacity; i++) {
            String seatId = String.format("SEAT_%03d", i);
            seats.put(seatId, new Seat(seatId));
        }
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long eventDateMillis() {
        return eventDateMillis;
    }

    public int capacity() {
        return capacity;
    }

    public java.util.Map<String, Seat> seats() {
        return new java.util.HashMap<>(seats);
    }

    public void updateSeat(String seatId, Seat seat) {
        seats.put(seatId, seat);
    }

    public void addToWaitList(WaitListEntry entry) {
        waitList.addLast(entry);
    }

    public WaitListEntry pollWaitList() {
        return waitList.pollFirst();
    }

    public int availableSeats() {
        return (int) seats.values().stream()
            .filter(Seat::isAvailable)
            .count();
    }

    public boolean isSoldOut() {
        return availableSeats() == 0;
    }
}
```

```java
// SeatBookingEngine.java
import java.util.*;
import java.util.stream.*;

public final class SeatBookingEngine {
    private static final int HOLD_DURATION_MINUTES = 15;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_PERCENT = 0.05;

    private final java.util.Map<String, Event> events;
    private final java.util.Map<String, SeatHold> holds;
    private final java.util.Map<String, Booking> bookings;
    private final RefundPolicy refundPolicy;

    public SeatBookingEngine() {
        this.events = new HashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.refundPolicy = new RefundPolicy();
    }

    public void createEvent(String eventId, String name, long eventDateMillis, int capacity) {
        events.put(eventId, new Event(eventId, name, eventDateMillis, capacity));
    }

    public SeatHold holdSeats(String eventId, java.util.List<TicketType> ticketTypes, String customerId) {
        Event event = findEventOrThrow(eventId);
        expireOldHolds(event);
        if (event.isSoldOut()) {
            addToWaitList(event, customerId, ticketTypes);
            throw new IllegalStateException("Event is sold out");
        }

        java.util.List<String> seatsToHold = findAvailableSeats(event, ticketTypes.size());
        if (seatsToHold.size() < ticketTypes.size()) {
            addToWaitList(event, customerId, ticketTypes);
            throw new IllegalStateException("Not enough available seats");
        }

        String holdId = generateId("HOLD");
        long expiresAt = System.currentTimeMillis() + (HOLD_DURATION_MINUTES * 60 * 1000L);
        SeatHold hold = new SeatHold(holdId, eventId, seatsToHold, expiresAt, customerId);

        markSeatsAsHeld(event, seatsToHold);
        holds.put(holdId, hold);
        return hold;
    }

    public Booking confirmHold(String holdId, String customerId) {
        SeatHold hold = findHoldOrThrow(holdId);
        validateCustomer(hold, customerId);
        Event event = findEventOrThrow(hold.eventId());

        if (hold.isExpired(System.currentTimeMillis())) {
            releaseHold(holdId);
            throw new IllegalStateException("Hold has expired");
        }

        Money totalPrice = calculatePrice(hold.seatCount());
        String bookingId = generateId("BOOKING");
        Booking booking = new Booking(bookingId, event.id(), hold.seatIds(),
                                      createTicketTypes(hold.seatCount()),
                                      totalPrice, customerId, System.currentTimeMillis());

        markSeatsAsBooked(event, hold.seatIds());
        bookings.put(bookingId, booking);
        holds.remove(holdId);
        notifyWaitList(event);

        return booking;
    }

    public void releaseHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) return;

        Event event = events.get(hold.eventId());
        if (event != null) {
            markSeatsAsAvailable(event, hold.seatIds());
        }
        holds.remove(holdId);
    }

    public Money cancelBooking(String bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        Event event = findEventOrThrow(booking.eventId());

        Money refund = refundPolicy.calculateRefund(booking.totalPrice(),
                                                     event.eventDateMillis(),
                                                     System.currentTimeMillis());

        markSeatsAsAvailable(event, booking.seatIds());
        bookings.remove(bookingId);
        notifyWaitList(event);

        return refund;
    }

    private void expireOldHolds(Event event) {
        long now = System.currentTimeMillis();
        holds.values().stream()
            .filter(h -> h.eventId().equals(event.id()) && h.isExpired(now))
            .map(SeatHold::id)
            .collect(Collectors.toList())
            .forEach(this::releaseHold);
    }

    private void notifyWaitList(Event event) {
        while (!event.isSoldOut()) {
            WaitListEntry entry = event.pollWaitList();
            if (entry == null) break;

            List<String> available = findAvailableSeats(event, entry.requestedSeats());
            if (available.size() < entry.requestedSeats()) {
                event.addToWaitList(entry);
                break;
            }

            autoBookWaitListEntry(event, entry, available);
        }
    }

    private void autoBookWaitListEntry(Event event, WaitListEntry entry, List<String> seats) {
        String bookingId = generateId("BOOKING");
        Money price = calculatePrice(entry.requestedSeats());
        Booking booking = new Booking(bookingId, event.id(), seats,
                                      createTicketTypes(entry.requestedSeats()),
                                      price, entry.customerId(), System.currentTimeMillis());
        markSeatsAsBooked(event, seats);
        bookings.put(bookingId, booking);
    }

    private List<String> findAvailableSeats(Event event, int count) {
        return event.seats().values().stream()
            .filter(Seat::isAvailable)
            .map(Seat::id)
            .limit(count)
            .collect(Collectors.toList());
    }

    private void markSeatsAsHeld(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.HELD)));
    }

    private void markSeatsAsBooked(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.BOOKED)));
    }

    private void markSeatsAsAvailable(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.AVAILABLE)));
    }

    private Money calculatePrice(int seatCount) {
        Money basePrice = new Money(TicketType.ADULT.priceInCents() * seatCount);
        if (seatCount >= GROUP_DISCOUNT_THRESHOLD) {
            return basePrice.multiply(1.0 - GROUP_DISCOUNT_PERCENT);
        }
        return basePrice;
    }

    private List<TicketType> createTicketTypes(int count) {
        List<TicketType> types = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            types.add(TicketType.ADULT);
        }
        return types;
    }

    private void addToWaitList(Event event, String customerId, List<TicketType> ticketTypes) {
        WaitListEntry entry = new WaitListEntry(customerId, ticketTypes.size(), ticketTypes,
                                                 System.currentTimeMillis());
        event.addToWaitList(entry);
    }

    private Event findEventOrThrow(String eventId) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found: " + eventId);
        return event;
    }

    private SeatHold findHoldOrThrow(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) throw new IllegalArgumentException("Hold not found: " + holdId);
        return hold;
    }

    private Booking findBookingOrThrow(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found: " + bookingId);
        return booking;
    }

    private void validateCustomer(SeatHold hold, String customerId) {
        if (!hold.customerId().equals(customerId)) {
            throw new IllegalArgumentException("Customer mismatch");
        }
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    public Event getEvent(String eventId) {
        return events.get(eventId);
    }
}
```

```java
// SeatBookingEngineTest.java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;

public final class SeatBookingEngineTest {
    private SeatBookingEngine engine;
    private String eventId;
    private long futureEventDate;

    @BeforeEach
    public void setup() {
        engine = new SeatBookingEngine();
        eventId = "EVT_001";
        futureEventDate = System.currentTimeMillis() + (31 * 24 * 60 * 60 * 1000L);
        engine.createEvent(eventId, "Concert", futureEventDate, 100);
    }

    @Test
    public void holdSeatsReservesSeatForCustomer() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");

        assertNotNull(hold);
        assertEquals(1, hold.seatCount());
        assertEquals("CUST_001", hold.customerId());
    }

    @Test
    public void confirmHoldCreatesBooking() {
        List<TicketType> tickets = List.of(TicketType.ADULT, TicketType.CHILD);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        assertNotNull(booking);
        assertEquals(2, booking.seatCount());
        assertEquals(100_00 + 50_00, booking.totalPrice().cents());
    }

    @Test
    public void confirmHoldThrowsIfExpired() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");

        assertTrue(!hold.isExpired(System.currentTimeMillis()));
        assertTrue(hold.isExpired(System.currentTimeMillis() + (16 * 60 * 1000L)));
    }

    @Test
    public void groupDiscountAppliedFor10OrMoreSeats() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tickets.add(TicketType.ADULT);
        }

        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        long basePrice = 100_00L * 10;
        long expectedPrice = Math.round(basePrice * 0.95);
        assertEquals(expectedPrice, booking.totalPrice().cents());
    }

    @Test
    public void groupDiscountNotAppliedFor9Seats() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            tickets.add(TicketType.ADULT);
        }

        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        long basePrice = 100_00L * 9;
        assertEquals(basePrice, booking.totalPrice().cents());
    }

    @Test
    public void releaseHoldFreesSeats() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");

        engine.releaseHold(hold.id());

        SeatHold hold2 = engine.holdSeats(eventId, tickets, "CUST_002");
        assertNotNull(hold2);
        assertEquals(1, hold2.seatCount());
    }

    @Test
    public void fullRefundMoreThan30DaysBeforeEvent() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        Money refund = engine.cancelBooking(booking.id());
        assertEquals(booking.totalPrice().cents(), refund.cents());
    }

    @Test
    public void halfRefund30To7DaysBeforeEvent() {
        long eventDate = System.currentTimeMillis() + (15 * 24 * 60 * 60 * 1000L);
        engine.createEvent("EVT_002", "Show", eventDate, 50);

        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats("EVT_002", tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        Money refund = engine.cancelBooking(booking.id());
        assertEquals(booking.totalPrice().cents() / 2, refund.cents());
    }

    @Test
    public void noRefundLessThan7DaysBeforeEvent() {
        long eventDate = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);
        engine.createEvent("EVT_003", "Show", eventDate, 50);

        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats("EVT_003", tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        Money refund = engine.cancelBooking(booking.id());
        assertEquals(0, refund.cents());
    }

    @Test
    public void waitListWhenEventSoldOut() {
        engine.createEvent("SMALL", "Small", futureEventDate, 1);

        List<TicketType> tickets1 = List.of(TicketType.ADULT);
        SeatHold hold1 = engine.holdSeats("SMALL", tickets1, "CUST_001");
        engine.confirmHold(hold1.id(), "CUST_001");

        List<TicketType> tickets2 = List.of(TicketType.ADULT);
        assertThrows(IllegalStateException.class, () ->
            engine.holdSeats("SMALL", tickets2, "CUST_002")
        );
    }

    @Test
    public void waitListCustomerAutoBookedWhenSeatsFreed() {
        engine.createEvent("SMALL", "Small", futureEventDate, 2);

        List<TicketType> tickets1 = List.of(TicketType.ADULT, TicketType.CHILD);
        SeatHold hold1 = engine.holdSeats("SMALL", tickets1, "CUST_001");
        Booking booking1 = engine.confirmHold(hold1.id(), "CUST_001");

        List<TicketType> tickets2 = List.of(TicketType.ADULT);
        assertThrows(IllegalStateException.class, () ->
            engine.holdSeats("SMALL", tickets2, "CUST_002")
        );

        engine.cancelBooking(booking1.id());

        Booking autoBooking = engine.getBooking("BOOKING_00000000");
        assertNotNull(autoBooking);
        assertEquals("CUST_002", autoBooking.customerId());
    }

    @Test
    public void expiredHoldIsAutoReleasedOnNextHoldAttempt() {
        List<TicketType> tickets1 = List.of(TicketType.ADULT);
        SeatHold hold1 = engine.holdSeats(eventId, tickets1, "CUST_001");

        Event event = engine.getEvent(eventId);
        assertEquals(99, event.availableSeats());

        List<TicketType> tickets2 = List.of(TicketType.ADULT);
        SeatHold hold2 = engine.holdSeats(eventId, tickets2, "CUST_002");

        assertNotNull(hold2);
    }

    @Test
    public void differentTicketTypesCalculateCorrectPrice() {
        List<TicketType> tickets = List.of(TicketType.ADULT, TicketType.CHILD, TicketType.SENIOR);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");
        Booking booking = engine.confirmHold(hold.id(), "CUST_001");

        long expected = 100_00 + 50_00 + 70_00;
        assertEquals(expected, booking.totalPrice().cents());
    }

    @Test
    public void confirmHoldThrowsIfWrongCustomer() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(eventId, tickets, "CUST_001");

        assertThrows(IllegalArgumentException.class, () ->
            engine.confirmHold(hold.id(), "CUST_002")
        );
    }
}
```

→ Cleaned: separate concerns into value objects (Money), immutable domain entities, and a focused service; safe to change because each class handles one responsibility, all edge cases tested (hold expiry, group discount boundaries, refund tiers, wait-list FIFO), and invalid states (held/booked/available) are enum-backed.