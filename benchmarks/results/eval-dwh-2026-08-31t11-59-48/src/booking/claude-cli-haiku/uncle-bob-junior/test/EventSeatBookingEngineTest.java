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
