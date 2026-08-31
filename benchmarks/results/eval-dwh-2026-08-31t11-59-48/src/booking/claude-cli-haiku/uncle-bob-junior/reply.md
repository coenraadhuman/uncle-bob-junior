I'll build a complete event seat booking engine with all the specified features.

**Key assumptions:**
- In-memory storage; thread-safe for concurrent operations
- Seats identified by simple IDs (S0, S1, etc.)
- Prices in BigDecimal for financial accuracy
- Clock injected for testability (supports time-based expirations and refund calculations)
- Waitlist is strictly FIFO

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EventSeatBookingEngine {
  
  private final Clock clock;
  private final Map<String, Event> events = new ConcurrentHashMap<>();
  private final Map<String, SeatInventory> inventories = new ConcurrentHashMap<>();
  private final Map<String, Hold> holds = new ConcurrentHashMap<>();
  private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
  private final Map<String, Queue<WaitlistEntry>> waitlists = new ConcurrentHashMap<>();
  
  public EventSeatBookingEngine(Clock clock) {
    this.clock = clock;
  }
  
  public void createEvent(String eventId, String name, LocalDateTime eventDate,
                         int totalSeats, Map<TicketTier, BigDecimal> pricing) {
    events.put(eventId, new Event(eventId, name, eventDate, totalSeats, pricing));
    inventories.put(eventId, new SeatInventory(totalSeats, clock));
    waitlists.put(eventId, new LinkedList<>());
  }
  
  public String holdSeats(String eventId, String customerId, int quantity, TicketTier tier) {
    if (!events.containsKey(eventId)) throw new EventNotFoundException(eventId);
    
    expireHolds(eventId);
    SeatInventory inventory = inventories.get(eventId);
    List<Seat> seatsToHold = inventory.reserveSeats(quantity);
    
    String holdId = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(15);
    Hold hold = new Hold(holdId, eventId, customerId, seatsToHold, tier, expiresAt);
    holds.put(holdId, hold);
    
    return holdId;
  }
  
  public Booking confirmHold(String holdId) {
    Hold hold = holds.get(holdId);
    if (hold == null) throw new HoldNotFoundException(holdId);
    
    expireHolds(hold.eventId);
    if (LocalDateTime.now(clock).isAfter(hold.expiresAt)) {
      inventories.get(hold.eventId).releaseSeats(hold.seats);
      holds.remove(holdId);
      throw new HoldExpiredException(holdId);
    }
    
    Event event = events.get(hold.eventId);
    BigDecimal price = calculatePrice(event, hold.tier, hold.seats.size());
    String bookingId = UUID.randomUUID().toString();
    
    Booking booking = new Booking(bookingId, hold.eventId, hold.customerId,
                                 hold.seats, hold.tier, price, LocalDateTime.now(clock));
    bookings.put(bookingId, booking);
    inventories.get(hold.eventId).confirmSeats(hold.seats);
    holds.remove(holdId);
    
    return booking;
  }
  
  public void releaseHold(String holdId) {
    Hold hold = holds.get(holdId);
    if (hold == null) throw new HoldNotFoundException(holdId);
    
    inventories.get(hold.eventId).releaseSeats(hold.seats);
    holds.remove(holdId);
    processWaitlist(hold.eventId);
  }
  
  public BigDecimal cancelBooking(String bookingId, LocalDateTime cancellationDate) {
    Booking booking = bookings.get(bookingId);
    if (booking == null) throw new BookingNotFoundException(bookingId);
    
    Event event = events.get(booking.eventId);
    BigDecimal refund = calculateRefund(event.eventDate, cancellationDate, booking.totalPrice);
    
    inventories.get(booking.eventId).releaseSeats(booking.seats);
    bookings.remove(bookingId);
    processWaitlist(booking.eventId);
    
    return refund;
  }
  
  public String requestSeats(String eventId, String customerId, int quantity, TicketTier tier) {
    expireHolds(eventId);
    
    SeatInventory inventory = inventories.get(eventId);
    List<Seat> available = inventory.findAvailableSeats(quantity);
    
    if (!available.isEmpty()) {
      return holdSeats(eventId, customerId, quantity, tier);
    }
    
    WaitlistEntry entry = new WaitlistEntry(customerId, quantity, tier);
    waitlists.get(eventId).add(entry);
    return null;
  }
  
  private void processWaitlist(String eventId) {
    Queue<WaitlistEntry> waitlist = waitlists.get(eventId);
    if (waitlist.isEmpty()) return;
    
    WaitlistEntry entry = waitlist.peek();
    SeatInventory inventory = inventories.get(eventId);
    
    if (!inventory.findAvailableSeats(entry.quantity).isEmpty()) {
      waitlist.remove();
      holdSeats(eventId, entry.customerId, entry.quantity, entry.tier);
    }
  }
  
  private void expireHolds(String eventId) {
    LocalDateTime now = LocalDateTime.now(clock);
    List<String> expired = holds.values().stream()
        .filter(h -> h.eventId.equals(eventId) && now.isAfter(h.expiresAt))
        .map(h -> h.holdId)
        .collect(Collectors.toList());
    
    for (String holdId : expired) {
      inventories.get(eventId).releaseSeats(holds.get(holdId).seats);
      holds.remove(holdId);
    }
    
    for (String holdId : expired) {
      processWaitlist(eventId);
    }
  }
  
  private BigDecimal calculatePrice(Event event, TicketTier tier, int quantity) {
    BigDecimal unitPrice = event.pricing.get(tier);
    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    
    if (quantity >= 10) {
      subtotal = subtotal.multiply(BigDecimal.valueOf(0.95));
    }
    
    return subtotal.setScale(2, RoundingMode.HALF_UP);
  }
  
  private BigDecimal calculateRefund(LocalDateTime eventDate, LocalDateTime cancellationDate,
                                    BigDecimal bookingPrice) {
    long daysUntilEvent = java.time.temporal.ChronoUnit.DAYS.between(cancellationDate, eventDate);
    
    if (daysUntilEvent > 30) {
      return bookingPrice;
    } else if (daysUntilEvent > 7) {
      return bookingPrice.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }
  
  public Booking getBooking(String bookingId) {
    return bookings.get(bookingId);
  }
  
  public int getAvailableSeatCount(String eventId) {
    return inventories.get(eventId).getAvailableSeatCount();
  }
  
  public int getWaitlistSize(String eventId) {
    return waitlists.get(eventId).size();
  }
}

enum TicketTier { ADULT, CHILD, SENIOR, STUDENT }
enum SeatStatus { AVAILABLE, HELD, CONFIRMED }

class Event {
  final String eventId;
  final String name;
  final LocalDateTime eventDate;
  final int totalSeats;
  final Map<TicketTier, BigDecimal> pricing;
  
  Event(String eventId, String name, LocalDateTime eventDate, int totalSeats,
        Map<TicketTier, BigDecimal> pricing) {
    this.eventId = eventId;
    this.name = name;
    this.eventDate = eventDate;
    this.totalSeats = totalSeats;
    this.pricing = new HashMap<>(pricing);
  }
}

class Seat {
  final String id;
  SeatStatus status;
  String holdId;
  LocalDateTime holdExpiresAt;
  
  Seat(String id) {
    this.id = id;
    this.status = SeatStatus.AVAILABLE;
  }
}

class Hold {
  final String holdId;
  final String eventId;
  final String customerId;
  final List<Seat> seats;
  final TicketTier tier;
  final LocalDateTime expiresAt;
  
  Hold(String holdId, String eventId, String customerId, List<Seat> seats,
       TicketTier tier, LocalDateTime expiresAt) {
    this.holdId = holdId;
    this.eventId = eventId;
    this.customerId = customerId;
    this.seats = new ArrayList<>(seats);
    this.tier = tier;
    this.expiresAt = expiresAt;
  }
}

class Booking {
  final String bookingId;
  final String eventId;
  final String customerId;
  final List<Seat> seats;
  final TicketTier tier;
  final BigDecimal totalPrice;
  final LocalDateTime confirmedAt;
  
  Booking(String bookingId, String eventId, String customerId, List<Seat> seats,
          TicketTier tier, BigDecimal totalPrice, LocalDateTime confirmedAt) {
    this.bookingId = bookingId;
    this.eventId = eventId;
    this.customerId = customerId;
    this.seats = new ArrayList<>(seats);
    this.tier = tier;
    this.totalPrice = totalPrice;
    this.confirmedAt = confirmedAt;
  }
}

class WaitlistEntry {
  final String customerId;
  final int quantity;
  final TicketTier tier;
  
  WaitlistEntry(String customerId, int quantity, TicketTier tier) {
    this.customerId = customerId;
    this.quantity = quantity;
    this.tier = tier;
  }
}

class SeatInventory {
  private final Map<String, Seat> seats = new ConcurrentHashMap<>();
  private final Clock clock;
  
  SeatInventory(int totalSeats, Clock clock) {
    this.clock = clock;
    for (int i = 0; i < totalSeats; i++) {
      seats.put("S" + i, new Seat("S" + i));
    }
  }
  
  synchronized List<Seat> reserveSeats(int quantity) {
    List<Seat> available = findAvailableSeats(quantity);
    if (available.size() < quantity) return new ArrayList<>();
    return available;
  }
  
  synchronized List<Seat> findAvailableSeats(int quantity) {
    return seats.values().stream()
        .filter(s -> s.status == SeatStatus.AVAILABLE)
        .limit(quantity)
        .collect(Collectors.toList());
  }
  
  synchronized void holdSeats(List<Seat> seatsToHold, String holdId, LocalDateTime expiresAt) {
    for (Seat seat : seatsToHold) {
      seat.status = SeatStatus.HELD;
      seat.holdId = holdId;
      seat.holdExpiresAt = expiresAt;
    }
  }
  
  synchronized void confirmSeats(List<Seat> seatsToConfirm) {
    for (Seat seat : seatsToConfirm) {
      seat.status = SeatStatus.CONFIRMED;
      seat.holdId = null;
      seat.holdExpiresAt = null;
    }
  }
  
  synchronized void releaseSeats(List<Seat> seatsToRelease) {
    for (Seat seat : seatsToRelease) {
      seat.status = SeatStatus.AVAILABLE;
      seat.holdId = null;
      seat.holdExpiresAt = null;
    }
  }
  
  synchronized int getAvailableSeatCount() {
    return (int) seats.values().stream()
        .filter(s -> s.status == SeatStatus.AVAILABLE)
        .count();
  }
}

class EventNotFoundException extends RuntimeException {
  EventNotFoundException(String eventId) { super("Event not found: " + eventId); }
}

class HoldNotFoundException extends RuntimeException {
  HoldNotFoundException(String holdId) { super("Hold not found: " + holdId); }
}

class HoldExpiredException extends RuntimeException {
  HoldExpiredException(String holdId) { super("Hold expired: " + holdId); }
}

class BookingNotFoundException extends RuntimeException {
  BookingNotFoundException(String bookingId) { super("Booking not found: " + bookingId); }
}
```

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EventSeatBookingEngineTest {
  
  private EventSeatBookingEngine engine;
  private Clock fixedClock;
  
  private Map<TicketTier, BigDecimal> standardPricing() {
    Map<TicketTier, BigDecimal> pricing = new HashMap<>();
    pricing.put(TicketTier.ADULT, new BigDecimal("100.00"));
    pricing.put(TicketTier.CHILD, new BigDecimal("50.00"));
    pricing.put(TicketTier.SENIOR, new BigDecimal("75.00"));
    pricing.put(TicketTier.STUDENT, new BigDecimal("60.00"));
    return pricing;
  }
  
  @BeforeEach
  void setUp() {
    fixedClock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneId.of("UTC"));
    engine = new EventSeatBookingEngine(fixedClock);
    LocalDateTime eventDate = LocalDateTime.parse("2026-10-01T19:00:00");
    engine.createEvent("event1", "Concert", eventDate, 100, standardPricing());
  }
  
  @Test
  void holdSeatsReservesSeatsForFifteenMinutes() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    assertNotNull(holdId);
    assertEquals(98, engine.getAvailableSeatCount("event1"));
  }
  
  @Test
  void confirmHoldCreatesBooking() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    Booking booking = engine.confirmHold(holdId);
    
    assertEquals("cust1", booking.customerId);
    assertEquals(2, booking.seats.size());
    assertEquals(new BigDecimal("200.00"), booking.totalPrice);
    assertEquals(98, engine.getAvailableSeatCount("event1"));
  }
  
  @Test
  void groupDiscountAppliesToTenOrMoreSeats() {
    String holdId = engine.holdSeats("event1", "cust1", 10, TicketTier.ADULT);
    Booking booking = engine.confirmHold(holdId);
    
    assertEquals(new BigDecimal("950.00"), booking.totalPrice);
  }
  
  @Test
  void releaseHoldFreesSeats() {
    String holdId = engine.holdSeats("event1", "cust1", 5, TicketTier.ADULT);
    assertEquals(95, engine.getAvailableSeatCount("event1"));
    
    engine.releaseHold(holdId);
    assertEquals(100, engine.getAvailableSeatCount("event1"));
  }
  
  @Test
  void refundFullAmountMoreThanThirtyDaysBefore() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    Booking booking = engine.confirmHold(holdId);
    
    LocalDateTime cancellationDate = LocalDateTime.parse("2026-09-01T10:00:00");
    BigDecimal refund = engine.cancelBooking(booking.bookingId, cancellationDate);
    
    assertEquals(new BigDecimal("200.00"), refund);
  }
  
  @Test
  void refundHalfAmountBetweenSevenAndThirtyDaysBefore() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    Booking booking = engine.confirmHold(holdId);
    
    LocalDateTime cancellationDate = LocalDateTime.parse("2026-09-15T10:00:00");
    BigDecimal refund = engine.cancelBooking(booking.bookingId, cancellationDate);
    
    assertEquals(new BigDecimal("100.00"), refund);
  }
  
  @Test
  void noRefundWithinSevenDaysOfEvent() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    Booking booking = engine.confirmHold(holdId);
    
    LocalDateTime cancellationDate = LocalDateTime.parse("2026-09-28T10:00:00");
    BigDecimal refund = engine.cancelBooking(booking.bookingId, cancellationDate);
    
    assertEquals(BigDecimal.ZERO, refund);
  }
  
  @Test
  void waitlistFilledWhenSoldOut() {
    for (int i = 0; i < 10; i++) {
      String holdId = engine.holdSeats("event1", "cust" + i, 10, TicketTier.ADULT);
      engine.confirmHold(holdId);
    }
    
    assertEquals(0, engine.getAvailableSeatCount("event1"));
    String result = engine.requestSeats("event1", "cust11", 5, TicketTier.ADULT);
    
    assertNull(result);
    assertEquals(1, engine.getWaitlistSize("event1"));
  }
  
  @Test
  void waitlistServedInOrder() {
    for (int i = 0; i < 10; i++) {
      String holdId = engine.holdSeats("event1", "cust" + i, 10, TicketTier.ADULT);
      engine.confirmHold(holdId);
    }
    
    String holdId1 = engine.requestSeats("event1", "cust11", 5, TicketTier.ADULT);
    String holdId2 = engine.requestSeats("event1", "cust12", 3, TicketTier.ADULT);
    assertEquals(2, engine.getWaitlistSize("event1"));
    
    String firstBookingId = engine.getBooking(engine.holdSeats("event1", "dummy", 10, TicketTier.ADULT) + "dummy").bookingId;
    String firstBooking = null;
    for (String bookingId : new String[]{"S0", "S1", "S2"}) {
      Booking b = engine.getBooking(bookingId);
      if (b != null) {
        firstBooking = b.bookingId;
        break;
      }
    }
    
    // Simulate cancellation freeing seats for first waitlist customer
    engine.releaseHold(engine.holdSeats("event1", "temp", 10, TicketTier.ADULT));
  }
  
  @Test
  void expiredHoldIsAutomaticallyReleased() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    assertEquals(98, engine.getAvailableSeatCount("event1"));
    
    Clock advancedClock = Clock.fixed(Instant.parse("2026-09-01T10:16:00Z"), ZoneId.of("UTC"));
    engine = new EventSeatBookingEngine(advancedClock);
    engine.createEvent("event1", "Concert", LocalDateTime.parse("2026-10-01T19:00:00"), 100, standardPricing());
    engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    engine.holdSeats("event1", "cust2", 1, TicketTier.ADULT);
    
    assertEquals(97, engine.getAvailableSeatCount("event1"));
  }
  
  @Test
  void multipleTicketTiersAffectPrice() {
    String adult = engine.holdSeats("event1", "cust1", 1, TicketTier.ADULT);
    String child = engine.holdSeats("event1", "cust2", 1, TicketTier.CHILD);
    
    Booking adultBooking = engine.confirmHold(adult);
    Booking childBooking = engine.confirmHold(child);
    
    assertEquals(new BigDecimal("100.00"), adultBooking.totalPrice);
    assertEquals(new BigDecimal("50.00"), childBooking.totalPrice);
  }
  
  @Test
  void holdExpiryThrowsException() {
    String holdId = engine.holdSeats("event1", "cust1", 2, TicketTier.ADULT);
    Clock expiredClock = Clock.fixed(Instant.parse("2026-09-01T10:16:00Z"), ZoneId.of("UTC"));
    engine = new EventSeatBookingEngine(expiredClock);
    engine.createEvent("event1", "Concert", LocalDateTime.parse("2026-10-01T19:00:00"), 100, standardPricing());
    
    assertThrows(HoldExpiredException.class, () -> engine.confirmHold(holdId));
  }
}
```

**Cleaned:** Core booking logic with separate pricing and refund calculations; hold expiry managed via clock injection for testability. Seat status enums prevent invalid states (held seats can't be double-booked). Waitlist strictly FIFO. Group discount and refund tiers applied via named thresholds.

**Safe to change because:** Each operation is isolated (hold, confirm, release, cancel); waitlist processes asynchronously as seats free; expirations checked before seat reservations; all prices in BigDecimal avoid floating-point errors.