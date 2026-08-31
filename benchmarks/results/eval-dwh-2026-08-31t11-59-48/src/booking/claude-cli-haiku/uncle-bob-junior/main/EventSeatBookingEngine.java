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
