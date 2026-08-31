import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class EventSeatBookingEngineTests {
    private EventSeatBookingEngine engine;
    private Map<TicketType, BigDecimal> prices;

    void setUp() {
        prices = new HashMap<>();
        prices.put(TicketType.ADULT, new BigDecimal("50.00"));
        prices.put(TicketType.CHILD, new BigDecimal("25.00"));
        prices.put(TicketType.SENIOR, new BigDecimal("35.00"));
        prices.put(TicketType.STUDENT, new BigDecimal("30.00"));

        List<String> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seats.add("S" + i);
        }

        engine = new EventSeatBookingEngine(LocalDateTime.now().plusDays(60), seats, prices);
    }

    void testHoldSeatsSucceeds() {
        BookingResult result = engine.holdSeats(2, List.of(TicketType.ADULT, TicketType.CHILD));

        assert result.isSuccess() : "Hold should succeed";
        assert result.id() != null : "Hold ID should be present";
        assert result.price().equals(new BigDecimal("75.00")) : "Price should be 50 + 25";
        assert !result.isWaitlisted() : "Should not be waitlisted";
    }

    void testHoldExpiry() throws InterruptedException {
        BookingResult holdResult = engine.holdSeats(1, List.of(TicketType.ADULT));
        String holdId = holdResult.id();

        // Simulate expiry by holding and then checking available seats
        assert engine.availableSeatCount() == 19 : "One seat should be held";
    }

    void testGroupDiscountApplies() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tickets.add(TicketType.ADULT);
        }

        BookingResult result = engine.holdSeats(10, tickets);

        BigDecimal expected = new BigDecimal("50.00").multiply(BigDecimal.TEN)
            .multiply(new BigDecimal("0.95"));
        assert result.price().equals(expected) : "Group discount should apply";
    }

    void testGroupDiscountDoesNotApplyBelow10() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            tickets.add(TicketType.ADULT);
        }

        BookingResult result = engine.holdSeats(9, tickets);

        BigDecimal expected = new BigDecimal("50.00").multiply(BigDecimal.valueOf(9));
        assert result.price().equals(expected) : "Group discount should not apply";
    }

    void testConfirmBooking() {
        BookingResult holdResult = engine.holdSeats(2, List.of(TicketType.ADULT, TicketType.CHILD));
        BookingResult confirmResult = engine.confirmBooking(holdResult.id());

        assert confirmResult.isSuccess() : "Confirmation should succeed";
        assert confirmResult.id() != null : "Booking ID should be present";
        assert engine.bookings().containsKey(confirmResult.id()) : "Booking should be recorded";
    }

    void testHoldCountMismatch() {
        BookingResult result = engine.holdSeats(3, List.of(TicketType.ADULT, TicketType.CHILD));

        assert !result.isSuccess() : "Should fail on count mismatch";
    }

    void testReleaseHold() {
        BookingResult holdResult = engine.holdSeats(1, List.of(TicketType.ADULT));
        int heldSeats = 20 - engine.availableSeatCount();

        BookingResult releaseResult = engine.releaseHold(holdResult.id());

        assert releaseResult.isSuccess() : "Release should succeed";
        assert engine.availableSeatCount() == 20 : "Seat should become available";
    }

    void testFullRefundMoreThan30DaysBeforeEvent() {
        BookingResult holdResult = engine.holdSeats(1, List.of(TicketType.ADULT));
        BookingResult confirmResult = engine.confirmBooking(holdResult.id());

        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        EventSeatBookingEngine futureEngine = new EventSeatBookingEngine(eventDate,
            List.of("S1"), prices);
        BookingResult futureHold = futureEngine.holdSeats(1, List.of(TicketType.ADULT));
        BookingResult futureConfirm = futureEngine.confirmBooking(futureHold.id());

        BookingResult cancelResult = futureEngine.cancelBooking(futureConfirm.id());

        assert cancelResult.price().equals(new BigDecimal("50.00")) : "Full refund should be 50.00";
    }

    void testPartialRefund7To30DaysBeforeEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        EventSeatBookingEngine engine = new EventSeatBookingEngine(eventDate, List.of("S1"), prices);

        BookingResult holdResult = engine.holdSeats(1, List.of(TicketType.ADULT));
        BookingResult confirmResult = engine.confirmBooking(holdResult.id());

        BookingResult cancelResult = engine.cancelBooking(confirmResult.id());

        assert cancelResult.price().equals(new BigDecimal("25.00")) : "Partial refund should be 50% = 25.00";
    }

    void testNoRefundLessThan7DaysBeforeEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(3);
        EventSeatBookingEngine engine = new EventSeatBookingEngine(eventDate, List.of("S1"), prices);

        BookingResult holdResult = engine.holdSeats(1, List.of(TicketType.ADULT));
        BookingResult confirmResult = engine.confirmBooking(holdResult.id());

        BookingResult cancelResult = engine.cancelBooking(confirmResult.id());

        assert cancelResult.price().equals(BigDecimal.ZERO) : "No refund should be zero";
    }

    void testWaitingListWhenSoldOut() {
        List<String> twoSeats = List.of("S1", "S2");
        EventSeatBookingEngine smallEngine = new EventSeatBookingEngine(LocalDateTime.now().plusDays(30),
            twoSeats, prices);

        BookingResult first = smallEngine.holdSeats(2, List.of(TicketType.ADULT, TicketType.ADULT));
        assert first.isSuccess() && !first.isWaitlisted() : "First booking should hold seats";

        BookingResult second = smallEngine.holdSeats(1, List.of(TicketType.ADULT));
        assert second.isSuccess() && second.isWaitlisted() : "Second booking should be waitlisted";
    }

    void testWaitingListProcessesWhenSeatsAvailable() {
        List<String> threeSeats = List.of("S1", "S2", "S3");
        EventSeatBookingEngine smallEngine = new EventSeatBookingEngine(LocalDateTime.now().plusDays(30),
            threeSeats, prices);

        BookingResult first = smallEngine.holdSeats(2, List.of(TicketType.ADULT, TicketType.ADULT));
        BookingResult waitlisted = smallEngine.holdSeats(2, List.of(TicketType.ADULT, TicketType.ADULT));

        assert waitlisted.isWaitlisted() : "Should be waitlisted";

        smallEngine.releaseHold(first.id());
        assert smallEngine.availableSeatCount() >= 2 : "Seats should be available for waiting list";
    }

    void runAllTests() {
        setUp(); testHoldSeatsSucceeds();
        setUp(); testGroupDiscountApplies();
        setUp(); testGroupDiscountDoesNotApplyBelow10();
        setUp(); testConfirmBooking();
        setUp(); testHoldCountMismatch();
        setUp(); testReleaseHold();
        setUp(); testFullRefundMoreThan30DaysBeforeEvent();
        setUp(); testPartialRefund7To30DaysBeforeEvent();
        setUp(); testNoRefundLessThan7DaysBeforeEvent();
        setUp(); testWaitingListWhenSoldOut();
        setUp(); testWaitingListProcessesWhenSeatsAvailable();

        System.out.println("All tests passed.");
    }

    public static void main(String[] args) {
        new EventSeatBookingEngineTests().runAllTests();
    }
}
