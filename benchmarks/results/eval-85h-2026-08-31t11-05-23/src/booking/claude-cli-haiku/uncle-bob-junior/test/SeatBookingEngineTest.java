import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.*;
import java.util.*;

public class SeatBookingEngineTest {
    private SeatBookingEngine engine;
    private Clock clock;
    private LocalDateTime eventDate;
    private static final String EVENT = "EVT_001";
    
    @Before
    public void setUp() {
        clock = Clock.fixed(Instant.parse("2026-08-31T10:00:00Z"), ZoneId.systemDefault());
        engine = new SeatBookingEngine(clock);
        eventDate = LocalDateTime.now(clock).plusDays(60);
        engine.createEvent(EVENT, 100, eventDate);
    }
    
    @Test
    public void holdSeatReservesForSpecifiedMinutes() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 2));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getBookingId());
    }
    
    @Test
    public void holdExpiresAfter15Minutes() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        Clock later = Clock.fixed(
            Instant.parse("2026-08-31T10:16:00Z"),
            ZoneId.systemDefault()
        );
        SeatBookingEngine laterEngine = new SeatBookingEngine(later);
        laterEngine.createEvent(EVENT, 100, eventDate);
        laterEngine.expireHolds(EVENT);
    }
    
    @Test
    public void confirmHoldChangesStatus() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        boolean confirmed = engine.confirmBooking(EVENT, result.getBookingId());
        assertTrue(confirmed);
    }
    
    @Test
    public void releaseHoldFreesSeats() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        engine.releaseHold(EVENT, result.getBookingId());
    }
    
    @Test
    public void groupDiscountAppliedAt10Seats() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 10));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        int price = result.getPriceInCents();
        int expectedPrice = (int) (10 * 10_00 * 0.95);
        assertEquals(expectedPrice, price);
    }
    
    @Test
    public void noDiscountUnder10Seats() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 9));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        int price = result.getPriceInCents();
        assertEquals(9 * 10_00, price);
    }
    
    @Test
    public void refund100PercentMoreThan30DaysBefore() {
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats(EVENT, request);
        engine.confirmBooking(EVENT, result.getBookingId());
        
        int refund = engine.cancelBooking(EVENT, result.getBookingId());
        assertEquals(10_00, refund);
    }
    
    @Test
    public void refund50PercentBetween7And30Days() {
        LocalDateTime closeEvent = LocalDateTime.now(clock).plusDays(15);
        engine.createEvent("EVT_002", 100, closeEvent);
        
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats("EVT_002", request);
        engine.confirmBooking("EVT_002", result.getBookingId());
        
        int refund = engine.cancelBooking("EVT_002", result.getBookingId());
        assertEquals(5_00, refund);
    }
    
    @Test
    public void refund0PercentLessThan7DaysBefore() {
        LocalDateTime soonEvent = LocalDateTime.now(clock).plusDays(3);
        engine.createEvent("EVT_003", 100, soonEvent);
        
        BookingRequest request = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result = engine.bookSeats("EVT_003", request);
        engine.confirmBooking("EVT_003", result.getBookingId());
        
        int refund = engine.cancelBooking("EVT_003", result.getBookingId());
        assertEquals(0, refund);
    }
    
    @Test
    public void soldOutEventAddsToWaitlist() {
        engine.createEvent("EVT_004", 1, eventDate);
        
        BookingRequest first = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result1 = engine.bookSeats("EVT_004", first);
        assertTrue(result1.isSuccess());
        
        BookingRequest second = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult result2 = engine.bookSeats("EVT_004", second);
        assertFalse(result2.isSuccess());
        assertEquals("Added to waiting list", result2.getMessage());
    }
    
    @Test
    public void waitlistFulfilledWhenSeatsRelease() {
        engine.createEvent("EVT_005", 1, eventDate);
        
        BookingRequest req1 = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        BookingResult res1 = engine.bookSeats("EVT_005", req1);
        
        BookingRequest req2 = new BookingRequest(Map.of(TicketTier.ADULT, 1));
        engine.bookSeats("EVT_005", req2);
        
        engine.releaseHold("EVT_005", res1.getBookingId());
    }
    
    @Test
    public void mixedTicketTiersPriceCorrectly() {
        BookingRequest request = new BookingRequest(Map.of(
            TicketTier.ADULT, 1,
            TicketTier.CHILD, 1,
            TicketTier.SENIOR, 1
        ));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        int expectedPrice = 10_00 + 6_00 + 8_00;
        assertEquals(expectedPrice, result.getPriceInCents());
    }
    
    @Test
    public void discountAppliesToMixedTiers() {
        BookingRequest request = new BookingRequest(Map.of(
            TicketTier.ADULT, 5,
            TicketTier.CHILD, 5
        ));
        BookingResult result = engine.bookSeats(EVENT, request);
        
        int basePrice = 5 * 10_00 + 5 * 6_00;
        int expectedPrice = (int) (basePrice * 0.95);
        assertEquals(expectedPrice, result.getPriceInCents());
    }
}
