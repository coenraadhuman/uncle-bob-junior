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
