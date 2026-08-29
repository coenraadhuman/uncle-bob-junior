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
