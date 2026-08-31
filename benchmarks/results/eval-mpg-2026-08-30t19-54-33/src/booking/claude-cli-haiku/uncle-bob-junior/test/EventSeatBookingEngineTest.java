import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventSeatBookingEngineTest {
    private EventSeatBookingEngine engine;
    private Clock fixedClock;
    private static final LocalDateTime EVENT_TIME = LocalDateTime.of(2026, 9, 15, 19, 0, 0);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 30, 12, 0, 0);
    
    @Before
    public void setUp() {
        fixedClock = Clock.fixed(NOW.atZone(ZoneId.systemDefault()).toInstant(), 
                               ZoneId.systemDefault());
        
        Map<EventSeatBookingEngine.TicketTier, BigDecimal> prices = new EnumMap<>(
            EventSeatBookingEngine.TicketTier.class);
        prices.put(EventSeatBookingEngine.TicketTier.ADULT, new BigDecimal("100.00"));
        prices.put(EventSeatBookingEngine.TicketTier.CHILD, new BigDecimal("100.00"));
        prices.put(EventSeatBookingEngine.TicketTier.SENIOR, new BigDecimal("100.00"));
        prices.put(EventSeatBookingEngine.TicketTier.STUDENT, new BigDecimal("100.00"));
        
        EventSeatBookingEngine.Event event = new EventSeatBookingEngine.Event(
            "Concert", EVENT_TIME, 100, prices);
        engine = new EventSeatBookingEngine(event);
        engine.setClock(fixedClock);
    }
    
    @Test
    public void holdSeatsReducesAvailability() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        assertTrue(result instanceof EventSeatBookingEngine.HoldResult);
        
        EventSeatBookingEngine.HoldResult holdResult = (EventSeatBookingEngine.HoldResult) result;
        assertNotNull(holdResult.getHoldId());
        assertEquals(95, engine.getAvailableSeatCount());
    }
    
    @Test
    public void holdExpires15MinutesAfterCreation() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        assertEquals(95, engine.getAvailableSeatCount());
        
        Instant later = NOW.plusMinutes(16).atZone(ZoneId.systemDefault()).toInstant();
        engine.setClock(Clock.fixed(later, ZoneId.systemDefault()));
        
        engine.expireHolds();
        assertEquals(100, engine.getAvailableSeatCount());
    }
    
    @Test
    public void holdDoesNotExpireAt14Minutes() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        Instant before = NOW.plusMinutes(14).atZone(ZoneId.systemDefault()).toInstant();
        engine.setClock(Clock.fixed(before, ZoneId.systemDefault()));
        
        engine.expireHolds();
        assertEquals(95, engine.getAvailableSeatCount());
    }
    
    @Test
    public void confirmHoldCreatesBooking() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        String bookingId = engine.confirmHold(hold.getHoldId());
        
        assertNotNull(bookingId);
        EventSeatBookingEngine.Booking booking = engine.getBooking(bookingId);
        assertNotNull(booking);
        assertEquals(EventSeatBookingEngine.Booking.BookingStatus.ACTIVE, booking.getStatus());
    }
    
    @Test
    public void releaseHoldFreesSeats() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        assertEquals(95, engine.getAvailableSeatCount());
        engine.releaseHold(hold.getHoldId());
        assertEquals(100, engine.getAvailableSeatCount());
    }
    
    @Test
    public void groupDiscountAppliedFor10Seats() {
        Object result = engine.requestSeats(10, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        BigDecimal expected = new BigDecimal("950.00");
        assertEquals(expected, hold.getTotalPrice());
    }
    
    @Test
    public void noGroupDiscountFor9Seats() {
        Object result = engine.requestSeats(9, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        BigDecimal expected = new BigDecimal("900.00");
        assertEquals(expected, hold.getTotalPrice());
    }
    
    @Test
    public void childTierApplies50PercentDiscount() {
        Object result = engine.requestSeats(1, EventSeatBookingEngine.TicketTier.CHILD);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        BigDecimal expected = new BigDecimal("50.00");
        assertEquals(expected, hold.getTotalPrice());
    }
    
    @Test
    public void studentTierApplies60PercentDiscount() {
        Object result = engine.requestSeats(1, EventSeatBookingEngine.TicketTier.STUDENT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        
        BigDecimal expected = new BigDecimal("60.00");
        assertEquals(expected, hold.getTotalPrice());
    }
    
    @Test
    public void fullRefundMoreThan30DaysBeforeEvent() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        String bookingId = engine.confirmHold(hold.getHoldId());
        
        Instant thirtyFiveDaysBefore = NOW.minusDays(35)
            .atZone(ZoneId.systemDefault()).toInstant();
        engine.setClock(Clock.fixed(thirtyFiveDaysBefore, ZoneId.systemDefault()));
        
        EventSeatBookingEngine.CancellationResult cancellation = 
            engine.cancelBooking(bookingId);
        
        assertEquals(new BigDecimal("500.00"), cancellation.getRefundAmount());
    }
    
    @Test
    public void partialRefundBetween7And30DaysBeforeEvent() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        String bookingId = engine.confirmHold(hold.getHoldId());
        
        Instant fifteenDaysBefore = NOW.minusDays(15)
            .atZone(ZoneId.systemDefault()).toInstant();
        engine.setClock(Clock.fixed(fifteenDaysBefore, ZoneId.systemDefault()));
        
        EventSeatBookingEngine.CancellationResult cancellation = 
            engine.cancelBooking(bookingId);
        
        assertEquals(new BigDecimal("250.00"), cancellation.getRefundAmount());
    }
    
    @Test
    public void noRefundLessThan7DaysBeforeEvent() {
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult hold = (EventSeatBookingEngine.HoldResult) result;
        String bookingId = engine.confirmHold(hold.getHoldId());
        
        Instant fiveDaysBefore = NOW.minusDays(5)
            .atZone(ZoneId.systemDefault()).toInstant();
        engine.setClock(Clock.fixed(fiveDaysBefore, ZoneId.systemDefault()));
        
        EventSeatBookingEngine.CancellationResult cancellation = 
            engine.cancelBooking(bookingId);
        
        assertEquals(BigDecimal.ZERO, cancellation.getRefundAmount());
    }
    
    @Test
    public void waitlistWhenSoldOut() {
        engine.requestSeats(100, EventSeatBookingEngine.TicketTier.ADULT);
        Object result = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.STUDENT);
        
        assertTrue(result instanceof EventSeatBookingEngine.WaitlistResult);
        EventSeatBookingEngine.WaitlistResult waitlist = 
            (EventSeatBookingEngine.WaitlistResult) result;
        
        assertEquals(1, waitlist.getQueuePosition());
        assertEquals(1, engine.getWaitlistSize());
    }
    
    @Test
    public void waitlistProcessedWhenSeatsBecomeFree() {
        Object hold1 = engine.requestSeats(100, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult firstHold = 
            (EventSeatBookingEngine.HoldResult) hold1;
        
        Object waitlistReq = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.STUDENT);
        assertEquals(1, engine.getWaitlistSize());
        
        engine.releaseHold(firstHold.getHoldId());
        assertEquals(0, engine.getWaitlistSize());
        assertEquals(5, engine.getAvailableSeatCount());
    }
    
    @Test
    public void waitlistPrioritizedByOrder() {
        engine.requestSeats(100, EventSeatBookingEngine.TicketTier.ADULT);
        
        Object wait1 = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.STUDENT);
        Object wait2 = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.CHILD);
        
        EventSeatBookingEngine.WaitlistResult first = 
            (EventSeatBookingEngine.WaitlistResult) wait1;
        EventSeatBookingEngine.WaitlistResult second = 
            (EventSeatBookingEngine.WaitlistResult) wait2;
        
        assertEquals(1, first.getQueuePosition());
        assertEquals(2, second.getQueuePosition());
    }
    
    @Test
    public void cancelBookingFreesSeatsForWaitlist() {
        Object hold = engine.requestSeats(100, EventSeatBookingEngine.TicketTier.ADULT);
        EventSeatBookingEngine.HoldResult booking100 = 
            (EventSeatBookingEngine.HoldResult) hold;
        String bookingId = engine.confirmHold(booking100.getHoldId());
        
        Object waitReq = engine.requestSeats(5, EventSeatBookingEngine.TicketTier.STUDENT);
        assertEquals(1, engine.getWaitlistSize());
        
        engine.cancelBooking(bookingId);
        
        assertEquals(0, engine.getWaitlistSize());
        assertEquals(5, engine.getAvailableSeatCount());
    }
    
    @Test
    public void invalidHoldThrowsException() {
        assertThrows(IllegalArgumentException.class, 
                    () -> engine.confirmHold("nonexistent"));
    }
    
    @Test
    public void invalidBookingThrowsException() {
        assertThrows(IllegalArgumentException.class, 
                    () -> engine.cancelBooking("nonexistent"));
    }
}
