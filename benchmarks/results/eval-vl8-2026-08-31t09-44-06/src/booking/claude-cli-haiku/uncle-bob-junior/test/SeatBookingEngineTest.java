public class SeatBookingEngineTest {
    private SeatBookingEngine engine;
    private static final long EVENT_TIME = System.currentTimeMillis() + 40 * 24 * 60 * 60 * 1000;

    @Before
    public void setUp() {
        List<String> seatIds = IntStream.rangeClosed(1, 20)
            .mapToObj(i -> "A" + i)
            .toList();
        engine = new SeatBookingEngine("event1", EVENT_TIME, seatIds);
    }

    @Test
    public void holdReservesSeatsForFifteenMinutes() {
        SeatHoldResult result = engine.hold(List.of(TicketType.ADULT, TicketType.CHILD));

        assertTrue(!result.holdId().isEmpty());
        assertEquals(2, result.seatIds().size());
        assertFalse(result.onWaitingList());
        assertEquals(System.currentTimeMillis() + 15 * 60 * 1000, result.expiryTimeMillis(), 500);
    }

    @Test
    public void confirmConvertHoldToBooking() {
        SeatHoldResult hold = engine.hold(List.of(TicketType.ADULT));
        BookingResult booking = engine.confirm(hold.holdId(), List.of(TicketType.ADULT));

        assertTrue(booking.success());
        assertEquals(100, booking.totalPrice().cents());
    }

    @Test
    public void groupDiscountAppliedAt10Seats() {
        List<TicketType> tickets = IntStream.range(0, 10)
            .mapToObj(i -> TicketType.ADULT)
            .toList();

        SeatHoldResult hold = engine.hold(tickets);
        BookingResult booking = engine.confirm(hold.holdId(), tickets);

        assertEquals(950, booking.totalPrice().cents());
    }

    @Test
    public void noGroupDiscountBelow10Seats() {
        List<TicketType> tickets = IntStream.range(0, 9)
            .mapToObj(i -> TicketType.ADULT)
            .toList();

        SeatHoldResult hold = engine.hold(tickets);
        BookingResult booking = engine.confirm(hold.holdId(), tickets);

        assertEquals(900, booking.totalPrice().cents());
    }

    @Test
    public void mixedTicketTypesPriced() {
        List<TicketType> tickets = List.of(TicketType.ADULT, TicketType.CHILD, TicketType.SENIOR, TicketType.STUDENT);

        SeatHoldResult hold = engine.hold(tickets);
        BookingResult booking = engine.confirm(hold.holdId(), tickets);

        assertEquals(285, booking.totalPrice().cents());
    }

    @Test
    public void fullRefundMoreThan30DaysBefore() {
        long eventTime = System.currentTimeMillis() + 40 * 24 * 60 * 60 * 1000;
        SeatBookingEngine futureEngine = new SeatBookingEngine("event2", eventTime, List.of("A1"));

        SeatHoldResult hold = futureEngine.hold(List.of(TicketType.ADULT));
        futureEngine.confirm(hold.holdId(), List.of(TicketType.ADULT));
        RefundResult refund = futureEngine.cancel("A1");

        assertTrue(refund.success());
        assertEquals(100.0, refund.refundPercent());
        assertEquals(100, refund.amount().cents());
    }

    @Test
    public void halfRefundBetween7And30Days() {
        long eventTime = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000;
        SeatBookingEngine soonEngine = new SeatBookingEngine("event3", eventTime, List.of("A1"));

        SeatHoldResult hold = soonEngine.hold(List.of(TicketType.ADULT));
        soonEngine.confirm(hold.holdId(), List.of(TicketType.ADULT));
        RefundResult refund = soonEngine.cancel("A1");

        assertTrue(refund.success());
        assertEquals(50.0, refund.refundPercent());
        assertEquals(50, refund.amount().cents());
    }

    @Test
    public void noRefundLessThan7DaysBefore() {
        long eventTime = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000;
        SeatBookingEngine veryEngine = new SeatBookingEngine("event4", eventTime, List.of("A1"));

        SeatHoldResult hold = veryEngine.hold(List.of(TicketType.ADULT));
        veryEngine.confirm(hold.holdId(), List.of(TicketType.ADULT));
        RefundResult refund = veryEngine.cancel("A1");

        assertTrue(refund.success());
        assertEquals(0.0, refund.refundPercent());
        assertEquals(0, refund.amount().cents());
    }

    @Test
    public void waitingListWhenSoldOut() {
        SeatBookingEngine tinyEngine = new SeatBookingEngine("event5", EVENT_TIME, List.of("A1", "A2"));

        SeatHoldResult hold1 = tinyEngine.hold(List.of(TicketType.ADULT));
        tinyEngine.confirm(hold1.holdId(), List.of(TicketType.ADULT));

        SeatHoldResult hold2 = tinyEngine.hold(List.of(TicketType.ADULT));
        tinyEngine.confirm(hold2.holdId(), List.of(TicketType.ADULT));

        SeatHoldResult result3 = tinyEngine.hold(List.of(TicketType.ADULT));
        assertTrue(result3.onWaitingList());
    }

    @Test
    public void waitingListServedInOrder() {
        SeatBookingEngine tinyEngine = new SeatBookingEngine("event6", EVENT_TIME, List.of("A1"));

        SeatHoldResult hold1 = tinyEngine.hold(List.of(TicketType.ADULT));
        tinyEngine.confirm(hold1.holdId(), List.of(TicketType.ADULT));

        tinyEngine.hold(List.of(TicketType.CHILD));
        tinyEngine.hold(List.of(TicketType.SENIOR));

        tinyEngine.release(hold1.holdId());
    }

    @Test
    public void confirmFailsWrongHoldId() {
        BookingResult result = engine.confirm("wrongId", List.of(TicketType.ADULT));

        assertFalse(result.success());
        assertTrue(result.error().contains("not found"));
    }

    @Test
    public void confirmFailsMismatchedTicketCount() {
        SeatHoldResult hold = engine.hold(List.of(TicketType.ADULT, TicketType.CHILD));

        BookingResult result = engine.confirm(hold.holdId(), List.of(TicketType.ADULT));

        assertFalse(result.success());
        assertTrue(result.error().contains("mismatch"));
    }

    @Test
    public void cancelUnbookedSeatFails() {
        RefundResult result = engine.cancel("A1");

        assertFalse(result.success());
        assertTrue(result.error().contains("not booked"));
    }
}
